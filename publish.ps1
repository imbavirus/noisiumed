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
    - MODRINTH_GAME_VERSIONS: comma-separated Minecraft versions (e.g., "1.21.1")
        - If omitted, the script attempts to auto-detect from minecraft_version in gradle.properties.
    - MODRINTH_LOADERS: comma-separated mod loaders (e.g., "neoforge", "forge", "fabric")
        - If omitted, the script attempts to auto-detect "neoforge" based on the project.
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
  
  & $gradleCmd clean build
  if ($LASTEXITCODE -ne 0) {
    throw "Gradle build failed"
  }
  
  Write-Host "Build completed successfully"
}

function Find-BuildArtifacts() {
  # Find JAR files in build/libs directory
  $libsDir = "build/libs"
  if (-not (Test-Path $libsDir)) {
    throw "Build directory not found: $libsDir (build may have failed)"
  }
  
  # Find all JAR files, but exclude sources and javadoc JARs
  $artifacts = Get-ChildItem -Path $libsDir -Filter "*.jar" | Where-Object {
    $name = $_.Name
    -not $name.Contains("-sources") -and -not $name.Contains("-javadoc")
  }
  
  if ($artifacts.Count -eq 0) {
    throw "No JAR artifacts found in $libsDir"
  }
  
  return $artifacts
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

function Resolve-CurseForgeGameVersionIds([hashtable]$cf, [string]$minecraftVersion) {
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

  $mc = $all | Where-Object {
    ($_.name -eq $minecraftVersion) -or ($_.versionString -eq $minecraftVersion) -or ($_.slug -eq $minecraftVersion)
  } | Select-Object -First 1

  if ($mc -and $mc.id) { $resolved += [int]$mc.id }

  # If this is a loader-specific build, also try to include the loader ID.
  $loaderName = $cf.ModLoaderName
  if (-not $loaderName) {
    # Heuristic: this repo is NeoForge-based.
    $loaderName = "NeoForge"
  }

  $loader = $all | Where-Object {
    ($_.name -eq $loaderName) -or ($_.slug -eq $loaderName) -or ($_.name -like "*$loaderName*")
  } | Select-Object -First 1

  if ($loader -and $loader.id) { $resolved += [int]$loader.id }

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
    # Auto-resolve server and client environment IDs
    # Server-side environment type ID is 1, Client is 2 (from CurseForge API docs)
    # Search for environment versions with these type IDs
    $serverEnv = $all | Where-Object {
      $_.gameVersionTypeID -eq 1 -and ($_.name -like "*server*" -or $_.slug -like "*server*" -or $_.id -eq 1)
    } | Select-Object -First 1
    $clientEnv = $all | Where-Object {
      $_.gameVersionTypeID -eq 1 -and ($_.name -like "*client*" -or $_.slug -like "*client*" -or $_.id -eq 2)
    } | Select-Object -First 1
    
    # If not found by name, try by ID (common: Server = 1, Client = 2)
    if (-not $serverEnv) {
      $serverEnv = $all | Where-Object { $_.id -eq 1 -and $_.gameVersionTypeID -eq 1 } | Select-Object -First 1
    }
    if (-not $clientEnv) {
      $clientEnv = $all | Where-Object { $_.id -eq 2 -and $_.gameVersionTypeID -eq 1 } | Select-Object -First 1
    }
    
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

  # We want minecraft_version from gradle.properties (not mod_version).
  $gp = Get-Content -Path "gradle.properties"
  $mcLine = $gp | Where-Object { $_ -match '^minecraft_version=(.+)$' } | Select-Object -First 1
  $mcVersion = if ($mcLine -and ($mcLine -match '^minecraft_version=(.+)$')) { $Matches[1].Trim() } else { $null }

  if (-not $mcVersion) {
    throw "Could not read minecraft_version from gradle.properties (required for CurseForge auto-resolution)."
  }

  $gameVersionIds = Resolve-CurseForgeGameVersionIds $cf $mcVersion

  $releaseType = if ($cf.ReleaseType) { $cf.ReleaseType.Trim().ToLowerInvariant() } else { "release" }
  if ($releaseType -notin @("release", "beta", "alpha")) {
    throw "Invalid CF_RELEASE_TYPE '$releaseType'. Expected: release | beta | alpha"
  }

  $artifact = Pick-MainArtifact $artifacts
  $filePath = $artifact.FullName
  $fileName = $artifact.Name

  $changelog = $null
  # Priority: 1) Explicit CF_CHANGELOG_FILE, 2) Auto-detect changelogs/{version}.md, 3) Note parameter, 4) Default
  if ($cf.ChangelogFile -and (Test-Path $cf.ChangelogFile)) {
    $changelog = Get-Content -Path $cf.ChangelogFile -Raw
    Write-Host "Using changelog from CF_CHANGELOG_FILE: $($cf.ChangelogFile)"
  } else {
    # Auto-detect changelog from changelogs/{version}.md
    $autoChangelogPath = "changelogs/$version.md"
    if (Test-Path $autoChangelogPath) {
      $changelog = Get-Content -Path $autoChangelogPath -Raw
      Write-Host "Using auto-detected changelog: $autoChangelogPath"
    } elseif ($script:Note) {
      $changelog = $script:Note
      Write-Host "Using changelog from -Note parameter"
    } else {
      $changelog = "Release v$version"
      Write-Host "Using default changelog message"
    }
  }

  # Build metadata per CurseForge Upload API.
  $metadata = @{
    changelog = [string]$changelog
    changelogType = "markdown"
    displayName = $fileName
    gameVersions = $gameVersionIds
    releaseType = $releaseType
  }

  $metadataJson = $metadata | ConvertTo-Json -Compress -Depth 10

  $uploadUri = "$($cf.BaseUrl)/api/projects/$projectId/upload-file"
  Write-Host "Uploading to CurseForge project $projectId..."
  Write-Host "  URL: $uploadUri"
  Write-Host "  File: $fileName"
  Write-Host "  GameVersion IDs: $($gameVersionIds -join ',')"
  Write-Host "  ReleaseType: $releaseType"

  Add-Type -AssemblyName System.Net.Http
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
      throw "CurseForge upload failed: $([int]$resp.StatusCode) $($resp.ReasonPhrase)
$respBody"
    }

    Write-Host "  [OK] Uploaded to CurseForge."
    if ($respBody) {
      Write-Host "  Response: $respBody"
    }
  }   finally {
    $fileStream.Close()
    $multipart.Dispose()
    $client.Dispose()
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

function Resolve-ModrinthGameVersions([hashtable]$mr, [string]$minecraftVersion) {
  # Prefer explicit versions if user supplied them.
  if (-not [string]::IsNullOrWhiteSpace($mr.GameVersionsRaw)) {
    $versions = @()
    foreach ($part in ($mr.GameVersionsRaw -split "[,;\s]+" | Where-Object { $_ })) {
      $t = $part.Trim()
      if ($t) {
        $versions += $t
      }
    }
    if ($versions.Count -eq 0) { throw "MODRINTH_GAME_VERSIONS was provided but no versions could be parsed." }
    return $versions
  }

  # Auto-detect from gradle.properties - ensure it's always an array
  return @($minecraftVersion)
}

function Resolve-ModrinthLoaders([hashtable]$mr) {
  # Prefer explicit loaders if user supplied them.
  if (-not [string]::IsNullOrWhiteSpace($mr.LoadersRaw)) {
    $loaders = @()
    foreach ($part in ($mr.LoadersRaw -split "[,;\s]+" | Where-Object { $_ })) {
      $t = $part.Trim().ToLowerInvariant()
      if ($t) {
        $loaders += $t
      }
    }
    if ($loaders.Count -eq 0) { throw "MODRINTH_LOADERS was provided but no loaders could be parsed." }
    return $loaders
  }

  # Auto-detect: this project uses NeoForge
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

  # Validate token format
  if ([string]::IsNullOrWhiteSpace($mr.Token)) {
    throw "MODRINTH_TOKEN is empty or whitespace-only. Please check your .env file or environment variable."
  }

  # Clean token
  if ($mr.Token -match '[\r\n]') {
    $tokenPreview = if ($mr.Token.Length -gt 20) { $mr.Token.Substring(0, 20) + "..." } else { $mr.Token }
    $tokenLength = $mr.Token.Length
    throw "MODRINTH_TOKEN appears to contain newlines or carriage returns (length: $tokenLength, starts with: '$tokenPreview'). In your .env file, ensure the token is on a single line."
  }

  if ($mr.Token.Length -lt 10) {
    throw "MODRINTH_TOKEN appears too short (length: $($mr.Token.Length)). Please verify your token is correct."
  }

  $tokenPreview = if ($mr.Token.Length -gt 10) { $mr.Token.Substring(0, 10) + "..." } else { "***" }
  Write-Host "Using Modrinth API token (length: $($mr.Token.Length), starts with: $tokenPreview)"

  # Get Minecraft version from gradle.properties
  $gp = Get-Content -Path "gradle.properties"
  $mcLine = $gp | Where-Object { $_ -match '^minecraft_version=(.+)$' } | Select-Object -First 1
  $mcVersion = if ($mcLine -and ($mcLine -match '^minecraft_version=(.+)$')) { $Matches[1].Trim() } else { $null }

  if (-not $mcVersion) {
    throw "Could not read minecraft_version from gradle.properties (required for Modrinth auto-resolution)."
  }

  $gameVersions = Resolve-ModrinthGameVersions $mr $mcVersion
  $loaders = Resolve-ModrinthLoaders $mr

  $releaseType = if ($mr.ReleaseType) { $mr.ReleaseType.Trim().ToLowerInvariant() } else { "release" }
  if ($releaseType -notin @("release", "beta", "alpha")) {
    throw "Invalid MODRINTH_RELEASE_TYPE '$releaseType'. Expected: release | beta | alpha"
  }

  $artifact = Pick-MainArtifact $artifacts
  $filePath = $artifact.FullName
  $fileName = $artifact.Name

  $changelog = $null
  # Priority: 1) Explicit MODRINTH_CHANGELOG_FILE, 2) Auto-detect changelogs/{version}.md, 3) Note parameter, 4) Default
  if ($mr.ChangelogFile -and (Test-Path $mr.ChangelogFile)) {
    $changelog = Get-Content -Path $mr.ChangelogFile -Raw
    Write-Host "Using changelog from MODRINTH_CHANGELOG_FILE: $($mr.ChangelogFile)"
  } else {
    # Auto-detect changelog from changelogs/{version}.md
    $autoChangelogPath = "changelogs/$version.md"
    if (Test-Path $autoChangelogPath) {
      $changelog = Get-Content -Path $autoChangelogPath -Raw
      Write-Host "Using auto-detected changelog: $autoChangelogPath"
    } elseif ($script:Note) {
      $changelog = $script:Note
      Write-Host "Using changelog from -Note parameter"
    } else {
      $changelog = "Release v$version"
      Write-Host "Using default changelog message"
    }
  }

  # Build metadata per Modrinth API v2
  # Note: project_id is included in the metadata, not the URL
  # Ensure game_versions and loaders are always arrays (not single strings)
  # PowerShell's ConvertTo-Json can serialize single-element arrays as strings, so we need to force array type
  $gameVersionsArray = [string[]]@(if ($gameVersions -is [array]) { $gameVersions } else { $gameVersions })
  $loadersArray = [string[]]@(if ($loaders -is [array]) { $loaders } else { $loaders })
  
  $metadata = @{
    name = "v$version"
    version_number = $version
    changelog = [string]$changelog
    dependencies = @()
    game_versions = $gameVersionsArray
    loaders = $loadersArray
    release_channel = $releaseType
    featured = $false
    project_id = $mr.ProjectId
    file_parts = @($fileName)
  }

  # Convert to JSON, ensuring arrays are always arrays
  # PowerShell's ConvertTo-Json can serialize single-element arrays as strings
  # Fix by replacing string values with array syntax in JSON
  $metadataJson = $metadata | ConvertTo-Json -Compress -Depth 10
  
  # Fix game_versions: replace "game_versions":"value" with "game_versions":["value"]
  $metadataJson = $metadataJson -replace '("game_versions"\s*:\s*)"([^"]+)"', '$1["$2"]'
  
  # Fix loaders: replace "loaders":"value" with "loaders":["value"]  
  $metadataJson = $metadataJson -replace '("loaders"\s*:\s*)"([^"]+)"', '$1["$2"]'
  
  # Fix file_parts: replace "file_parts":"value" with "file_parts":["value"]
  $metadataJson = $metadataJson -replace '("file_parts"\s*:\s*)"([^"]+)"', '$1["$2"]'

  $uploadUri = "$($mr.BaseUrl)/v2/version"
  Write-Host "Uploading to Modrinth project $($mr.ProjectId)..."
  Write-Host "  URL: $uploadUri"
  Write-Host "  File: $fileName"
  Write-Host "  Game Versions: $($gameVersions -join ',')"
  Write-Host "  Loaders: $($loaders -join ',')"
  Write-Host "  Release Type: $releaseType"

  Add-Type -AssemblyName System.Net.Http
  $client = New-Object System.Net.Http.HttpClient
  $client.Timeout = [System.TimeSpan]::FromSeconds(120)
  $client.Timeout = [System.TimeSpan]::FromSeconds(60)
  # Modrinth uses "Authorization: <token>" header format
  $client.DefaultRequestHeaders.Add("Authorization", $mr.Token)

  $multipart = New-Object System.Net.Http.MultipartFormDataContent
  
  # Modrinth API v2 expects "data" field with JSON metadata
  $metaContent = New-Object System.Net.Http.StringContent($metadataJson, [System.Text.Encoding]::UTF8, "application/json")
  $multipart.Add($metaContent, "data")

  $fileStream = [System.IO.File]::OpenRead($filePath)
  try {
    $fileContent = New-Object System.Net.Http.StreamContent($fileStream)
    $fileContent.Headers.ContentType = New-Object System.Net.Http.Headers.MediaTypeHeaderValue("application/java-archive")
    $multipart.Add($fileContent, "file", $fileName)

    $resp = $client.PostAsync($uploadUri, $multipart).Result
    $respBody = $resp.Content.ReadAsStringAsync().Result
    if (-not $resp.IsSuccessStatusCode) {
      throw "Modrinth upload failed: $([int]$resp.StatusCode) $($resp.ReasonPhrase)
$respBody"
    }

    Write-Host "  [OK] Uploaded to Modrinth."
    if ($respBody) {
      try {
        $responseObj = $respBody | ConvertFrom-Json
        if ($responseObj.id) {
          Write-Host "  Version ID: $($responseObj.id)"
          Write-Host "  Version URL: https://modrinth.com/mod/$($mr.ProjectId)/version/$($responseObj.version_number)"
        }
      } catch {
        # Ignore JSON parse errors, just show raw response
        Write-Host "  Response: $respBody"
      }
    }
  } finally {
    $fileStream.Close()
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
  
  foreach ($artifact in $artifacts) {
    $fileName = $artifact.Name
    $filePath = $artifact.FullName
    $fileSize = (Get-Item $filePath).Length
    
    Write-Host "Uploading $fileName ($([math]::Round($fileSize / 1MB, 2)) MB)..."
    
    # GitHub release asset upload API expects raw file content
    # The upload URL should include ?name= parameter
    # Use PowerShell's built-in URI encoding
    $encodedFileName = [Uri]::EscapeDataString($fileName)
    # Use explicit string concatenation to avoid interpolation issues
    $uploadUrlWithName = $uploadUrlBase + "?name=" + $encodedFileName
    
    Write-Host "Debug: uploadUrlBase in loop = '$uploadUrlBase'"
    Write-Host "Debug: encodedFileName = '$encodedFileName'"
    Write-Host "Full upload URL: $uploadUrlWithName"
    
    # Validate URL before attempting upload
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
