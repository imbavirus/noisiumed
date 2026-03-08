<#
.SYNOPSIS
  Iterates through all Minecraft version branches and runs the publish.ps1 script on each.

.DESCRIPTION
  This script automates the process of checking out each version branch, pulling the latest changes,
  and executing the local publish.ps1 script with the provided parameters.

.EXAMPLE
  pwsh ./publish-all.ps1 -Bump minor -Note "Release 3.0.2"
#>

[CmdletBinding(PositionalBinding=$false)]
param(
  [ValidateSet("patch", "minor", "major")]
  [string]$Bump = "patch",

  [switch]$OnlyPublish,
  [string]$Version,
  [string]$Note = "",
  [string]$GitHubToken,
  [string]$GitHubRepo,
  [switch]$SkipCurseForge,
  [switch]$SkipModrinth,
  [switch]$OnlyGit,
  [switch]$OnlyCurseForge,
  [switch]$OnlyModrinth,
  [switch]$SkipBuild
)

$ErrorActionPreference = "Stop"

# List of branches to iterate over
$Branches = @(
  "1.20-1.20.1",
  "1.20.2-1.20.4",
  "1.20.5-1.20.6",
  "1.21-1.21.1",
  "1.21.2-1.21.3",
  "1.21.4",
  "1.21.5",
  "1.21.6"
)

# Remember starting branch
$StartingBranch = git rev-parse --abbrev-ref HEAD

try {
  foreach ($Branch in $Branches) {
    Write-Host "`n" + ("=" * 50) -ForegroundColor Cyan
    Write-Host " PROCESSING BRANCH: $Branch " -ForegroundColor Cyan -BackgroundColor DarkBlue
    Write-Host ("=" * 50) + "`n" -ForegroundColor Cyan

    Write-Host "Checking out $Branch..."
    git checkout $Branch
    if ($LASTEXITCODE -ne 0) { throw "Failed to checkout branch $Branch" }

    Write-Host "Pulling latest changes for $Branch..."
    git pull origin $Branch
    # git pull might fail if remote doesn't have the branch yet, which is fine for new syncs
    if ($LASTEXITCODE -ne 0) { Write-Warning "Git pull failed for $Branch. Continuing anyway..." }

    # Construct arguments to pass to publish.ps1
    $PublishArgs = @()
    if ($PSBoundParameters.ContainsKey('Bump')) { $PublishArgs += "-Bump", $Bump }
    if ($OnlyPublish) { $PublishArgs += "-OnlyPublish" }
    if ($Version) { $PublishArgs += "-Version", $Version }
    if ($Note) { $PublishArgs += "-Note", $Note }
    if ($GitHubToken) { $PublishArgs += "-GitHubToken", $GitHubToken }
    if ($GitHubRepo) { $PublishArgs += "-GitHubRepo", $GitHubRepo }
    if ($SkipCurseForge) { $PublishArgs += "-SkipCurseForge" }
    if ($SkipModrinth) { $PublishArgs += "-SkipModrinth" }
    if ($OnlyGit) { $PublishArgs += "-OnlyGit" }
    if ($OnlyCurseForge) { $PublishArgs += "-OnlyCurseForge" }
    if ($OnlyModrinth) { $PublishArgs += "-OnlyModrinth" }
    if ($SkipBuild) { $PublishArgs += "-SkipBuild" }

    if (Test-Path "publish.ps1") {
      Write-Host "Running publish.ps1 on $Branch with arguments: $($PublishArgs -join ' ')" -ForegroundColor Green
      pwsh ./publish.ps1 @PublishArgs
      if ($LASTEXITCODE -ne 0) { throw "Publish failed on branch $Branch" }
    } else {
      Write-Warning "No publish.ps1 found on branch $Branch. Skipping."
    }
  }

  Write-Host "`n" + ("*" * 50) -ForegroundColor Green
  Write-Host " ALL BRANCHES PROCESSED SUCCESSFULLY " -ForegroundColor Green -BackgroundColor DarkGreen
  Write-Host ("*" * 50) + "`n" -ForegroundColor Green

} finally {
  Write-Host "Returning to starting branch: $StartingBranch"
  git checkout $StartingBranch
}
