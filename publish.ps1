<#
.SYNOPSIS
  Build + publish Fairy Lights mod to GitHub, with SemVer bump, git commit + tag + push.

.DESCRIPTION
  - Requires a clean git working tree before starting (unless using -OnlyCurseForge, -OnlyModrinth, or -SkipBuild).
  - Bumps version (default: patch, i.e. +0.0.1).
  - Updates:
      - gradle.properties (mod_version)
  - Commits, tags vX.Y.Z, pushes branch + tag to origin.
  - Builds the mod using Gradle (unless -SkipBuild, -OnlyCurseForge, or -OnlyModrinth is used).
  - Creates a GitHub release and uploads JAR artifacts.
  - Optionally uploads the main JAR to CurseForge if env vars are set.
  - Optionally uploads the main JAR to Modrinth if env vars are set.
  
  Special modes:
    - -OnlyGit: Only perform git operations (commit, tag, push), skip build and uploads.
    - -OnlyCurseForge: Only upload to CurseForge using existing JARs, skip git operations and build.
    - -OnlyModrinth: Only upload to Modrinth using existing JARs, skip git operations and build.
    - -SkipBuild: Use existing JARs from build/libs without building.

  CurseForge env vars (optional):
    - CF_PROJECT_ID (or CURSEFORGE_PROJECT_ID / CF_PRODUCT_ID / CURSEFORGE_PRODUCT_ID)
    - CF_API_TOKEN  (or CF_API_KEY / CURSEFORGE_API_TOKEN / CURSEFORGE_API_KEY)
    - CF_GAME_VERSION_IDS: comma-separated numeric version IDs (e.g. "9990,68441")
        - Required by CurseForge Upload API. Includes Minecraft version ID and (optionally) mod loader ID.
        - If omitted, the script attempts to auto-resolve IDs via /api/game/versions using minecraft_version from gradle.properties,
          and (if detected) a loader name (default: "NeoForge").
    - CF_ENVIRONMENT_IDS: comma-separated server/client environment IDs (e.g. "1,2")
        - If omitted, the script attempts to auto-resolve Server (ID: 1) and Client (ID: 2) environment IDs.
    - CF_JAVA_VERSION_ID: Java version ID (e.g., Java 21)
        - If omitted, the script attempts to auto-resolve Java 21 version ID.
    - CF_RELEASE_TYPE: release | beta | alpha (default: release)
    - CF_CHANGELOG_FILE: optional path to a changelog file (markdown/text)
        - If not set, automatically looks for changelogs/{version}.md (e.g., changelogs/3.0.1.md)
    - CF_BASE_URL: optional base URL for the upload API (default: https://minecraft.curseforge.com)

  Modrinth env vars (optional):
    - MODRINTH_PROJECT_ID (or MR_PROJECT_ID / MODRINTH_PROJECT)
    - MODRINTH_TOKEN (or MR_TOKEN / MODRINTH_API_TOKEN)
    - MODRINTH_GAME_VERSIONS: comma-separated Minecraft versions (e.g., "1.20.2,1.20.3,1.20.4")
        - If omitted, expands supported_minecraft_version_name from gradle.properties
          (e.g. 1.20.2-1.20.4 -> 1.20.2,1.20.3,1.20.4). Matches GitHub mc-publish standard.
    - MODRINTH_LOADERS: comma-separated mod loaders (e.g., "neoforge", "forge", "fabric")
        - If omitted, uses enabled_platforms from gradle.properties (excluding common).
    - MODRINTH_RELEASE_TYPE: release | beta | alpha (default: release)
    - MODRINTH_CHANGELOG_FILE: optional path to a changelog file (markdown/text)
        - If not set, automatically looks for changelogs/{version}.md (e.g., changelogs/3.0.1.md)

.PARAMETER Bump
  patch | minor | major
  Default: patch

.PARAMETER OnlyPublish
  If set, skip version bump + commit/tag/push and only build+publish.

.PARAMETER Version
  Optional explicit version to publish (X.Y.Z). When provided, no bump is performed.

.PARAMETER Note
  Optional release note string for the GitHub release.

.PARAMETER GitHubToken
  GitHub personal access token with repo scope.
  If omitted, uses $env:GITHUB_TOKEN.

.PARAMETER GitHubRepo
  GitHub repository in format "owner/repo" (e.g., "username/fairy-lights").
  If omitted, tries to detect from git remote.

.EXAMPLE
  pwsh ./publish.ps1

.EXAMPLE
  pwsh ./publish.ps1 -Bump minor -Note "New features added"

.EXAMPLE
  pwsh ./publish.ps1 -OnlyPublish -Version "2.4.0"

.EXAMPLE
  pwsh ./publish.ps1 -OnlyGit
  # Only commit, tag, and push to git (skip build and uploads)

.EXAMPLE
  pwsh ./publish.ps1 -OnlyCurseForge
  # Only upload to CurseForge using existing JARs (skip git operations and build)

.EXAMPLE
  pwsh ./publish.ps1 -OnlyModrinth
  # Only upload to Modrinth using existing JARs (skip git operations and build)
#>

[CmdletBinding()]
param(
  [ValidateSet("patch", "minor", "major")]
  [string]$Bump = "patch",

  # If set, skip version bump + commit/tag/push and only build+publish.
  [switch]$OnlyPublish,

  # Optional explicit version to publish (X.Y.Z). When provided, no bump is performed.
  [string]$Version,

  [string]$Note = "",

  # GitHub options
  [string]$GitHubToken = $env:GITHUB_TOKEN,
  [string]$GitHubRepo,

  # If set, skip CurseForge upload even if env vars are present.
  [switch]$SkipCurseForge,

  # If set, skip Modrinth upload even if env vars are present.
  [switch]$SkipModrinth,

  # If set, only perform git operations (commit, tag, push) and skip build/upload.
  [switch]$OnlyGit,

  # If set, only upload to CurseForge using existing JARs (skip git operations and build).
  [switch]$OnlyCurseForge,

  # If set, only upload to Modrinth using existing JARs (skip git operations and build).
  [switch]$OnlyModrinth,

  # If set, skip building and use existing JARs from build/libs.
  [switch]$SkipBuild
)

$ErrorActionPreference = "Stop"

function Import-DotEnvIfPresent([string]$path) {
  if (-not (Test-Path $path)) { return }
  $lines = Get-Content -Path $path -ErrorAction Stop
  foreach ($line in $lines) {
    $trim = $line.Trim()
    if (-not $trim) { continue }
    if ($trim.StartsWith("#")) { continue }
    $idx = $trim.IndexOf("=")
    if ($idx -lt 1) { continue }
    $key = $trim.Substring(0, $idx).Trim()
    $value = $trim.Substring($idx + 1).Trim()
    # Strip surrounding quotes if present
    if (($value.StartsWith('"') -and $value.EndsWith('"')) -or ($value.StartsWith("'") -and $value.EndsWith("'"))) {
      $value = $value.Substring(1, $value.Length - 2).Trim()
    }
    # Remove any embedded newlines, carriage returns, or tabs (in case token was split across lines)
    $value = $value -replace "[\r\n\t]", ""
    if ($key) {
      # Don't overwrite variables already set in the environment
      $existing = $null
      try {
        $existing = (Get-Item -Path ("Env:{0}" -f $key) -ErrorAction Stop).Value
      } catch {
        $existing = $null
      }
      if (-not [string]::IsNullOrWhiteSpace($existing)) { continue }
      Set-Item -Path "Env:$key" -Value $value
    }
  }
}

function Require-Command([string]$Name) {
  if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
    throw "Missing required command: $Name"
  }
}

function Assert-GitClean() {
  $status = git status --porcelain
  if ($status) {
    throw "Git working tree is not clean. Commit/stash changes before publishing.`n`n$status"
  }
}

function Parse-SemVer([string]$v) {
  # Handle both "2.3" and "2.3.0" formats
  if ($v -match '^v?(\d+)\.(\d+)(?:\.(\d+))?$') {
    return @{
      Major = [int]$Matches[1]
      Minor = [int]$Matches[2]
      Patch = if ($Matches[3]) { [int]$Matches[3] } else { 0 }
    }
  }
  throw "Invalid SemVer version: '$v' (expected X.Y or X.Y.Z)"
}

function Format-SemVer([hashtable]$p) {
  return "$($p.Major).$($p.Minor).$($p.Patch)"
}

function Bump-SemVer([string]$v, [string]$kind) {
  $p = Parse-SemVer $v
  switch ($kind) {
    "major" { $p.Major++; $p.Minor = 0; $p.Patch = 0 }
    "minor" { $p.Minor++; $p.Patch = 0 }
    "patch" { $p.Patch++ }
    default { throw "Unknown bump kind: $kind" }
  }
  return Format-SemVer $p
}

function Read-GradleVersion([string]$gradlePropsPath) {
  $content = Get-Content -Path $gradlePropsPath
  foreach ($line in $content) {
    if ($line -match '^mod_version=(.+)$') {
      return $Matches[1].Trim()
    }
  }
  throw "Could not find mod_version in $gradlePropsPath"
}

function Update-GradleVersion([string]$gradlePropsPath, [string]$newVersion) {
  $content = Get-Content -Path $gradlePropsPath
  $updated = $false
  $newContent = @()
  
  foreach ($line in $content) {
    if ($line -match '^mod_version=(.+)$') {
      $newContent += "mod_version=$newVersion"
      $updated = $true
    } else {
      $newContent += $line
    }
  }
  
  if (-not $updated) {
    throw "Could not find mod_version in $gradlePropsPath"
  }
  
  Set-Content -Path $gradlePropsPath -Value $newContent
}

function Git-CommitTagPush([string]$newVersion) {
  $tag = "v$newVersion"
  git add gradle.properties

  git commit -m "chore(release): $tag"

  # Ensure tag doesn't already exist
  $existing = git tag -l $tag
  if ($existing) {
    throw "Tag already exists: $tag"
  }
  git tag $tag

  # Fetch latest from remote to see if we're behind
  Write-Host "Fetching latest from remote..."
  git fetch origin 2>&1 | Out-Null
  if ($LASTEXITCODE -ne 0) {
    Write-Warning "Failed to fetch from origin, but continuing..."
  }

  # Get current branch name
  $currentBranch = git rev-parse --abbrev-ref HEAD
  if (-not $currentBranch) {
    throw "Could not determine current branch name"
  }

  # Check if remote branch exists and if we're behind
  $remoteBranch = "origin/$currentBranch"
  $remoteExists = git rev-parse --verify "$remoteBranch" 2>$null
  $isBehind = $false
  
  if ($remoteExists) {
    # Check if we're behind the remote
    git fetch origin $currentBranch 2>&1 | Out-Null
    $localCommit = git rev-parse HEAD
    $remoteCommit = git rev-parse $remoteBranch 2>$null
    if ($remoteCommit -and $localCommit) {
      $mergeBase = git merge-base HEAD $remoteBranch 2>$null
      if ($mergeBase -and $remoteCommit -ne $mergeBase) {
        $isBehind = $true
        Write-Host "Remote branch has new commits. Attempting to rebase..."
        # Try to rebase on top of remote
        git rebase $remoteBranch
        if ($LASTEXITCODE -ne 0) {
          Write-Warning "Rebase failed. You may have conflicts. Continuing with tag push only."
          Write-Warning "You'll need to manually resolve: git rebase --continue or git rebase --abort"
        } else {
          Write-Host "Rebase successful"
        }
      }
    }
  }

  # Push branch with all local commits
  Write-Host "Pushing branch $currentBranch to origin..."
  git push origin HEAD
  if ($LASTEXITCODE -ne 0) {
    Write-Warning "Failed to push branch to origin."
    Write-Warning "Continuing with tag push. You may need to manually sync the branch later."
  } else {
    Write-Host "Pushed branch to origin"
  }

  # Push tag (required for releases)
  Write-Host "Pushing tag $tag to origin..."
  git push origin $tag
  if ($LASTEXITCODE -ne 0) {
    throw "Failed to push tag $tag to origin. This is required for releases."
  }
  Write-Host "Pushed tag $tag to origin"

  return $tag
}

function Build-Mod() {
  Write-Host "Building mod with Gradle..."
  
  # Use gradlew if available, otherwise try gradle
  $gradleCmd = if (Test-Path "gradlew.bat") { ".\gradlew.bat" } elseif (Test-Path "gradlew") { ".\gradlew" } else { "gradle" }

  # Prefer clean build, but on Windows file locks in common/build/devlibs can make :clean fail.
  # Artifact discovery already filters by this branch's +mc suffix, so a plain build is safe.
  & $gradleCmd clean build
  if ($LASTEXITCODE -ne 0) {
    Write-Warning "Gradle clean build failed; retrying without clean..."
    & $gradleCmd --stop 2>$null | Out-Null
    & $gradleCmd build
    if ($LASTEXITCODE -ne 0) {
      throw "Gradle build failed"
    }
  }
  
  Write-Host "Build completed successfully"
}

function Get-GradleProperty([string]$name) {
  $line = Get-Content -Path "gradle.properties" | Where-Object { $_ -match ("^{0}=(.+)$" -f [regex]::Escape($name)) } | Select-Object -First 1
  if ($line -and ($line -match ("^{0}=(.+)$" -f [regex]::Escape($name)))) {
    return $Matches[1].Trim()
  }
  return $null
}

function Get-EnabledPlatforms() {
  $raw = Get-GradleProperty "enabled_platforms"
  if (-not $raw) { return @("fabric", "neoforge") }
  return @($raw -split "[,;\s]+" | Where-Object { $_ } | ForEach-Object { $_.Trim().ToLowerInvariant() })
}

function Get-SupportedMinecraftVersionName() {
  $name = Get-GradleProperty "supported_minecraft_version_name"
  if (-not $name) { $name = Get-GradleProperty "minecraft_version" }
  if (-not $name) { throw "Could not read supported_minecraft_version_name or minecraft_version from gradle.properties." }
  return $name
}

function Expand-MinecraftVersionRange([string]$rangeName) {
  # Expand supported_minecraft_version_name into concrete Modrinth game_versions.
  # Examples:
  #   1.21.6          -> 1.21.6
  #   1.20.5-1.20.6   -> 1.20.5, 1.20.6
  #   1.20.2-1.20.4   -> 1.20.2, 1.20.3, 1.20.4
  #   1.20-1.20.1     -> 1.20, 1.20.1
  #   1.21-1.21.1     -> 1.21, 1.21.1
  #   1.21.2-1.21.3   -> 1.21.2, 1.21.3
  if ([string]::IsNullOrWhiteSpace($rangeName)) { return @() }
  if ($rangeName -notmatch '-') { return @($rangeName) }

  $parts = $rangeName -split '-', 2
  if ($parts.Count -ne 2) { return @($rangeName) }
  $start = $parts[0].Trim()
  $end = $parts[1].Trim()

  function Parse-Mc([string]$v) {
    if ($v -match '^(\d+)\.(\d+)(?:\.(\d+))?$') {
      return @{
        Major = [int]$Matches[1]
        Minor = [int]$Matches[2]
        Patch = if ($Matches[3]) { [int]$Matches[3] } else { -1 } # -1 means "major.minor" form (e.g. 1.20)
        Raw = $v
      }
    }
    return $null
  }

  $a = Parse-Mc $start
  $b = Parse-Mc $end
  if (-not $a -or -not $b) { return @($start, $end) | Select-Object -Unique }

  # Same major.minor, expand patch numbers (treat missing patch as 0 for ordering, but keep display form)
  if ($a.Major -eq $b.Major -and $a.Minor -eq $b.Minor) {
    $startPatch = if ($a.Patch -lt 0) { 0 } else { $a.Patch }
    $endPatch = if ($b.Patch -lt 0) { 0 } else { $b.Patch }
    if ($startPatch -gt $endPatch) { $tmp = $startPatch; $startPatch = $endPatch; $endPatch = $tmp }

    $versions = @()
    for ($p = $startPatch; $p -le $endPatch; $p++) {
      if ($p -eq 0 -and $a.Patch -lt 0) {
        $versions += "{0}.{1}" -f $a.Major, $a.Minor
      } else {
        $versions += "{0}.{1}.{2}" -f $a.Major, $a.Minor, $p
      }
    }
    # Ensure endpoint raw forms are present (e.g. include both 1.20 and 1.20.1)
    if ($versions -notcontains $start) { $versions = @($start) + $versions }
    if ($versions -notcontains $end) { $versions += $end }
    return @($versions | Select-Object -Unique)
  }

  # Adjacent minors with explicit patches (e.g. 1.21.2-1.21.3)
  if ($a.Major -eq $b.Major -and $b.Minor -eq ($a.Minor + 1) -and $a.Patch -ge 0 -and $b.Patch -ge 0) {
    $versions = @()
    for ($p = $a.Patch; $p -le 20; $p++) { # safety cap; MC patches rarely exceed this per minor
      $versions += "{0}.{1}.{2}" -f $a.Major, $a.Minor, $p
      if ($a.Minor -eq $b.Minor -and $p -ge $b.Patch) { break }
    }
    # Simpler: just list start..end minors' known endpoints when only one patch each
    return @($start, $end) | Select-Object -Unique
  }

  # Fallback: include both endpoints
  return @($start, $end) | Select-Object -Unique
}

function Find-BuildArtifacts() {
  # Discover loader JARs from subprojects (fabric/forge/neoforge), scoped to this branch's MC range.
  # Avoids picking up stale jars left from publishing other version branches.
  $supportedName = Get-SupportedMinecraftVersionName
  $mcSuffix = "+mc$supportedName"
  $platforms = Get-EnabledPlatforms
  $scanLoaders = @($platforms + @("common") | Select-Object -Unique)
  $foundArtifacts = @()

  foreach ($loader in $scanLoaders) {
    $libsDir = "$loader/build/libs"
    if (-not (Test-Path $libsDir)) { continue }

    $loaderArtifacts = Get-ChildItem -Path $libsDir -Filter "*.jar" | Where-Object {
      $name = $_.Name
      -not $name.Contains("-sources") -and
      -not $name.Contains("-javadoc") -and
      -not $name.Contains("-dev-shadow") -and
      -not $name.Contains("-dev") -and
      -not $name.Contains("transformProduction") -and
      (
        $name.Contains($mcSuffix) -or
        # Older naming: noisiumed-3.0.6-neoforge-1.21.1.jar
        ($name -match ("-{0}\.jar$" -f [regex]::Escape((Get-GradleProperty "minecraft_version"))))
      )
    }

    foreach ($art in $loaderArtifacts) {
      $art | Add-Member -MemberType NoteProperty -Name "Loader" -Value $loader -Force
      $foundArtifacts += $art
    }
  }

  # Fallback: root build/libs (single-jar projects)
  if ($foundArtifacts.Count -eq 0 -and (Test-Path "build/libs")) {
    $rootArts = Get-ChildItem -Path "build/libs" -Filter "*.jar" | Where-Object {
      $name = $_.Name
      -not $name.Contains("-sources") -and -not $name.Contains("-javadoc") -and -not $name.Contains("-dev")
    }
    foreach ($art in $rootArts) {
      $art | Add-Member -MemberType NoteProperty -Name "Loader" -Value "unknown" -Force
      $foundArtifacts += $art
    }
  }

  if ($foundArtifacts.Count -eq 0) {
    throw "No JAR artifacts found for MC range '$supportedName' in loader subprojects: $($platforms -join ', ')"
  }

  Write-Host "Discovered artifacts for MC $supportedName (loaders: $(($foundArtifacts.Loader | Select-Object -Unique) -join ', ')):"
  foreach ($a in $foundArtifacts) { Write-Host "  - $($a.Name) [$($a.Loader)]" }
  return $foundArtifacts
}

function Get-EnvAny([string[]]$Names) {
  foreach ($n in $Names) {
    try {
      $v = (Get-Item -Path ("Env:{0}" -f $n) -ErrorAction Stop).Value
      if (-not [string]::IsNullOrWhiteSpace($v)) { return $v }
    } catch {
      # ignore missing
    }
  }
  return $null
}

function Get-CurseForgeConfig() {
  $projectId = Get-EnvAny @("CF_PROJECT_ID", "CURSEFORGE_PROJECT_ID", "CF_PRODUCT_ID", "CURSEFORGE_PRODUCT_ID")
  $token = Get-EnvAny @("CF_API_TOKEN", "CF_API_KEY", "CURSEFORGE_API_TOKEN", "CURSEFORGE_API_KEY")
  $baseUrl = Get-EnvAny @("CF_BASE_URL")
  if (-not $baseUrl) { $baseUrl = "https://minecraft.curseforge.com" }

  # Clean up token: trim whitespace and remove any control characters
  if ($token) {
    # Remove all whitespace (spaces, tabs, newlines) from start and end
    $token = $token.Trim()
    # Remove any embedded newlines, carriage returns, or tabs that might have been introduced
    $token = $token -replace "[\r\n\t]", ""
    # Remove any trailing/leading quotes if they exist
    if (($token.StartsWith('"') -and $token.EndsWith('"')) -or ($token.StartsWith("'") -and $token.EndsWith("'"))) {
      $token = $token.Substring(1, $token.Length - 2)
    }
  }

  return @{
    ProjectId = $projectId
    Token = $token
    BaseUrl = $baseUrl.TrimEnd("/")
    ReleaseType = (Get-EnvAny @("CF_RELEASE_TYPE"))
    GameVersionIdsRaw = (Get-EnvAny @("CF_GAME_VERSION_IDS"))
    ChangelogFile = (Get-EnvAny @("CF_CHANGELOG_FILE"))
    ModLoaderName = (Get-EnvAny @("CF_MOD_LOADER", "CF_MODLOADER", "CF_LOADER")) # optional; e.g. "NeoForge"
    EnvironmentIdsRaw = (Get-EnvAny @("CF_ENVIRONMENT_IDS", "CF_ENV_IDS")) # optional; comma-separated server/client environment IDs
    JavaVersionIdRaw = (Get-EnvAny @("CF_JAVA_VERSION_ID", "CF_JAVA_ID")) # optional; Java version ID (e.g., Java 21)
  }
}

function Get-CurseForgeGameVersions([hashtable]$cf) {
  # Unofficial but widely used endpoint for the Upload API host.
  $uri = "$($cf.BaseUrl)/api/game/versions"
  $headers = @{ "X-Api-Token" = $cf.Token }
  return Invoke-RestMethod -Uri $uri -Headers $headers -Method Get -ErrorAction Stop
}

function Resolve-CurseForgeGameVersionIds([hashtable]$cf, [string[]]$minecraftVersions, [string]$loaderName = $null) {
  # Prefer explicit IDs if user supplied them.
  if (-not [string]::IsNullOrWhiteSpace($cf.GameVersionIdsRaw)) {
    $ids = @()
    foreach ($part in ($cf.GameVersionIdsRaw -split "[,;\s]+" | Where-Object { $_ })) {
      $t = $part.Trim()
      if ($t -match '^\d+$') {
        $ids += [int]$t
      } else {
        throw "CF_GAME_VERSION_IDS contains a non-numeric value: '$t'"
      }
    }
    if ($ids.Count -eq 0) { throw "CF_GAME_VERSION_IDS was provided but no IDs could be parsed." }
    return $ids
  }

  # Auto-resolve via /api/game/versions (best effort).
  $all = Get-CurseForgeGameVersions $cf
  if (-not $all) { throw "Failed to retrieve CurseForge game versions from $($cf.BaseUrl)/api/game/versions" }

  $resolved = @()

  foreach ($minecraftVersion in $minecraftVersions) {
    # CurseForge lists the same MC version under multiple gameVersionTypeIDs.
    # Type 1 = environment, 615 = invalid-for-upload dependency bucket.
    # Prefer the real Minecraft Java type (e.g. 75125 for 1.20.x, 77784 for 1.21.x).
    $candidates = @($all | Where-Object {
      ($_.name -eq $minecraftVersion) -or ($_.versionString -eq $minecraftVersion) -or ($_.slug -eq $minecraftVersion)
    })
    $mc = $candidates | Where-Object { $_.gameVersionTypeID -notin @(1, 615) } | Select-Object -First 1
    if (-not $mc) { $mc = $candidates | Select-Object -First 1 }
    if ($mc -and $mc.id) {
      $resolved += [int]$mc.id
      Write-Host "Auto-resolved CurseForge Minecraft $($minecraftVersion): $($mc.id) (type $($mc.gameVersionTypeID))"
    } else {
      Write-Warning "Could not resolve CurseForge Minecraft version ID for '$minecraftVersion'."
    }
  }

  # If this is a loader-specific build, also try to include the loader ID.
  if (-not $loaderName) { $loaderName = $cf.ModLoaderName }
  if (-not $loaderName) { $loaderName = "NeoForge" }

  $loader = $all | Where-Object {
    ($_.name -eq $loaderName) -or ($_.slug -eq $loaderName) -or ($_.name -like "*$loaderName*")
  } | Select-Object -First 1

  if ($loader -and $loader.id) {
    $resolved += [int]$loader.id
    Write-Host "Auto-resolved CurseForge loader ID for ${loaderName}: $($loader.id)"
  }

  # Add server/client environment IDs if not explicitly provided
  if (-not [string]::IsNullOrWhiteSpace($cf.EnvironmentIdsRaw)) {
    $envIds = @()
    foreach ($part in ($cf.EnvironmentIdsRaw -split "[,;\s]+" | Where-Object { $_ })) {
      $t = $part.Trim()
      if ($t -match '^\d+$') {
        $envIds += [int]$t
      }
    }
    $resolved += $envIds
  } else {
    # Environment group (gameVersionTypeID 75208): Client=9638, Server=9639
    $serverEnv = $all | Where-Object {
      ($_.name -eq "Server" -or $_.slug -eq "server") -and ($_.gameVersionTypeID -eq 75208 -or -not $_.gameVersionTypeID)
    } | Select-Object -First 1
    $clientEnv = $all | Where-Object {
      ($_.name -eq "Client" -or $_.slug -eq "client") -and ($_.gameVersionTypeID -eq 75208 -or -not $_.gameVersionTypeID)
    } | Select-Object -First 1

    if (-not $serverEnv) { $serverEnv = $all | Where-Object { $_.id -eq 9639 } | Select-Object -First 1 }
    if (-not $clientEnv) { $clientEnv = $all | Where-Object { $_.id -eq 9638 } | Select-Object -First 1 }
    
    if ($serverEnv -and $serverEnv.id) { 
      $resolved += [int]$serverEnv.id
      Write-Host "Auto-resolved Server environment ID: $($serverEnv.id)"
    }
    if ($clientEnv -and $clientEnv.id) { 
      $resolved += [int]$clientEnv.id
      Write-Host "Auto-resolved Client environment ID: $($clientEnv.id)"
    }
  }

  # Add Java version ID if not explicitly provided
  if (-not [string]::IsNullOrWhiteSpace($cf.JavaVersionIdRaw)) {
    $javaId = $cf.JavaVersionIdRaw.Trim()
    if ($javaId -match '^\d+$') {
      $resolved += [int]$javaId
    }
  } else {
    # Auto-resolve Java 21
    $java21 = $all | Where-Object {
      ($_.name -like "*Java 21*" -or $_.name -like "*21*" -or $_.versionString -like "*21*") -and
      ($_.gameVersionTypeID -eq 68541 -or $_.type -eq 68541) # Java version type ID
    } | Select-Object -First 1
    
    if (-not $java21) {
      # Try alternative search patterns
      $java21 = $all | Where-Object {
        ($_.name -eq "Java 21" -or $_.slug -eq "java-21" -or $_.versionString -eq "21")
      } | Select-Object -First 1
    }
    
    if ($java21 -and $java21.id) {
      $resolved += [int]$java21.id
      Write-Host "Auto-resolved Java 21 version ID: $($java21.id)"
    } else {
      Write-Warning "Could not auto-resolve Java 21 version ID. Set CF_JAVA_VERSION_ID to specify it manually."
    }
  }

  $resolved = $resolved | Select-Object -Unique
  if ($resolved.Count -eq 0) {
    throw "Could not auto-resolve CurseForge game version IDs. Set CF_GAME_VERSION_IDS (comma-separated numeric IDs) and re-run."
  }

  return $resolved
}

function Pick-MainArtifact([array]$artifacts) {
  if ($artifacts.Count -eq 1) { return $artifacts[0] }

  # Prefer non-sources/non-javadoc already filtered; now prefer the 'plain' jar.
  # Heuristic: avoid "all" or "dev" jars if present.
  $preferred = $artifacts | Where-Object { $_.Name -notmatch '(-all|-dev|-shadow)' } | Select-Object -First 1
  if ($preferred) { return $preferred }
  return $artifacts | Select-Object -First 1
}

function Upload-ToCurseForge([string]$version, [array]$artifacts) {
  $cf = Get-CurseForgeConfig

  if ($script:SkipCurseForge) {
    Write-Host "CurseForge upload skipped (-SkipCurseForge)."
    return
  }

  if (-not $cf.ProjectId -or -not $cf.Token) {
    Write-Host "CurseForge upload not configured (missing CF_PROJECT_ID and/or CF_API_TOKEN). Skipping."
    return
  }

  # Validate token format
  if ([string]::IsNullOrWhiteSpace($cf.Token)) {
    throw "CF_API_TOKEN is empty or whitespace-only. Please check your .env file or environment variable."
  }
  
  # Check for problematic characters
  if ($cf.Token -match '[\r\n]') {
    $tokenPreview = if ($cf.Token.Length -gt 20) { $cf.Token.Substring(0, 20) + "..." } else { $cf.Token }
    $tokenLength = $cf.Token.Length
    throw "CF_API_TOKEN appears to contain newlines or carriage returns (length: $tokenLength, starts with: '$tokenPreview'). In your .env file, ensure the token is on a single line. If your token has special characters, wrap it in quotes: CF_API_TOKEN=`"your_token_here`""
  }
  
  # Check token length (CurseForge tokens are typically 32+ characters)
  if ($cf.Token.Length -lt 10) {
    throw "CF_API_TOKEN appears too short (length: $($cf.Token.Length)). Please verify your token is correct."
  }
  
  # Check if token looks like a bcrypt hash (common mistake - user might have copied wrong value)
  if ($cf.Token -match '^\$2[aby]\$') {
    throw "CF_API_TOKEN appears to be a bcrypt hash (starts with `$2a$, `$2b$, or `$2y$), not a CurseForge API token. Please get your actual API token from https://console.curseforge.com/ (API Tokens section)."
  }
  
  # CurseForge API tokens typically don't start with special characters like $
  if ($cf.Token.StartsWith('$')) {
    Write-Warning "CF_API_TOKEN starts with '$' which is unusual for CurseForge tokens. Please verify you're using the correct token from https://console.curseforge.com/"
  }
  
  # Debug: show token length and first few chars (for troubleshooting)
  $tokenPreview = if ($cf.Token.Length -gt 10) { $cf.Token.Substring(0, 10) + "..." } else { "***" }
  Write-Host "Using CurseForge API token (length: $($cf.Token.Length), starts with: $tokenPreview)"

  $projectId = $cf.ProjectId
  if ($projectId -notmatch '^\d+$') {
    throw "CurseForge project ID must be numeric. Got: '$projectId'"
  }

  $supportedName = Get-SupportedMinecraftVersionName
  $mcVersions = @(Expand-MinecraftVersionRange $supportedName)
  if ($mcVersions.Count -eq 0) {
    throw "Could not expand supported_minecraft_version_name for CurseForge game versions."
  }

  $releaseType = if ($cf.ReleaseType) { $cf.ReleaseType.Trim().ToLowerInvariant() } else { "release" }
  if ($releaseType -notin @("release", "beta", "alpha")) {
    throw "Invalid CF_RELEASE_TYPE '$releaseType'. Expected: release | beta | alpha"
  }

  $changelog = $null
  if ($cf.ChangelogFile -and (Test-Path $cf.ChangelogFile)) {
    $changelog = Get-Content -Path $cf.ChangelogFile -Raw
    Write-Host "Using changelog from CF_CHANGELOG_FILE: $($cf.ChangelogFile)"
  } else {
    $autoChangelogPath = "changelogs/$version.md"
    if (Test-Path $autoChangelogPath) {
      $changelog = Get-Content -Path $autoChangelogPath -Raw
      Write-Host "Using auto-detected changelog: $autoChangelogPath"
    } elseif ($script:Note) {
      $changelog = $script:Note
    } else {
      $changelog = "Release v$version"
    }
  }

  $modArtifacts = @($artifacts | Where-Object { $_.Loader -ne "common" })
  if ($modArtifacts.Count -eq 0) { $modArtifacts = @(Pick-MainArtifact $artifacts) }

  Add-Type -AssemblyName System.Net.Http
  $uploadUri = "$($cf.BaseUrl)/api/projects/$projectId/upload-file"

  foreach ($artifact in $modArtifacts) {
    $loaderKey = if ($artifact.Loader) { $artifact.Loader } else { "neoforge" }
    $loaderDisplay = switch ($loaderKey.ToLowerInvariant()) {
      "neoforge" { "NeoForge" }
      "fabric" { "Fabric" }
      "forge" { "Forge" }
      default { $loaderKey }
    }

    $gameVersionIds = Resolve-CurseForgeGameVersionIds $cf $mcVersions $loaderDisplay
    $filePath = $artifact.FullName
    $fileName = $artifact.Name

    $metadata = @{
      changelog = [string]$changelog
      changelogType = "markdown"
      displayName = $fileName
      gameVersions = @($gameVersionIds | Select-Object -Unique)
      releaseType = $releaseType
    }
    $metadataJson = $metadata | ConvertTo-Json -Compress -Depth 10

    Write-Host "Uploading $fileName to CurseForge (Loader: $loaderKey)..."
    Write-Host "  Game versions: $($mcVersions -join ', ') | IDs: $((@($gameVersionIds | Select-Object -Unique)) -join ',')"

    $client = New-Object System.Net.Http.HttpClient
    $client.Timeout = [System.TimeSpan]::FromSeconds(120)
    $client.DefaultRequestHeaders.Add("X-Api-Token", $cf.Token)
    $multipart = New-Object System.Net.Http.MultipartFormDataContent
    $metaContent = New-Object System.Net.Http.StringContent($metadataJson, [System.Text.Encoding]::UTF8, "application/json")
    $multipart.Add($metaContent, "metadata")
    $fileStream = [System.IO.File]::OpenRead($filePath)
    try {
      $fileContent = New-Object System.Net.Http.StreamContent($fileStream)
      $fileContent.Headers.ContentType = New-Object System.Net.Http.Headers.MediaTypeHeaderValue("application/java-archive")
      $multipart.Add($fileContent, "file", $fileName)
      $resp = $client.PostAsync($uploadUri, $multipart).Result
      $respBody = $resp.Content.ReadAsStringAsync().Result
      if (-not $resp.IsSuccessStatusCode) {
        throw "CurseForge upload failed for ${fileName}: $([int]$resp.StatusCode) $($resp.ReasonPhrase)`n$respBody"
      }
      Write-Host "  [OK] Uploaded $fileName to CurseForge."
    } finally {
      $fileStream.Close()
      $multipart.Dispose()
      $client.Dispose()
    }
  }
}

function Get-ModrinthConfig() {
  $projectId = Get-EnvAny @("MODRINTH_PROJECT_ID", "MR_PROJECT_ID", "MODRINTH_PROJECT")
  $token = Get-EnvAny @("MODRINTH_TOKEN", "MR_TOKEN", "MODRINTH_API_TOKEN")
  $baseUrl = "https://api.modrinth.com"

  # Clean up token: trim whitespace and remove any control characters
  if ($token) {
    $token = $token.Trim()
    $token = $token -replace "[\r\n\t]", ""
    if (($token.StartsWith('"') -and $token.EndsWith('"')) -or ($token.StartsWith("'") -and $token.EndsWith("'"))) {
      $token = $token.Substring(1, $token.Length - 2)
    }
  }

  return @{
    ProjectId = $projectId
    Token = $token
    BaseUrl = $baseUrl
    ReleaseType = (Get-EnvAny @("MODRINTH_RELEASE_TYPE"))
    GameVersionsRaw = (Get-EnvAny @("MODRINTH_GAME_VERSIONS"))
    LoadersRaw = (Get-EnvAny @("MODRINTH_LOADERS"))
    ChangelogFile = (Get-EnvAny @("MODRINTH_CHANGELOG_FILE"))
  }
}

function Resolve-ModrinthGameVersions([hashtable]$mr) {
  # Prefer explicit versions if user supplied them.
  if (-not [string]::IsNullOrWhiteSpace($mr.GameVersionsRaw)) {
    $versions = @()
    foreach ($part in ($mr.GameVersionsRaw -split "[,;\s]+" | Where-Object { $_ })) {
      $t = $part.Trim()
      if ($t) { $versions += $t }
    }
    if ($versions.Count -eq 0) { throw "MODRINTH_GAME_VERSIONS was provided but no versions could be parsed." }
    return $versions
  }

  # Standard: expand supported_minecraft_version_name (matches GitHub mc-publish / jar +mc suffix)
  $supportedName = Get-SupportedMinecraftVersionName
  $expanded = Expand-MinecraftVersionRange $supportedName
  if ($expanded.Count -eq 0) {
    throw "Could not expand supported_minecraft_version_name '$supportedName' into Modrinth game_versions."
  }
  return @($expanded)
}

function Resolve-ModrinthLoaders([hashtable]$mr, [array]$foundLoaders) {
  # Prefer explicit loaders if user supplied them.
  if (-not [string]::IsNullOrWhiteSpace($mr.LoadersRaw)) {
    $loaders = @()
    foreach ($part in ($mr.LoadersRaw -split "[,;\s]+" | Where-Object { $_ })) {
      $t = $part.Trim().ToLowerInvariant()
      if ($t) { $loaders += $t }
    }
    if ($loaders.Count -eq 0) { throw "MODRINTH_LOADERS was provided but no loaders could be parsed." }
    return $loaders
  }

  # Prefer loaders actually present in filtered artifacts
  $detected = @($foundLoaders | Where-Object { $_ -and $_ -ne "common" -and $_ -ne "unknown" } | Select-Object -Unique)
  if ($detected.Count -gt 0) { return $detected }

  # Fallback: enabled_platforms from gradle.properties
  $platforms = @(Get-EnabledPlatforms | Where-Object { $_ -ne "common" })
  if ($platforms.Count -gt 0) { return $platforms }

  return @("neoforge")
}

function Upload-ToModrinth([string]$version, [array]$artifacts) {
  $mr = Get-ModrinthConfig

  if ($script:SkipModrinth) {
    Write-Host "Modrinth upload skipped (-SkipModrinth)."
    return
  }

  if (-not $mr.ProjectId -or -not $mr.Token) {
    Write-Host "Modrinth upload not configured (missing MODRINTH_PROJECT_ID and/or MODRINTH_TOKEN). Skipping."
    return
  }

  if ([string]::IsNullOrWhiteSpace($mr.Token)) {
    throw "MODRINTH_TOKEN is empty or whitespace-only. Please check your .env file or environment variable."
  }

  if ($mr.Token -match '[\r\n]') {
    throw "MODRINTH_TOKEN appears to contain newlines or carriage returns. Ensure the token is on a single line in .env."
  }

  if ($mr.Token.Length -lt 10) {
    throw "MODRINTH_TOKEN appears too short (length: $($mr.Token.Length)). Please verify your token is correct."
  }

  $supportedName = Get-SupportedMinecraftVersionName
  # Standard version identity (matches .github/workflows/publish-modrinth.yml):
  #   version_number = 3.0.6+mc1.20.2-1.20.4
  #   name           = 3.0.6 (1.20.2-1.20.4)
  $versionNumber = "{0}+mc{1}" -f $version, $supportedName
  $versionName = "{0} ({1})" -f $version, $supportedName

  $gameVersions = Resolve-ModrinthGameVersions $mr
  $modArtifacts = @($artifacts | Where-Object { $_.Loader -ne "common" })
  if ($modArtifacts.Count -eq 0) {
    # Artifacts without Loader property (root build/libs fallback)
    $modArtifacts = @($artifacts)
  }
  if ($modArtifacts.Count -eq 0) {
    Write-Warning "No mod artifacts found for Modrinth upload."
    return
  }

  $loaders = Resolve-ModrinthLoaders $mr @($modArtifacts | ForEach-Object { $_.Loader })
  $releaseType = if ($mr.ReleaseType) { $mr.ReleaseType.Trim().ToLowerInvariant() } else { "release" }
  if ($releaseType -notin @("release", "beta", "alpha")) {
    throw "Invalid MODRINTH_RELEASE_TYPE '$releaseType'. Expected: release | beta | alpha"
  }

  $changelog = $null
  if ($mr.ChangelogFile -and (Test-Path $mr.ChangelogFile)) {
    $changelog = Get-Content -Path $mr.ChangelogFile -Raw
    Write-Host "Using changelog from MODRINTH_CHANGELOG_FILE: $($mr.ChangelogFile)"
  } else {
    $autoChangelogPath = "changelogs/$version.md"
    if (Test-Path $autoChangelogPath) {
      $changelog = Get-Content -Path $autoChangelogPath -Raw
      Write-Host "Using auto-detected changelog: $autoChangelogPath"
    } elseif ($script:Note) {
      $changelog = $script:Note
    } else {
      $changelog = "Release v$version"
    }
  }

  $gameVersionsArray = [string[]]@($gameVersions)
  $loadersArray = [string[]]@($loaders)
  $filePartsArray = [string[]]@($modArtifacts | ForEach-Object { $_.Name })

  $metadata = @{
    name = $versionName
    version_number = $versionNumber
    changelog = [string]$changelog
    dependencies = @()
    game_versions = $gameVersionsArray
    loaders = $loadersArray
    release_channel = $releaseType
    featured = $false
    project_id = $mr.ProjectId
    file_parts = $filePartsArray
  }

  $metadataJson = $metadata | ConvertTo-Json -Compress -Depth 10
  $metadataJson = $metadataJson -replace '("game_versions"\s*:\s*)"([^"]+)"', '$1["$2"]'
  $metadataJson = $metadataJson -replace '("loaders"\s*:\s*)"([^"]+)"', '$1["$2"]'
  $metadataJson = $metadataJson -replace '("file_parts"\s*:\s*)"([^"]+)"', '$1["$2"]'

  $uploadUri = "$($mr.BaseUrl)/v2/version"
  Write-Host "Uploading unified Modrinth version..."
  Write-Host "  Name: $versionName"
  Write-Host "  Version number: $versionNumber"
  Write-Host "  Game versions: $($gameVersionsArray -join ', ')"
  Write-Host "  Loaders: $($loadersArray -join ', ')"
  Write-Host "  Files: $($filePartsArray -join ', ')"
  Write-Host "  Release type: $releaseType"

  Add-Type -AssemblyName System.Net.Http
  $client = New-Object System.Net.Http.HttpClient
  $client.Timeout = [System.TimeSpan]::FromSeconds(120)
  $client.DefaultRequestHeaders.Add("Authorization", $mr.Token)

  $multipart = New-Object System.Net.Http.MultipartFormDataContent
  $metaContent = New-Object System.Net.Http.StringContent($metadataJson, [System.Text.Encoding]::UTF8, "application/json")
  $multipart.Add($metaContent, "data")

  $streams = @()
  try {
    foreach ($artifact in $modArtifacts) {
      $fileStream = [System.IO.File]::OpenRead($artifact.FullName)
      $streams += $fileStream
      $fileContent = New-Object System.Net.Http.StreamContent($fileStream)
      $fileContent.Headers.ContentType = New-Object System.Net.Http.Headers.MediaTypeHeaderValue("application/java-archive")
      # Field name must match file_parts entry
      $multipart.Add($fileContent, $artifact.Name, $artifact.Name)
    }

    $resp = $client.PostAsync($uploadUri, $multipart).Result
    $respBody = $resp.Content.ReadAsStringAsync().Result
    if (-not $resp.IsSuccessStatusCode) {
      throw "Modrinth upload failed: $([int]$resp.StatusCode) $($resp.ReasonPhrase)`n$respBody"
    }

    Write-Host "  [OK] Uploaded unified version to Modrinth."
    if ($respBody) {
      try {
        $responseObj = $respBody | ConvertFrom-Json
        if ($responseObj.id) {
          Write-Host "  Version ID: $($responseObj.id)"
          Write-Host "  Version URL: https://modrinth.com/mod/$($mr.ProjectId)/version/$($responseObj.version_number)"
        }
      } catch {
        Write-Host "  Response: $respBody"
      }
    }
  } finally {
    foreach ($s in $streams) { $s.Close() }
    $multipart.Dispose()
    $client.Dispose()
  }
}

function Upload-ToGitHubRelease([string]$version, [array]$artifacts) {
  # Load token from .env if not provided
  if (-not $script:GitHubToken) {
    Import-DotEnvIfPresent ".env"
    if (-not $script:GitHubToken) { $script:GitHubToken = $env:GITHUB_TOKEN }
  }
  
  if (-not $script:GitHubToken) {
    throw "Missing GitHubToken. Pass -GitHubToken or set `$env:GITHUB_TOKEN (or put it in .env)."
  }
  
  # Auto-detect GitHub repo from git remote if not provided
  if (-not $script:GitHubRepo) {
    $remote = git remote get-url origin
    Write-Host "Detecting GitHub repository from remote: $remote"
    
    # Match various GitHub URL formats:
    # HTTPS: https://github.com/username/repo.git
    # SSH: git@github.com:username/repo.git
    # SSH with protocol: ssh://git@github.com/username/repo.git
    $detectedRepo = $null
    
    # Try SSH with protocol: ssh://git@host/path
    if ($remote -match '^ssh://[^@]+@([^/:]+)(?::\d+)?/(.+?)(?:\.git)?/?$') {
      $remoteHost = $Matches[1]
      $path = $Matches[2]
      if ($remoteHost -eq "github.com") {
        $detectedRepo = $path -replace '\.git/?$', ''
      }
    }
    # Try HTTPS: https://host/path
    elseif ($remote -match '^https?://([^/:]+)(?::\d+)?/(.+?)(?:\.git)?/?$') {
      $remoteHost = $Matches[1]
      $path = $Matches[2]
      if ($remoteHost -eq "github.com" -or $remoteHost -match '^github\.') {
        $detectedRepo = $path -replace '\.git/?$', ''
      }
    }
    # Try SSH without protocol: git@host:path
    elseif ($remote -match '^[^@]+@([^/:]+)(?::\d+)?[:/](.+?)(?:\.git)?/?$') {
      $remoteHost = $Matches[1]
      $path = $Matches[2]
      if ($remoteHost -eq "github.com" -or $remoteHost -match '^github\.') {
        $detectedRepo = $path -replace '\.git/?$', ''
      }
    }
    
    if ($detectedRepo) {
      $script:GitHubRepo = $detectedRepo
      Write-Host "Detected GitHub repository: $script:GitHubRepo"
    }
  }
  
  if (-not $script:GitHubRepo) {
    throw "Could not detect GitHub repository from git remote. Set -GitHubRepo or ensure origin remote points to GitHub."
  }
  
  $tag = "v$version"
  $releaseName = "Release $tag"
  $releaseDescription = if ($script:Note) { 
    $script:Note 
  } else { 
    "Release $tag"
  }
  
  # GitHub API base URL
  $apiBase = "https://api.github.com"
  $repoPath = $script:GitHubRepo -replace '/', '/'
  $releaseUrl = "$apiBase/repos/$repoPath/releases"
  
  $headers = @{
    "Authorization" = "token $script:GitHubToken"
    "Accept" = "application/vnd.github.v3+json"
  }
  
  # Check if release already exists
  Write-Host "Checking if release $tag already exists..."
  try {
    $existingRelease = Invoke-RestMethod -Uri "$releaseUrl/tags/$tag" -Headers $headers -ErrorAction Stop
    Write-Host "Release already exists, updating..."
    
    $releaseBody = @{
      name = $releaseName
      body = $releaseDescription
      draft = $false
      prerelease = $false
    } | ConvertTo-Json -Compress -Depth 10
    
    $releaseResponse = Invoke-RestMethod -Uri "$releaseUrl/$($existingRelease.id)" -Method Patch -Headers $headers -Body $releaseBody -ErrorAction Stop
    Write-Host "Updated existing release: $tag"
    $releaseId = $existingRelease.id
    # Store release response for potential upload_url usage
    $currentRelease = $releaseResponse
  } catch {
    # Release doesn't exist, create it
    Write-Host "Creating new release: $tag"
    
    $releaseBody = @{
      tag_name = $tag
      name = $releaseName
      body = $releaseDescription
      draft = $false
      prerelease = $false
    } | ConvertTo-Json -Compress -Depth 10
    
    try {
      $releaseResponse = Invoke-RestMethod -Uri $releaseUrl -Method Post -Headers $headers -Body $releaseBody -ErrorAction Stop
      Write-Host "Created GitHub release: $tag"
      $releaseId = $releaseResponse.id
    } catch {
      $statusCode = $_.Exception.Response.StatusCode.value__
      $errorContent = $_.ErrorDetails.Message
      throw "Failed to create GitHub release: HTTP $statusCode - $errorContent"
    }
  }
  
  # Upload artifacts
  # GitHub requires using uploads.github.com for asset uploads, not api.github.com
  # Construct upload URL manually for reliability
  Write-Host "Debug: repoPath = '$repoPath'"
  Write-Host "Debug: releaseId = '$releaseId'"
  
  if ([string]::IsNullOrWhiteSpace($repoPath)) {
    throw "repoPath is empty or null. Cannot construct upload URL."
  }
  if ([string]::IsNullOrWhiteSpace($releaseId)) {
    throw "releaseId is empty or null. Cannot construct upload URL."
  }
  
  $uploadsBase = "https://uploads.github.com"
  $uploadUrlBase = "$uploadsBase/repos/$repoPath/releases/$releaseId/assets"
  
  Write-Host "Debug: uploadsBase = '$uploadsBase'"
  Write-Host "Debug: uploadUrlBase = '$uploadUrlBase'"
  
  if ([string]::IsNullOrWhiteSpace($uploadUrlBase)) {
    throw "uploadUrlBase is empty or null. Cannot upload artifacts."
  }
  
  Write-Host "Uploading $($artifacts.Count) artifact(s) to GitHub release..."
  Write-Host "Upload base URL: $uploadUrlBase"

  # Refresh asset list so retries can replace same-named files.
  $existingAssets = @()
  try {
    $existingAssets = @(Invoke-RestMethod -Uri "$releaseUrl/$releaseId/assets?per_page=100" -Headers $headers -ErrorAction Stop)
  } catch {
    Write-Warning "Could not list existing release assets: $_"
  }
  
  foreach ($artifact in $artifacts) {
    $fileName = $artifact.Name
    $filePath = $artifact.FullName
    $fileSize = (Get-Item $filePath).Length
    
    Write-Host "Uploading $fileName ($([math]::Round($fileSize / 1MB, 2)) MB)..."

    $prior = $existingAssets | Where-Object { $_.name -eq $fileName } | Select-Object -First 1
    if ($prior) {
      Write-Host "  Replacing existing asset id $($prior.id)..."
      try {
        Invoke-RestMethod -Uri "$apiBase/repos/$repoPath/releases/assets/$($prior.id)" -Headers $headers -Method Delete -ErrorAction Stop | Out-Null
      } catch {
        Write-Warning "  Failed to delete existing asset '$fileName': $_"
      }
    }
    
    $encodedFileName = [Uri]::EscapeDataString($fileName)
    $uploadUrlWithName = $uploadUrlBase + "?name=" + $encodedFileName
    
    try {
      $uri = [Uri]::new($uploadUrlWithName)
      if (-not $uri.IsAbsoluteUri) {
        throw "Upload URL is not absolute: $uploadUrlWithName"
      }
    } catch {
      Write-Warning "Invalid upload URL: $uploadUrlWithName - $_"
      throw
    }
      
    $uploadHeaders = @{
      "Authorization" = "token $script:GitHubToken"
      "Accept" = "application/vnd.github.v3+json"
      "Content-Type" = "application/java-archive"
    }
      
    try {
      # Use HttpClient for reliable file uploads
      Add-Type -AssemblyName System.Net.Http
      
      $httpClient = New-Object System.Net.Http.HttpClient
      $httpClient.Timeout = [System.TimeSpan]::FromSeconds(120)
      $httpClient.DefaultRequestHeaders.Add("Authorization", "token $script:GitHubToken")
      $httpClient.DefaultRequestHeaders.Add("Accept", "application/vnd.github.v3+json")
      
      $fileStream = [System.IO.File]::OpenRead($filePath)
      $streamContent = New-Object System.Net.Http.StreamContent($fileStream)
      $streamContent.Headers.ContentType = New-Object System.Net.Http.Headers.MediaTypeHeaderValue("application/java-archive")
      
      $response = $httpClient.PostAsync($uri, $streamContent).Result
      
      $fileStream.Close()
      $httpClient.Dispose()
      
      if ($response.IsSuccessStatusCode) {
        Write-Host "  [OK] Uploaded $fileName"
      } else {
        $errorContent = $response.Content.ReadAsStringAsync().Result
        throw "Upload failed: $($response.StatusCode) - $errorContent"
      }
    } catch {
      # Fallback to Invoke-WebRequest if HttpClient fails
      try {
        $fileBytes = [System.IO.File]::ReadAllBytes($filePath)
        $response = Invoke-WebRequest -Uri $uri -Method Post -Headers $uploadHeaders -Body $fileBytes -ContentType "application/java-archive"
        
        if ($response.StatusCode -eq 201) {
          Write-Host "  [OK] Uploaded ${fileName}"
        } else {
          Write-Warning "  [FAIL] Failed to upload ${fileName}: HTTP $($response.StatusCode)"
        }
      } catch {
        Write-Warning "  [FAIL] Failed to upload ${fileName}: $_"
      }
    }
  }
  
  Write-Host "GitHub release: https://github.com/$script:GitHubRepo/releases/tag/$tag"
}

# Main execution
Require-Command git

# Load .env if present
Import-DotEnvIfPresent ".env"

# Validate mutually exclusive flags
if ($OnlyGit -and $OnlyCurseForge) {
  throw "Cannot use both -OnlyGit and -OnlyCurseForge. Choose one."
}
if ($OnlyGit -and $OnlyModrinth) {
  throw "Cannot use both -OnlyGit and -OnlyModrinth. Choose one."
}
if ($OnlyCurseForge -and $OnlyModrinth) {
  throw "Cannot use both -OnlyCurseForge and -OnlyModrinth. Choose one."
}
if ($OnlyGit -and $OnlyPublish) {
  throw "Cannot use both -OnlyGit and -OnlyPublish. Choose one."
}
if ($OnlyCurseForge -and $OnlyPublish) {
  throw "Cannot use both -OnlyCurseForge and -OnlyPublish. Choose one."
}
if ($OnlyModrinth -and $OnlyPublish) {
  throw "Cannot use both -OnlyModrinth and -OnlyPublish. Choose one."
}

# Read current version
$currentVersion = Read-GradleVersion "gradle.properties"
Write-Host "Current version: $currentVersion"

# Handle -OnlyCurseForge: skip git operations, use existing JARs
if ($OnlyCurseForge) {
  Write-Host "OnlyCurseForge mode: skipping git operations and build, using existing JARs"
  $newVersion = $currentVersion
  
  # Find existing build artifacts
  $artifacts = Find-BuildArtifacts
  Write-Host "Found $($artifacts.Count) artifact(s):"
  foreach ($artifact in $artifacts) {
    Write-Host "  - $($artifact.Name)"
  }
  
  # Upload to CurseForge only
  Upload-ToCurseForge $newVersion $artifacts
  
  Write-Host ""
  Write-Host "CurseForge upload complete!"
  exit 0
}

# Handle -OnlyModrinth: skip git operations, use existing JARs
if ($OnlyModrinth) {
  Write-Host "OnlyModrinth mode: skipping git operations and build, using existing JARs"
  $newVersion = $currentVersion
  
  # Find existing build artifacts
  $artifacts = Find-BuildArtifacts
  Write-Host "Found $($artifacts.Count) artifact(s):"
  foreach ($artifact in $artifacts) {
    Write-Host "  - $($artifact.Name)"
  }
  
  # Upload to Modrinth only
  Upload-ToModrinth $newVersion $artifacts
  
  Write-Host ""
  Write-Host "Modrinth upload complete!"
  exit 0
}

# Handle -OnlyGit: skip build and uploads, just do git operations
if ($OnlyGit) {
  Write-Host "OnlyGit mode: skipping build and uploads, only performing git operations"
  
  if (-not $OnlyPublish) {
    Assert-GitClean
  }
  
  if ($Version) {
    Parse-SemVer $Version | Out-Null
    $newVersion = $Version
    Write-Host "Updating gradle.properties to version: $newVersion"
    Update-GradleVersion "gradle.properties" $newVersion
    
    # Commit and push the version change
    $tag = Git-CommitTagPush $newVersion
    Write-Host "Created and pushed tag: $tag"
  } else {
    $newVersion = Bump-SemVer $currentVersion $Bump
    Write-Host "Bumping version: $currentVersion -> $newVersion (bump: $Bump)"
    Update-GradleVersion "gradle.properties" $newVersion
    
    # After version edit we must commit+tag
    $tag = Git-CommitTagPush $newVersion
    Write-Host "Created and pushed tag: $tag"
  }
  
  Write-Host ""
  Write-Host "Git operations complete!"
  exit 0
}

# Normal flow or -OnlyPublish
if (-not $OnlyPublish) {
  Assert-GitClean
}

if ($OnlyPublish) {
  if ($Version) {
    Parse-SemVer $Version | Out-Null
    $newVersion = $Version
    # Update gradle.properties with the specified version so the build uses it
    Write-Host "Updating gradle.properties to version: $newVersion"
    Update-GradleVersion "gradle.properties" $newVersion
    
    # Commit and push the version change
    $tag = Git-CommitTagPush $newVersion
    Write-Host "Created and pushed tag: $tag"
  } else {
    $newVersion = $currentVersion
  }
  Write-Host "OnlyPublish enabled. Publishing version: $newVersion"
} else {
  if ($Version) {
    throw "Do not pass -Version unless using -OnlyPublish. Normal publish flow bumps versions automatically."
  }

  $newVersion = Bump-SemVer $currentVersion $Bump
  Write-Host "Bumping version: $currentVersion -> $newVersion (bump: $Bump)"

  Update-GradleVersion "gradle.properties" $newVersion

  # After version edit we must commit+tag
  $tag = Git-CommitTagPush $newVersion
  Write-Host "Created and pushed tag: $tag"
}

# Build the mod (unless SkipBuild is set)
if (-not $SkipBuild) {
  Build-Mod
} else {
  Write-Host "SkipBuild enabled. Using existing JARs from build/libs"
}

# Find build artifacts
$artifacts = Find-BuildArtifacts
Write-Host "Found $($artifacts.Count) artifact(s):"
foreach ($artifact in $artifacts) {
  Write-Host "  - $($artifact.Name)"
}

# Upload to GitHub
Upload-ToGitHubRelease $newVersion $artifacts

# Upload to CurseForge (optional, env-driven)
Upload-ToCurseForge $newVersion $artifacts

# Upload to Modrinth (optional, env-driven)
Upload-ToModrinth $newVersion $artifacts

Write-Host ""
Write-Host "Publish complete!"
if ($GitHubRepo) {
  Write-Host "Release: https://github.com/$GitHubRepo/releases/tag/v$newVersion"
}
