#Requires -Version 5.1
param(
  [string]$BenchRoot = $PSScriptRoot,
  [string]$Java = "C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot\bin\java.exe",
  [int]$XmxGb = 4,
  [int]$RadiusChunks = 10,
  [int]$ProfileSeconds = 75,
  [int]$Seed = 12345,
  [int]$ServerPort = 25570,
  [int]$RconPort = 25575,
  [string]$RconPassword = "benchspark",
  [switch]$SkipNoisiumed,
  [switch]$SkipFastNoise
)

$ErrorActionPreference = "Stop"
$base = Join-Path $BenchRoot "server-base"
$jars = Join-Path $BenchRoot "jars"
$results = Join-Path $BenchRoot "results"
$rconPy = Join-Path $BenchRoot "rcon_cmd.py"
New-Item -ItemType Directory -Force -Path $results | Out-Null

if (-not (Test-Path $Java)) { throw "Java not found: $Java" }
$nfWinArgs = Get-ChildItem (Join-Path $base "libraries\net\neoforged\neoforge") -Recurse -Filter "win_args.txt" -ErrorAction SilentlyContinue |
  Sort-Object FullName -Descending | Select-Object -First 1
if (-not $nfWinArgs) { throw "NeoForge server-base incomplete (no win_args.txt)." }
$nfArgsAt = "@" + (($nfWinArgs.FullName -replace [regex]::Escape($base), "").TrimStart('\','/') -replace '\\','/')
Write-Host "Using NeoForge args: $nfArgsAt"

function Invoke-Rcon([string]$Command) {
  & python $rconPy 127.0.0.1 $RconPort $RconPassword $Command
}

function Prepare-ServerDir {
  param([string]$Name, [string[]]$ModJars, [int]$Port, [int]$RPort)
  $dir = Join-Path $BenchRoot "run-$Name"
  if (Test-Path $dir) { Remove-Item $dir -Recurse -Force }
  New-Item -ItemType Directory -Force -Path $dir, (Join-Path $dir "mods") | Out-Null

  $libLink = Join-Path $dir "libraries"
  cmd /c mklink /J "`"$libLink`"" "`"$(Join-Path $base 'libraries')`"" | Out-Null

  Set-Content -Path (Join-Path $dir "user_jvm_args.txt") -Value @"
-Xms${XmxGb}G
-Xmx${XmxGb}G
-XX:+UseG1GC
-Dfile.encoding=UTF-8
"@
  Set-Content -Path (Join-Path $dir "eula.txt") -Value "eula=true"
  # max-tick-time=-1 disables ServerWatchdog (forceload can block main thread >60s).
  Set-Content -Path (Join-Path $dir "server.properties") -Value @"
server-port=$Port
online-mode=false
max-players=2
motd=noisiumed-spark-bench-$Name
level-name=world
level-seed=$Seed
level-type=minecraft\:normal
spawn-protection=0
view-distance=10
simulation-distance=8
difficulty=peaceful
gamemode=creative
allow-flight=true
enable-command-block=true
white-list=false
spawn-monsters=false
spawn-animals=false
spawn-npcs=false
enable-rcon=true
rcon.port=$RPort
rcon.password=$RconPassword
broadcast-rcon-to-ops=true
max-tick-time=-1
"@
  foreach ($m in $ModJars) { Copy-Item $m (Join-Path $dir "mods") -Force }
  return $dir
}

function Get-SparkUrlFromLog([string]$logFile) {
  if (-not (Test-Path $logFile)) { return $null }
  # Exclude docs/* agent-warning links; keep short viewer codes only.
  $m = Select-String -Path $logFile -Pattern 'https://spark\.lucko\.me/[A-Za-z0-9]{6,14}(?![A-Za-z0-9/])' -AllMatches
  if (-not $m) { return $null }
  return ($m | ForEach-Object { $_.Matches } | ForEach-Object { $_.Value } | Select-Object -Last 1)
}

function Save-SparkBin([string]$SparkUrl, [string]$OutDir) {
  if (-not $SparkUrl) { return $null }
  if ($SparkUrl -notmatch 'spark\.lucko\.me/(\w+)') { return $null }
  $code = $Matches[1]
  $dest = Join-Path $OutDir "$code.bin"
  foreach ($baseUrl in @("https://spark-usercontent.lucko.me/$code", "https://bytebin.lucko.me/$code")) {
    try {
      Invoke-WebRequest -Uri $baseUrl -OutFile $dest -UseBasicParsing -TimeoutSec 90
      if ((Get-Item $dest).Length -gt 1000) {
        Write-Host "Saved Spark bin: $dest ($((Get-Item $dest).Length) bytes)" -ForegroundColor Green
        return $dest
      }
    } catch {
      Write-Host "Spark bin fetch failed from $baseUrl : $_" -ForegroundColor DarkYellow
    }
  }
  return $null
}

function Run-SparkWorldgen {
  param([string]$Label, [string]$ServerDir, [int]$Port, [int]$RPort)

  $script:RconPort = $RPort
  $logFile = Join-Path $results "$Label-console.log"
  $summaryFile = Join-Path $results "$Label-summary.txt"
  if (Test-Path $logFile) { Remove-Item $logFile -Force }

  $wall = [ordered]@{
    t_start = Get-Date
    t_ready = $null
    t_forceload_start = $null
    t_forceload_end = $null
    t_profile_end = $null
    t_stop = $null
  }

  $argLine = "@user_jvm_args.txt $nfArgsAt nogui"
  Write-Host "==== Starting $Label on :$Port (rcon :$RPort) ====" -ForegroundColor Cyan

  $bat = Join-Path $ServerDir "start-bench.cmd"
  @"
@echo off
"$Java" $argLine > "$logFile" 2>&1
"@ | Set-Content -Path $bat -Encoding ascii

  $p = Start-Process -FilePath "cmd.exe" -ArgumentList "/c `"$bat`"" -WorkingDirectory $ServerDir -PassThru -WindowStyle Hidden

  $ready = $false
  $deadline = (Get-Date).AddMinutes(12)
  while ((Get-Date) -lt $deadline) {
    if ($p.HasExited) { break }
    if (Test-Path $logFile) {
      $text = Get-Content $logFile -Raw -ErrorAction SilentlyContinue
      if ($text -and ($text -match 'Done \(' -or $text -match 'For help, type')) {
        $ready = $true
        break
      }
    }
    Start-Sleep -Milliseconds 400
  }

  if (-not $ready) {
    if (-not $p.HasExited) { Stop-Process -Id $p.Id -Force -ErrorAction SilentlyContinue }
    throw "$Label never became ready. See $logFile"
  }
  $wall.t_ready = Get-Date

  Write-Host "$Label READY - waiting for RCON..." -ForegroundColor Green
  Start-Sleep -Seconds 2

  $rconOk = $false
  for ($i = 0; $i -lt 30; $i++) {
    try {
      $null = Invoke-Rcon "list"
      $rconOk = $true
      break
    } catch {
      Start-Sleep -Seconds 1
    }
  }
  if (-not $rconOk) {
    Stop-Process -Id $p.Id -Force -EA SilentlyContinue
    throw "$Label RCON not available"
  }

  $R = $RadiusChunks
  Write-Host ">> spark profiler start"
  try { Invoke-Rcon "spark profiler start --thread *" | Write-Host } catch { Write-Host $_ }

  $wall.t_forceload_start = Get-Date
  $tileR = [Math]::Min($R, 7)
  $half = $tileR * 16
  $tiles = @(
    @{ ox = 2048; oz = 2048 },
    @{ ox = 2048 + 32 * 16; oz = 2048 },
    @{ ox = 2048; oz = 2048 + 32 * 16 },
    @{ ox = 2048 + 32 * 16; oz = 2048 + 32 * 16 }
  )
  foreach ($t in $tiles) {
    $x1 = $t.ox - $half; $z1 = $t.oz - $half
    $x2 = $t.ox + $half; $z2 = $t.oz + $half
    $flCmd = "forceload add $x1 $z1 $x2 $z2"
    Write-Host ">> $flCmd"
    try { Invoke-Rcon $flCmd | Write-Host } catch { Write-Host $_ }
    Start-Sleep -Milliseconds 500
  }
  $wall.t_forceload_end = Get-Date
  try { Invoke-Rcon "say bench profile ${ProfileSeconds}s tiles=$($tiles.Count) r=$tileR" | Out-Null } catch {}

  Write-Host "Profiling ${ProfileSeconds}s (worldgen via forceload tiles)..."
  Start-Sleep -Seconds $ProfileSeconds
  $wall.t_profile_end = Get-Date

  Write-Host ">> spark profiler stop"
  $stopOut = ""
  try { $stopOut = Invoke-Rcon "spark profiler stop" } catch { $stopOut = "$_" }
  Write-Host $stopOut
  Start-Sleep -Seconds 4
  $sparkUrl = Get-SparkUrlFromLog $logFile
  if ($sparkUrl) { $stopOut = "$stopOut`n$sparkUrl" }

  $tpsOut = ""
  try { $tpsOut = Invoke-Rcon "spark tps" } catch { $tpsOut = "$_" }
  Write-Host $tpsOut
  Start-Sleep -Seconds 1
  $healthOut = ""
  try { $healthOut = Invoke-Rcon "spark health --memory" } catch { $healthOut = "$_" }
  Write-Host $healthOut

  try { Invoke-Rcon "forceload remove all" | Out-Null } catch {}
  try { Invoke-Rcon "stop" | Out-Null } catch {}

  $stopDl = (Get-Date).AddMinutes(2)
  while (-not $p.HasExited -and (Get-Date) -lt $stopDl) { Start-Sleep -Milliseconds 400 }
  if (-not $p.HasExited) {
    Stop-Process -Id $p.Id -Force -EA SilentlyContinue
    # Only children/siblings under this label's server dir — never other MC servers.
    $dirEsc = [regex]::Escape($ServerDir)
    Get-CimInstance Win32_Process -Filter "Name = 'java.exe'" -EA SilentlyContinue |
      Where-Object { $_.CommandLine -and $_.CommandLine -match $dirEsc } |
      ForEach-Object { Stop-Process -Id $_.ProcessId -Force -EA SilentlyContinue }
  }
  $wall.t_stop = Get-Date
  Start-Sleep -Seconds 3

  # Re-read spark URL after stop flush
  if (-not $sparkUrl) { $sparkUrl = Get-SparkUrlFromLog $logFile }
  $binPath = Save-SparkBin -SparkUrl $sparkUrl -OutDir $results

  $sparkOut = Join-Path $results $Label
  New-Item -ItemType Directory -Force -Path $sparkOut | Out-Null
  $sparkDir = Join-Path $ServerDir "spark"
  if (Test-Path $sparkDir) {
    Copy-Item "$sparkDir\*" $sparkOut -Recurse -Force -EA SilentlyContinue
  }
  $latest = Join-Path $ServerDir "logs\latest.log"
  if (Test-Path $latest) { Copy-Item $latest (Join-Path $results "$Label-latest.log") -Force }

  $bootMs = [int](($wall.t_ready - $wall.t_start).TotalMilliseconds)
  $forceloadMs = [int](($wall.t_forceload_end - $wall.t_forceload_start).TotalMilliseconds)
  $profileMs = [int](($wall.t_profile_end - $wall.t_forceload_start).TotalMilliseconds)
  $totalMs = [int](($wall.t_stop - $wall.t_start).TotalMilliseconds)

  $blob = ""
  if (Test-Path $logFile) { $blob += Get-Content $logFile -Raw }
  if (Test-Path $latest) { $blob += "`n" + (Get-Content $latest -Raw) }

  $interesting = ($blob -split "`r?`n") | Where-Object {
    $_ -match 'spark|Profiler|profiler|TPS|MSPT|forceload|populateNoise|NoiseChunk|world.?gen|chunks|noisium|fastnoise|zfastnoise|Sampler|CPU|viewer|noisiumed\.path|PathMetrics|l1_avg'
  } | Select-Object -Last 160

  $pathLine = ($blob -split "`r?`n") | Where-Object { $_ -match 'noisiumed\.path' } | Select-Object -Last 3

  @(
    "label=$Label",
    "seed=$Seed radiusChunks=$RadiusChunks profileSeconds=$ProfileSeconds port=$Port",
    "wall_boot_ms=$bootMs wall_forceload_ms=$forceloadMs wall_profile_window_ms=$profileMs wall_total_ms=$totalMs",
    "spark_url=$sparkUrl",
    "spark_bin=$binPath",
    "--- path metrics (from log) ---"
  ) + @($pathLine) + @(
    "--- rcon spark profiler stop ---",
    $stopOut,
    "--- rcon spark tps ---",
    $tpsOut,
    "--- rcon spark health ---",
    $healthOut,
    "--- matched log lines ---"
  ) + $interesting | Set-Content $summaryFile

  Get-ChildItem $sparkOut -Recurse -EA SilentlyContinue | ForEach-Object { $_.FullName } |
    Set-Content (Join-Path $results "$Label-spark-files.txt")

  Write-Host "WALL $Label boot=${bootMs}ms forceload=${forceloadMs}ms profile_window=${profileMs}ms total=${totalMs}ms" -ForegroundColor Magenta
  Write-Host "Summary: $summaryFile" -ForegroundColor Yellow
  return $summaryFile
}

. (Join-Path $BenchRoot "Resolve-NoisiumedJar.ps1")
$sparkJar = Join-Path $jars "spark-1.10.124-neoforge.jar"
$noisiumedJar = Resolve-NoisiumedJar -JarsDir $jars
$fnJar = Join-Path $jars "zfastnoise-1.0.13+1.21.1+neoforge.jar"
Write-Host "Noisiumed jar: $noisiumedJar" -ForegroundColor Cyan

$report = New-Object System.Collections.Generic.List[string]
$report.Add("# Noisiumed vs Fast Noise - Spark worldgen compare")
$report.Add("Date: $(Get-Date -Format o)")
$report.Add("Platform: NeoForge 21.1.233 / MC 1.21.1 (dedicated, nogui, RCON)")
$report.Add("Seed=$Seed forceload radius=$RadiusChunks profile=${ProfileSeconds}s Xmx=${XmxGb}G")
$report.Add("Baseline mods: Spark + candidate only")
$report.Add("Huge-win metrics: wall_boot_ms / wall_forceload_ms / wall_profile_window_ms / wall_total_ms + PathMetrics")
$report.Add("")
$report.Add("**Scope:** Primary **1.21.1**. A/B = **$(Split-Path $noisiumedJar -Leaf)** vs Fast Noise 1.0.13.")
$report.Add("")

if (-not $SkipNoisiumed) {
  if (-not (Test-Path $noisiumedJar)) { throw "Missing $noisiumedJar" }
  $dir = Prepare-ServerDir -Name "noisiumed" -ModJars @($sparkJar, $noisiumedJar) -Port $ServerPort -RPort $RconPort
  $s = Run-SparkWorldgen -Label "noisiumed" -ServerDir $dir -Port $ServerPort -RPort $RconPort
  $report.Add("## Noisiumed")
  $report.Add('```')
  $report.Add((Get-Content $s -Raw))
  $report.Add('```')
  $report.Add("")
}

if (-not $SkipFastNoise) {
  $dir = Prepare-ServerDir -Name "fastnoise" -ModJars @($sparkJar, $fnJar) -Port ($ServerPort + 1) -RPort ($RconPort + 1)
  $s = Run-SparkWorldgen -Label "fastnoise" -ServerDir $dir -Port ($ServerPort + 1) -RPort ($RconPort + 1)
  $report.Add("## Fast Noise 1.0.13")
  $report.Add('```')
  $report.Add((Get-Content $s -Raw))
  $report.Add('```')
  $report.Add("")
}

$reportPath = Join-Path $results "COMPARE_REPORT.md"
$report | Set-Content $reportPath
Write-Host "DONE -> $reportPath" -ForegroundColor Green
