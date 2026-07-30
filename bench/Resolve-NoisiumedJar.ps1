# Shared jar resolution: exact pin first, then semver (not string sort — beta.9 must not beat beta.16).
function Resolve-NoisiumedJar {
  param(
    [Parameter(Mandatory)][string]$JarsDir,
    [string]$PreferName = "noisiumed-4.0.0-beta.16.5-w3-neoforge-1.21.1.jar"
  )
  $exact = Join-Path $JarsDir $PreferName
  if (Test-Path $exact) {
    return (Resolve-Path $exact).Path
  }

  $best = $null
  $bestKey = $null
  foreach ($f in Get-ChildItem $JarsDir -Filter "noisiumed-*-neoforge-1.21.1.jar" -EA SilentlyContinue) {
    # 4.0.0-beta.16.2 or 4.0.0 (release)
    if ($f.Name -notmatch 'noisiumed-(\d+)\.(\d+)\.(\d+)(?:-beta\.(\d+)(?:\.(\d+))?)?(?:-w\d+)?-neoforge') {
      continue
    }
    $maj = [int]$Matches[1]
    $min = [int]$Matches[2]
    $pat = [int]$Matches[3]
    # beta absent => treat as final (high). beta.N.M => (N, M)
    $b1 = if ($Matches[4]) { [int]$Matches[4] } else { 100000 }
    $b2 = if ($Matches[5]) { [int]$Matches[5] } else { 0 }
    $key = @($maj, $min, $pat, $b1, $b2)
    if ($null -eq $bestKey) {
      $best = $f
      $bestKey = $key
      continue
    }
    $better = $false
    for ($i = 0; $i -lt 5; $i++) {
      if ($key[$i] -gt $bestKey[$i]) { $better = $true; break }
      if ($key[$i] -lt $bestKey[$i]) { break }
    }
    if ($better) {
      $best = $f
      $bestKey = $key
    }
  }
  if (-not $best) {
    throw "No noisiumed-*-neoforge-1.21.1.jar under $JarsDir (prefer $PreferName)"
  }
  return $best.FullName
}
