#Requires -Version 5.1
<#
.SYNOPSIS
  Golden section-hash parity: generate same seed/radius with baseline (spark only)
  vs Noisiumed, hash overworld block sections, compare.

.EXAMPLE
  .\Run-ParityHash.ps1 -RadiusChunks 2 -Seed 12345
#>
param(
  [string]$BenchRoot = $PSScriptRoot,
  [string]$Java = "C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot\bin\java.exe",
  [int]$XmxGb = 3,
  [int]$RadiusChunks = 2,
  [int]$Seed = 12345,
  [int]$ServerPort = 25580,
  [int]$RconPort = 25585,
  [string]$RconPassword = "benchparity"
)

$ErrorActionPreference = "Stop"
$base = Join-Path $BenchRoot "server-base"
$jars = Join-Path $BenchRoot "jars"
$results = Join-Path $BenchRoot "results"
$rconPy = Join-Path $BenchRoot "rcon_cmd.py"
$hashPy = Join-Path $BenchRoot "parity_hash.py"
New-Item -ItemType Directory -Force -Path $results | Out-Null

if (-not (Test-Path $Java)) { throw "Java not found: $Java" }
if (-not (Test-Path $hashPy)) { throw "Missing $hashPy" }

$nfWinArgs = Get-ChildItem (Join-Path $base "libraries\net\neoforged\neoforge") -Recurse -Filter "win_args.txt" -ErrorAction SilentlyContinue |
  Sort-Object FullName -Descending | Select-Object -First 1
if (-not $nfWinArgs) { throw "NeoForge server-base incomplete." }
$nfArgsAt = "@" + (($nfWinArgs.FullName -replace [regex]::Escape($base), "").TrimStart('\','/') -replace '\\','/')

$sparkJar = Join-Path $jars "spark-1.10.124-neoforge.jar"
$noisiumedJar = Join-Path $jars "noisiumed-4.0.0-beta.12-neoforge-1.21.1.jar"
if (-not (Test-Path $noisiumedJar)) {
  $noisiumedJar = Join-Path $jars "noisiumed-4.0.0-beta.11-neoforge-1.21.1.jar"
}
if (-not (Test-Path $sparkJar)) { throw "Missing spark jar" }
if (-not (Test-Path $noisiumedJar)) { throw "Missing noisiumed jar" }

function Invoke-RconLocal([int]$Port, [string]$Command) {
  & python $rconPy 127.0.0.1 $Port $RconPassword $Command
}

function Prepare-ParityDir {
  param([string]$Name, [string[]]$ModJars, [int]$Port, [int]$RPort)
  $dir = Join-Path $BenchRoot "run-parity-$Name"
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
  Set-Content -Path (Join-Path $dir "server.properties") -Value @"
server-port=$Port
online-mode=false
max-players=2
motd=parity-$Name
level-name=world
level-seed=$Seed
level-type=minecraft\:normal
spawn-protection=0
view-distance=6
simulation-distance=4
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

function Stop-ParityProcess([System.Diagnostics.Process]$p) {
  if ($null -eq $p) { return }
  if (-not $p.HasExited) {
    try { Stop-Process -Id $p.Id -Force -ErrorAction SilentlyContinue } catch {}
    # also kill child java
    Get-CimInstance Win32_Process -Filter "Name='java.exe'" -ErrorAction SilentlyContinue |
      Where-Object { $_.CommandLine -match 'run-parity-' } |
      ForEach-Object { Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue }
  }
}

function Run-ParityGen {
  param([string]$Label, [string[]]$ModJars, [int]$Port, [int]$RPort)

  $dir = Prepare-ParityDir -Name $Label -ModJars $ModJars -Port $Port -RPort $RPort
  $console = Join-Path $results "parity-$Label-console.log"
  if (Test-Path $console) { Remove-Item $console -Force }

  $argLine = "@user_jvm_args.txt $nfArgsAt nogui"
  $bat = Join-Path $dir "start-parity.cmd"
  @"
@echo off
"$Java" $argLine > "$console" 2>&1
"@ | Set-Content -Path $bat -Encoding ascii

  Write-Host "==== parity $Label starting :$Port ====" -ForegroundColor Cyan
  $p = Start-Process -FilePath "cmd.exe" -ArgumentList "/c `"$bat`"" -WorkingDirectory $dir -PassThru -WindowStyle Hidden

  $ready = $false
  $deadline = (Get-Date).AddMinutes(12)
  while ((Get-Date) -lt $deadline) {
    if ($p.HasExited) { break }
    if (Test-Path $console) {
      $text = Get-Content $console -Raw -ErrorAction SilentlyContinue
      if ($text -and ($text -match 'Done \(' -or $text -match 'For help, type')) {
        $ready = $true
        break
      }
    }
    Start-Sleep -Milliseconds 400
  }
  if (-not $ready) {
    Stop-ParityProcess $p
    throw "$Label never became ready. See $console"
  }
  Write-Host "$Label READY" -ForegroundColor Green
  Start-Sleep -Seconds 2

  $rconOk = $false
  for ($i = 0; $i -lt 40; $i++) {
    try {
      $null = Invoke-RconLocal -Port $RPort -Command "list"
      $rconOk = $true
      break
    } catch {
      Start-Sleep -Seconds 1
    }
  }
  if (-not $rconOk) {
    Stop-ParityProcess $p
    throw "$Label RCON not available"
  }

  # forceload block box covering [-R..R] chunks around origin
  $b0 = -16 * $RadiusChunks
  $b1 = 16 * $RadiusChunks + 15
  $cmd = "forceload add $b0 $b0 $b1 $b1"
  Write-Host ">> $cmd"
  try { Invoke-RconLocal -Port $RPort -Command $cmd | Write-Host } catch { Write-Host $_ }

  # Wait until all radius chunks report Status full.
  $deadline = (Get-Date).AddSeconds(300)
  $tmpHash = Join-Path $results "parity-$Label-wait.json"
  $fullOk = $false
  while ((Get-Date) -lt $deadline) {
    try { Invoke-RconLocal -Port $RPort -Command "save-all flush" | Out-Null } catch {}
    Start-Sleep -Seconds 4
    $worldProbe = Join-Path $dir "world"
    if (Test-Path (Join-Path $worldProbe "region")) {
      & python $hashPy $worldProbe --radius $RadiusChunks --out $tmpHash 2>$null | Out-Null
      if (Test-Path $tmpHash) {
        $probe = Get-Content $tmpHash -Raw | ConvertFrom-Json
        if ([int]$probe.missing -eq 0 -and [int]$probe.not_full -eq 0 -and [int]$probe.errors -eq 0) {
          $fullOk = $true
          Write-Host "$Label chunks FULL" -ForegroundColor Green
          break
        }
        Write-Host ("  wait full: missing={0} not_full={1}" -f $probe.missing, $probe.not_full) -ForegroundColor DarkGray
      }
    }
    Start-Sleep -Seconds 3
  }
  if (-not $fullOk) { Write-Host "$Label WARNING: FULL wait timeout" -ForegroundColor Yellow }

  try { Invoke-RconLocal -Port $RPort -Command "save-all flush" | Out-Null } catch {}
  Start-Sleep -Seconds 3
  try { Invoke-RconLocal -Port $RPort -Command "stop" | Out-Null } catch {}
  $waited = 0
  while (-not $p.HasExited -and $waited -lt 90) {
    Start-Sleep -Seconds 1
    $waited++
  }
  Stop-ParityProcess $p

  $world = Join-Path $dir "world"
  $outJson = Join-Path $results "parity-$Label-hash.json"
  & python $hashPy $world --radius $RadiusChunks --seed $Seed --out $outJson | Write-Host
  $code = $LASTEXITCODE
  if ($code -ne 0 -and $code -ne 3) {
    Write-Host "hash exit=$code (3=missing chunks ok-ish)" -ForegroundColor Yellow
  }
  return ,$outJson
}

# kill leftover parity java
Get-CimInstance Win32_Process -Filter "Name='java.exe'" -EA SilentlyContinue |
  Where-Object { $_.CommandLine -match 'run-parity-' } |
  ForEach-Object { Stop-Process -Id $_.ProcessId -Force -EA SilentlyContinue }

$baseJson = Run-ParityGen -Label "baseline" -ModJars @($sparkJar) -Port $ServerPort -RPort $RconPort
$nJson = Run-ParityGen -Label "noisiumed" -ModJars @($sparkJar, $noisiumedJar) -Port ($ServerPort + 1) -RPort ($RconPort + 1)

Write-Host "==== COMPARE ====" -ForegroundColor Magenta
$cmpOut = Join-Path $results "PARITY_COMPARE.json"
& python $hashPy --compare-a $baseJson --compare-b $nJson | Tee-Object -FilePath $cmpOut
$cmp = Get-Content $cmpOut -Raw | ConvertFrom-Json

$md = Join-Path $results "PARITY_REPORT.md"
@"
# Golden section hash parity

Date: $(Get-Date -Format o)
Seed=$Seed radiusChunks=$RadiusChunks
Baseline: spark only (vanilla NoiseChunk)
Candidate: $(Split-Path $noisiumedJar -Leaf)

| | |
|--|--|
| baseline overall | $($cmp.overall_a) |
| noisiumed overall | $($cmp.overall_b) |
| chunk matches | $($cmp.matches) |
| mismatches | $($cmp.mismatches) |
| **PASS** | **$($cmp.match)** |

Details: ``bench/results/PARITY_COMPARE.json``
"@ | Set-Content $md -Encoding UTF8

Write-Host "Report: $md" -ForegroundColor Green
if (-not $cmp.match) {
  Write-Host "PARITY FAIL ($($cmp.mismatches) chunks differ)" -ForegroundColor Red
  exit 2
}
Write-Host "PARITY PASS" -ForegroundColor Green
exit 0
