#Requires -Version 5.1
# Run N full A/B worldgen Spark compares; report mean/median/spikes (honest wall scoreboard).
param(
  [int]$Runs = 5,
  [int]$RadiusChunks = 7,
  [int]$ProfileSeconds = 100,
  [int]$Seed = 12345,
  [string]$BenchRoot = $PSScriptRoot,
  [double]$SpikeFactor = 1.5
)

$ErrorActionPreference = "Stop"
$results = Join-Path $BenchRoot "results"
$compareScript = Join-Path $BenchRoot "Run-WorldgenSparkCompare.ps1"
$jars = Join-Path $BenchRoot "jars"
. (Join-Path $BenchRoot "Resolve-NoisiumedJar.ps1")
New-Item -ItemType Directory -Force -Path $results | Out-Null

$pinnedJar = Resolve-NoisiumedJar -JarsDir $jars
Write-Host "Pinned Noisiumed jar: $pinnedJar" -ForegroundColor Cyan

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
    if ($line -match 'write_pct=(\d+)') {
      $map.write_pct = [int]$Matches[1]
    }
    if ($line -match 'surface_skip=(\d+)') {
      $map.surface_skip = [int]$Matches[1]
    }
    if ($line -match 'nc3_grid=(\d+)') {
      $map.nc3_grid = [int]$Matches[1]
    }
  }
  return $map
}

function Avg([double[]]$arr) {
  if ($null -eq $arr -or $arr.Count -eq 0) { return $null }
  return ($arr | Measure-Object -Average).Average
}

function Std([double[]]$arr) {
  if ($null -eq $arr -or $arr.Count -lt 2) { return 0.0 }
  $m = Avg $arr
  $sum = 0.0
  foreach ($x in $arr) { $sum += ($x - $m) * ($x - $m) }
  return [math]::Sqrt($sum / ($arr.Count - 1))
}

function Median([double[]]$arr) {
  if ($null -eq $arr -or $arr.Count -eq 0) { return $null }
  $s = $arr | Sort-Object
  $n = $s.Count
  if ($n % 2 -eq 1) { return [double]$s[[int][math]::Floor($n / 2)] }
  return ([double]$s[$n / 2 - 1] + [double]$s[$n / 2]) / 2.0
}

function MinOf([double[]]$arr) {
  if ($null -eq $arr -or $arr.Count -eq 0) { return $null }
  return ($arr | Measure-Object -Minimum).Minimum
}

function MaxOf([double[]]$arr) {
  if ($null -eq $arr -or $arr.Count -eq 0) { return $null }
  return ($arr | Measure-Object -Maximum).Maximum
}

# Quiet set: drop values > SpikeFactor * median (when median known and n>=3).
function QuietSubset([double[]]$arr, [double]$factor) {
  if ($null -eq $arr -or $arr.Count -eq 0) { return @() }
  if ($arr.Count -lt 3) { return $arr }
  $med = Median $arr
  if ($null -eq $med -or $med -le 0) { return $arr }
  $thr = $med * $factor
  $q = @($arr | Where-Object { $_ -le $thr })
  if ($q.Count -eq 0) { return $arr }
  return $q
}

function SpikeFlags([double[]]$arr, [double]$factor) {
  if ($null -eq $arr -or $arr.Count -lt 3) { return @() }
  $med = Median $arr
  if ($null -eq $med -or $med -le 0) { return @() }
  $thr = $med * $factor
  $flags = @()
  for ($i = 0; $i -lt $arr.Count; $i++) {
    if ($arr[$i] -gt $thr) {
      $flags += ("run{0}={1:F0} (>{2:F1}x median {3:F0})" -f ($i + 1), $arr[$i], $factor, $med)
    }
  }
  return $flags
}

function PctFaster($avgN, $avgF) {
  if ($null -eq $avgN -or $null -eq $avgF -or $avgF -eq 0) { return $null }
  return 100.0 * ($avgF - $avgN) / $avgF
}

$nWalls = New-Object System.Collections.Generic.List[double]
$nBoots = New-Object System.Collections.Generic.List[double]
$nTotals = New-Object System.Collections.Generic.List[double]
$nL1 = New-Object System.Collections.Generic.List[double]
$nSample = New-Object System.Collections.Generic.List[double]
$nWrite = New-Object System.Collections.Generic.List[double]
$fWalls = New-Object System.Collections.Generic.List[double]
$fBoots = New-Object System.Collections.Generic.List[double]
$fTotals = New-Object System.Collections.Generic.List[double]
$nSparks = New-Object System.Collections.Generic.List[string]
$fSparks = New-Object System.Collections.Generic.List[string]
$runRows = New-Object System.Collections.Generic.List[string]

function Stop-BenchJavaOnly {
  # Never kill unrelated Minecraft/Java servers — only harness run trees under this bench root.
  $root = [regex]::Escape($BenchRoot)
  Get-CimInstance Win32_Process -Filter "Name='java.exe'" -EA SilentlyContinue |
    Where-Object {
      $_.CommandLine -and (
        $_.CommandLine -match $root -or
        $_.CommandLine -match 'run-noisiumed|run-fastnoise|run-parity'
      )
    } |
    ForEach-Object {
      Write-Host "Stopping bench java pid=$($_.ProcessId)" -ForegroundColor DarkYellow
      Stop-Process -Id $_.ProcessId -Force -EA SilentlyContinue
    }
}

for ($r = 1; $r -le $Runs; $r++) {
  Write-Host "======== MULTI RUN $r / $Runs ========" -ForegroundColor Cyan
  Stop-BenchJavaOnly
  Start-Sleep -Seconds 3

  & pwsh -NoProfile -ExecutionPolicy Bypass -File $compareScript `
    -RadiusChunks $RadiusChunks -ProfileSeconds $ProfileSeconds -Seed $Seed `
    -BenchRoot $BenchRoot
  if ($LASTEXITCODE -ne 0) {
    Write-Host "Run $r FAILED exit=$LASTEXITCODE" -ForegroundColor Red
    $runRows.Add("| $r | FAIL | FAIL | | |")
    continue
  }

  Copy-Item (Join-Path $results "noisiumed-summary.txt") (Join-Path $results "multi-run$r-noisiumed-summary.txt") -Force -EA SilentlyContinue
  Copy-Item (Join-Path $results "fastnoise-summary.txt") (Join-Path $results "multi-run$r-fastnoise-summary.txt") -Force -EA SilentlyContinue

  $n = Parse-SummaryWall (Join-Path $results "noisiumed-summary.txt")
  $f = Parse-SummaryWall (Join-Path $results "fastnoise-summary.txt")

  if ($n.forceload) { $nWalls.Add([double]$n.forceload) }
  if ($n.boot) { $nBoots.Add([double]$n.boot) }
  if ($n.total) { $nTotals.Add([double]$n.total) }
  if ($n.l1_avg_us) { $nL1.Add([double]$n.l1_avg_us) }
  if ($n.sample_pct) { $nSample.Add([double]$n.sample_pct) }
  if ($n.write_pct) { $nWrite.Add([double]$n.write_pct) }
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

$nArr = $nWalls.ToArray()
$fArr = $fWalls.ToArray()
$avgN = Avg $nArr
$avgF = Avg $fArr
$medN = Median $nArr
$medF = Median $fArr
$minN = MinOf $nArr
$maxN = MaxOf $nArr
$minF = MinOf $fArr
$maxF = MaxOf $fArr
$stdN = Std $nArr
$stdF = Std $fArr
$pctMean = PctFaster $avgN $avgF
$pctMed = PctFaster $medN $medF

$nQuiet = QuietSubset $nArr $SpikeFactor
$fQuiet = QuietSubset $fArr $SpikeFactor
$qAvgN = Avg $nQuiet
$qAvgF = Avg $fQuiet
$qMedN = Median $nQuiet
$qMedF = Median $fQuiet
$pctQuietMed = PctFaster $qMedN $qMedF
$pctQuietMean = PctFaster $qAvgN $qAvgF

$spikesN = SpikeFlags $nArr $SpikeFactor
$spikesF = SpikeFlags $fArr $SpikeFactor

$report = @()
$report += "# Multi-run Spark worldgen A/B (honest scoreboard)"
$report += ""
$report += "Date: $(Get-Date -Format o)"
$report += "Runs=$Runs seed=$Seed radiusChunks=$RadiusChunks profileSeconds=$ProfileSeconds"
$report += "SpikeFactor=$SpikeFactor (values > factor x median flagged / dropped from quiet set)"
$report += "Jar: ``$(Split-Path $pinnedJar -Leaf)`` vs zfastnoise 1.0.13"
$report += ""
$report += "## Per-run wall_forceload_ms"
$report += ""
$report += "| Run | Noisiumed | Fast Noise | Noisiumed Spark | FN Spark |"
$report += "|-----|-----------|------------|-----------------|----------|"
$report += $runRows
$report += ""
$report += "## Wall forceload summary"
$report += ""
$report += "| Metric | Noisiumed | Fast Noise | Delta (N-F) | % faster (N vs F) |"
$report += "|--------|-----------|------------|-------------|-------------------|"
$report += ("| **mean** | {0:F0} | {1:F0} | {2} | {3} |" -f `
    $(if ($null -ne $avgN) { $avgN } else { 0 }), `
    $(if ($null -ne $avgF) { $avgF } else { 0 }), `
    $(if ($null -ne $avgN -and $null -ne $avgF) { "{0:F0}" -f ($avgN - $avgF) } else { "-" }), `
    $(if ($null -ne $pctMean) { "{0:F1}%" -f $pctMean } else { "-" }))
$report += ("| **median** | {0:F0} | {1:F0} | {2} | {3} |" -f `
    $(if ($null -ne $medN) { $medN } else { 0 }), `
    $(if ($null -ne $medF) { $medF } else { 0 }), `
    $(if ($null -ne $medN -and $null -ne $medF) { "{0:F0}" -f ($medN - $medF) } else { "-" }), `
    $(if ($null -ne $pctMed) { "{0:F1}%" -f $pctMed } else { "-" }))
$report += ("| min | {0:F0} | {1:F0} | | |" -f $(if ($null -ne $minN) { $minN } else { 0 }), $(if ($null -ne $minF) { $minF } else { 0 }))
$report += ("| max | {0:F0} | {1:F0} | | |" -f $(if ($null -ne $maxN) { $maxN } else { 0 }), $(if ($null -ne $maxF) { $maxF } else { 0 }))
$report += ("| stdev | {0:F0} | {1:F0} | | |" -f $stdN, $stdF)
$report += ("| **quiet mean** (no spikes) | {0:F0} | {1:F0} | {2} | {3} |" -f `
    $(if ($null -ne $qAvgN) { $qAvgN } else { 0 }), `
    $(if ($null -ne $qAvgF) { $qAvgF } else { 0 }), `
    $(if ($null -ne $qAvgN -and $null -ne $qAvgF) { "{0:F0}" -f ($qAvgN - $qAvgF) } else { "-" }), `
    $(if ($null -ne $pctQuietMean) { "{0:F1}%" -f $pctQuietMean } else { "-" }))
$report += ("| **quiet median** | {0:F0} | {1:F0} | {2} | {3} |" -f `
    $(if ($null -ne $qMedN) { $qMedN } else { 0 }), `
    $(if ($null -ne $qMedF) { $qMedF } else { 0 }), `
    $(if ($null -ne $qMedN -and $null -ne $qMedF) { "{0:F0}" -f ($qMedN - $qMedF) } else { "-" }), `
    $(if ($null -ne $pctQuietMed) { "{0:F1}%" -f $pctQuietMed } else { "-" }))
$report += ("| wall_boot_ms avg | {0:F0} | {1:F0} | | |" -f (Avg $nBoots.ToArray()), (Avg $fBoots.ToArray()))
$report += ("| wall_total_ms avg | {0:F0} | {1:F0} | | |" -f (Avg $nTotals.ToArray()), (Avg $fTotals.ToArray()))
if ($nL1.Count -gt 0) {
  $report += ("| l1_avg_us avg | {0:F0} | n/a | | |" -f (Avg $nL1.ToArray()))
}
if ($nSample.Count -gt 0) {
  $report += ("| sample_pct avg | {0:F1} | n/a | | |" -f (Avg $nSample.ToArray()))
}
if ($nWrite.Count -gt 0) {
  $report += ("| write_pct avg | {0:F1} | n/a | | |" -f (Avg $nWrite.ToArray()))
}
$report += ""
$report += "Positive **% faster** means Noisiumed forceload is lower (better). **Quiet** drops runs > SpikeFactor x median."
$report += ""
$report += "## Spikes"
$report += ""
if ($spikesN.Count -eq 0) { $report += "- Noisiumed: none" } else { $report += "- Noisiumed: $($spikesN -join '; ')" }
if ($spikesF.Count -eq 0) { $report += "- Fast Noise: none" } else { $report += "- Fast Noise: $($spikesF -join '; ')" }
$report += ""
$report += "## Successful forceload samples"
$report += "- Noisiumed n=$($nWalls.Count): $($nWalls -join ', ')"
$report += "- Fast Noise n=$($fWalls.Count): $($fWalls -join ', ')"
$report += "- Quiet N n=$($nQuiet.Count): $($nQuiet -join ', ')"
$report += "- Quiet F n=$($fQuiet.Count): $($fQuiet -join ', ')"
$report += ""
$report += "## Dominance gate (plan)"
$report += ""
$gate = $false
if ($null -ne $pctQuietMed -and $pctQuietMed -ge 10.0 -and $null -ne $pctMean -and $pctMean -ge 5.0) {
  $gate = $true
}
$report += ("- Quiet median ≥10% faster: **{0}** ({1})" -f $(if ($null -ne $pctQuietMed -and $pctQuietMed -ge 10) { "PASS" } else { "FAIL" }), $(if ($null -ne $pctQuietMed) { "{0:F1}%" -f $pctQuietMed } else { "n/a" }))
$report += ("- Multi mean ≥5% faster: **{0}** ({1})" -f $(if ($null -ne $pctMean -and $pctMean -ge 5) { "PASS" } else { "FAIL" }), $(if ($null -ne $pctMean) { "{0:F1}%" -f $pctMean } else { "n/a" }))
$report += ("- Combined dominance gate: **{0}**" -f $(if ($gate) { "PASS" } else { "FAIL" }))
$report += ""

$out = Join-Path $results "MULTI_COMPARE_REPORT.md"
$report | Set-Content $out -Encoding utf8
Write-Host "DONE multi-report -> $out" -ForegroundColor Magenta
Write-Host ("MEAN  N={0:F0} FN={1:F0}  ({2})" -f $avgN, $avgF, $(if ($null -ne $pctMean) { "{0:F1}% N faster" -f $pctMean } else { "n/a" })) -ForegroundColor Yellow
Write-Host ("MEDIAN N={0:F0} FN={1:F0}  ({2})" -f $medN, $medF, $(if ($null -ne $pctMed) { "{0:F1}% N faster" -f $pctMed } else { "n/a" })) -ForegroundColor Yellow
Write-Host ("QUIET MEDIAN N={0:F0} FN={1:F0}  ({2})" -f $qMedN, $qMedF, $(if ($null -ne $pctQuietMed) { "{0:F1}% N faster" -f $pctQuietMed } else { "n/a" })) -ForegroundColor Yellow
