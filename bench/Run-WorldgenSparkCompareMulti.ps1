#Requires -Version 5.1
# Run N full A/B worldgen Spark compares and average wall metrics.
param(
  [int]$Runs = 5,
  [int]$RadiusChunks = 7,
  [int]$ProfileSeconds = 100,
  [int]$Seed = 12345,
  [string]$BenchRoot = $PSScriptRoot
)

$ErrorActionPreference = "Stop"
$results = Join-Path $BenchRoot "results"
$compareScript = Join-Path $BenchRoot "Run-WorldgenSparkCompare.ps1"
New-Item -ItemType Directory -Force -Path $results | Out-Null

function Parse-SummaryWall([string]$path) {
  $map = @{}
  if (-not (Test-Path $path)) { return $map }
  foreach ($line in Get-Content $path) {
    if ($line -match 'wall_boot_ms=(\d+)\s+wall_forceload_ms=(\d+)\s+wall_profile_window_ms=(\d+)\s+wall_total_ms=(\d+)') {
      $map.boot = [int]$Matches[1]
      $map.forceload = [int]$Matches[2]
      $map.profile = [int]$Matches[3]
      $map.total = [int]$Matches[4]
    }
    if ($line -match 'spark_url=(https://spark\.lucko\.me/\S+)') {
      $map.spark = $Matches[1]
    }
    if ($line -match 'l1_avg_us=(\d+)') {
      $map.l1_avg_us = [int]$Matches[1]
    }
    if ($line -match 'sample_pct=(\d+)') {
      $map.sample_pct = [int]$Matches[1]
    }
    if ($line -match 'nc3_grid=(\d+)') {
      $map.nc3_grid = [int]$Matches[1]
    }
  }
  return $map
}

function Avg([double[]]$arr) {
  if ($arr.Count -eq 0) { return $null }
  return ($arr | Measure-Object -Average).Average
}

function Std([double[]]$arr) {
  if ($arr.Count -lt 2) { return 0.0 }
  $m = Avg $arr
  $sum = 0.0
  foreach ($x in $arr) { $sum += ($x - $m) * ($x - $m) }
  return [math]::Sqrt($sum / ($arr.Count - 1))
}

$nWalls = New-Object System.Collections.Generic.List[double]
$nBoots = New-Object System.Collections.Generic.List[double]
$nTotals = New-Object System.Collections.Generic.List[double]
$nL1 = New-Object System.Collections.Generic.List[double]
$nSample = New-Object System.Collections.Generic.List[double]
$fWalls = New-Object System.Collections.Generic.List[double]
$fBoots = New-Object System.Collections.Generic.List[double]
$fTotals = New-Object System.Collections.Generic.List[double]
$nSparks = New-Object System.Collections.Generic.List[string]
$fSparks = New-Object System.Collections.Generic.List[string]
$runRows = New-Object System.Collections.Generic.List[string]

for ($r = 1; $r -le $Runs; $r++) {
  Write-Host "======== MULTI RUN $r / $Runs ========" -ForegroundColor Cyan
  Get-Process java -EA SilentlyContinue | Stop-Process -Force -EA SilentlyContinue
  Start-Sleep -Seconds 3

  & pwsh -NoProfile -ExecutionPolicy Bypass -File $compareScript `
    -RadiusChunks $RadiusChunks -ProfileSeconds $ProfileSeconds -Seed $Seed `
    -BenchRoot $BenchRoot
  if ($LASTEXITCODE -ne 0) {
    Write-Host "Run $r FAILED exit=$LASTEXITCODE" -ForegroundColor Red
    $runRows.Add("| $r | FAIL | FAIL | | |")
    continue
  }

  # Archive summaries
  $ts = Get-Date -Format "yyyyMMdd-HHmmss"
  Copy-Item (Join-Path $results "noisiumed-summary.txt") (Join-Path $results "multi-run$r-noisiumed-summary.txt") -Force -EA SilentlyContinue
  Copy-Item (Join-Path $results "fastnoise-summary.txt") (Join-Path $results "multi-run$r-fastnoise-summary.txt") -Force -EA SilentlyContinue

  $n = Parse-SummaryWall (Join-Path $results "noisiumed-summary.txt")
  $f = Parse-SummaryWall (Join-Path $results "fastnoise-summary.txt")

  if ($n.forceload) { $nWalls.Add([double]$n.forceload) }
  if ($n.boot) { $nBoots.Add([double]$n.boot) }
  if ($n.total) { $nTotals.Add([double]$n.total) }
  if ($n.l1_avg_us) { $nL1.Add([double]$n.l1_avg_us) }
  if ($n.sample_pct) { $nSample.Add([double]$n.sample_pct) }
  if ($n.spark) { $nSparks.Add($n.spark) }

  if ($f.forceload) { $fWalls.Add([double]$f.forceload) }
  if ($f.boot) { $fBoots.Add([double]$f.boot) }
  if ($f.total) { $fTotals.Add([double]$f.total) }
  if ($f.spark) { $fSparks.Add($f.spark) }

  $runRows.Add(("| {0} | {1} | {2} | {3} | {4} |" -f `
      $r, `
      $(if ($n.forceload) { $n.forceload } else { '-' }), `
      $(if ($f.forceload) { $f.forceload } else { '-' }), `
      $(if ($n.spark) { $n.spark } else { '-' }), `
      $(if ($f.spark) { $f.spark } else { '-' })))

  Write-Host ("Run {0}: noisiumed forceload={1}ms  fastnoise forceload={2}ms" -f $r, $n.forceload, $f.forceload) -ForegroundColor Green
}

$avgN = Avg $nWalls.ToArray()
$avgF = Avg $fWalls.ToArray()
$stdN = Std $nWalls.ToArray()
$stdF = Std $fWalls.ToArray()
$delta = $null
$pct = $null
if ($null -ne $avgN -and $null -ne $avgF -and $avgF -ne 0) {
  $delta = $avgN - $avgF
  $pct = 100.0 * ($avgF - $avgN) / $avgF
}

$report = @()
$report += "# Multi-run Spark worldgen A/B (averaged)"
$report += ""
$report += "Date: $(Get-Date -Format o)"
$report += "Runs=$Runs seed=$Seed radiusChunks=$RadiusChunks profileSeconds=$ProfileSeconds"
$report += "Jar: noisiumed beta (bench/jars) vs zfastnoise 1.0.13"
$report += ""
$report += "## Per-run wall_forceload_ms"
$report += ""
$report += "| Run | Noisiumed | Fast Noise | Noisiumed Spark | FN Spark |"
$report += "|-----|-----------|------------|-----------------|----------|"
$report += $runRows
$report += ""
$report += "## Averages"
$report += ""
$report += "| Metric | Noisiumed | Fast Noise | Delta (N-F) | % faster (N vs F) |"
$report += "|--------|-----------|------------|-------------|-------------------|"
$report += ("| wall_forceload_ms avg | {0:F0} | {1:F0} | {2} | {3} |" -f `
    $(if ($null -ne $avgN) { $avgN } else { 0 }), `
    $(if ($null -ne $avgF) { $avgF } else { 0 }), `
    $(if ($null -ne $delta) { "{0:F0}" -f $delta } else { "-" }), `
    $(if ($null -ne $pct) { "{0:F1}%" -f $pct } else { "-" }))
$report += ("| wall_forceload_ms stdev | {0:F0} | {1:F0} | | |" -f $stdN, $stdF)
$report += ("| wall_boot_ms avg | {0:F0} | {1:F0} | | |" -f (Avg $nBoots.ToArray()), (Avg $fBoots.ToArray()))
$report += ("| wall_total_ms avg | {0:F0} | {1:F0} | | |" -f (Avg $nTotals.ToArray()), (Avg $fTotals.ToArray()))
if ($nL1.Count -gt 0) {
  $report += ("| l1_avg_us avg | {0:F0} | n/a | | |" -f (Avg $nL1.ToArray()))
}
if ($nSample.Count -gt 0) {
  $report += ("| sample_pct avg | {0:F1} | n/a | | |" -f (Avg $nSample.ToArray()))
}
$report += ""
$report += "Positive **% faster** means Noisiumed forceload wall is lower (better)."
$report += ""
$report += "## Successful forceload samples"
$report += "- Noisiumed n=$($nWalls.Count): $($nWalls -join ', ')"
$report += "- Fast Noise n=$($fWalls.Count): $($fWalls -join ', ')"
$report += ""

$out = Join-Path $results "MULTI_COMPARE_REPORT.md"
$report | Set-Content $out -Encoding utf8
Write-Host "DONE multi-report -> $out" -ForegroundColor Magenta
Write-Host ("AVG forceload noisiumed={0:F0}ms FN={1:F0}ms  ({2})" -f $avgN, $avgF, $(if ($null -ne $pct) { "{0:F1}% faster for Noisiumed" -f $pct } else { "n/a" })) -ForegroundColor Yellow
