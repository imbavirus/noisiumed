#Requires -Version 5.1
<#
.SYNOPSIS
  Golden section-hash parity: generate same seed/radius with baseline (spark only)
  vs Noisiumed, hash overworld block sections, compare.

.EXAMPLE
  .\Run-ParityHash.ps1 -Radius 2 -Seed 12345
#>
param(
  [string]$BenchRoot = $PSScriptRoot,
  [string]$Java = "C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot\bin\java.exe",
  [int]$XmxGb = 3,
  [int]$RadiusChunks = 2,
  [int]$Seed = 12345,
  [int]$ServerPort = 25580,
  [int]$RconPort = 25585,
  [string]$RconPassword = "benchparity",
  [int]$ReadyTimeoutSec = 180
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
  # max-tick-time=-1 disables watchdog (needed when main thread waits on gen)
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

function Wait-ServerReady([System.Diagnostics.Process]$Proc, [string]$LogPath, [int]$TimeoutSec) {
  $deadline = (Get-Date).AddSeconds($TimeoutSec)
  while ((Get-Date) -lt $deadline) {
    if ($Proc.HasExited) { throw "Server exited early code=$($Proc.ExitCode)" }
    if (Test-Path $LogPath) {
      $hit = Select-String -Path $LogPath -Pattern 'Done \(|RCON running' -SimpleMatch:$false -ErrorAction SilentlyContinue
      if ($hit) { return }
    }
    Start-Sleep -Milliseconds 500
  }
  throw "Timeout waiting for server ready"
}

function Run-ParityGen {
  param([string]$Label, [string[]]$ModJars, [int]$Port, [int]$RPort)

  $dir = Prepare-ParityDir -Name $Label -ModJars $ModJars -Port $Port -RPort $RPort
  $logPath = Join-Path $dir "logs\latest.log"
  $console = Join-Path $results "parity-$Label-console.log"
  if (Test-Path $console) { Remove-Item $console -Force }

  $psi = New-Object System.Diagnostics.ProcessStartInfo
  $psi.FileName = $Java
  $psi.WorkingDirectory = $dir
  $psi.Arguments = "@user_jvm_args.txt $nfArgsAt nogui"
  $psi.UseShellExecute = $false
  $psi.RedirectStandardOutput = $true
  $psi.RedirectStandardError = $true
  $psi.CreateNoWindow = $true
  $proc = New-Object System.Diagnostics.Process
  $proc.StartInfo = $psi
  $null = $proc.Start()
  $outJob = Start-Job -ScriptBlock {
    param($p, $f)
    while (-not $p.HasExited) {
      $line = $p.StandardOutput.ReadLine()
      if ($null -ne $line) { Add-Content -Path $f -Value $line }
    }
  } -ArgumentList $proc, $console

  try {
    Write-Host "==== parity $Label starting :$Port ====" -ForegroundColor Cyan
    Wait-ServerReady -Proc $proc -LogPath $logPath -TimeoutSec $ReadyTimeoutSec
    Start-Sleep -Seconds 2

    # forceload radius around origin (block coords)
    $b0 = -16 * $RadiusChunks
    $b1 = 16 * $RadiusChunks + 15
    $cmd = "forceload add $b0 $b0 $b1 $b1"
    Write-Host ">> $cmd"
    Invoke-RconLocal -Port $RPort -Command $cmd | Out-Null

    # wait until chunks present: poll region file growth / sleep
    $deadline = (Get-Date).AddSeconds(300)
    $regionDir = Join-Path $dir "world\region"
    while ((Get-Date) -lt $deadline) {
      if (Test-Path $regionDir) {
        $mcas = Get-ChildItem $regionDir -Filter "*.mca" -ErrorAction SilentlyContinue
        if ($mcas -and ($mcas | Measure-Object -Property Length -Sum).Sum -gt 50000) {
          Start-Sleep -Seconds 5
          break
        }
      }
      Start-Sleep -Seconds 2
    }

    Invoke-RconLocal -Port $RPort -Command "save-all flush" | Out-Null
    Start-Sleep -Seconds 3
    Invoke-RconLocal -Port $RPort -Command "stop" | Out-Null
    $proc.WaitForExit(120000) | Out-Null
  } finally {
    if (-not $proc.HasExited) {
      try { $proc.Kill() } catch {}
    }
    Get-Job | Where-Object { $_.Id -eq $outJob.Id } | Remove-Job -Force -ErrorAction SilentlyContinue
  }

  $world = Join-Path $dir "world"
  $outJson = Join-Path $results "parity-$Label-hash.json"
  & python $hashPy $world --radius $RadiusChunks --seed $Seed --out $outJson
  if ($LASTEXITCODE -ne 0 -and $LASTEXITCODE -ne 3) {
    Write-Host "hash failed exit=$LASTEXITCODE" -ForegroundColor Red
  }
  return $outJson
}

# Baseline: spark only (vanilla noise path)
$baseJson = Run-ParityGen -Label "baseline" -ModJars @($sparkJar) -Port $ServerPort -RPort $RconPort
# Candidate: noisiumed + spark
$nJson = Run-ParityGen -Label "noisiumed" -ModJars @($sparkJar, $noisiumedJar) -Port ($ServerPort + 1) -RPort ($RconPort + 1)

Write-Host "==== COMPARE ====" -ForegroundColor Magenta
$cmpOut = Join-Path $results "PARITY_COMPARE.json"
& python $hashPy --compare $baseJson $nJson | Tee-Object -FilePath $cmpOut
$cmp = Get-Content $cmpOut -Raw | ConvertFrom-Json

$md = Join-Path $results "PARITY_REPORT.md"
@"
# Golden section hash parity

Date: $(Get-Date -Format o)
Seed=$Seed radiusChunks=$RadiusChunks
Baseline: spark only (vanilla NoiseChunk)
Candidate: $($noisiumedJar | Split-Path -Leaf)

| | Hash |
|--|------|
| baseline overall | $($cmp.overall_a) |
| noisiumed overall | $($cmp.overall_b) |
| chunk matches | $($cmp.matches) |
| mismatches | $($cmp.mismatches) |
| **PASS** | **$($cmp.match)** |

Full: ``bench/results/PARITY_COMPARE.json``
"@ | Set-Content $md -Encoding UTF8

Write-Host "Report: $md" -ForegroundColor Green
if (-not $cmp.match) {
  Write-Host "PARITY FAIL ($($cmp.mismatches) chunks differ)" -ForegroundColor Red
  exit 2
}
Write-Host "PARITY PASS" -ForegroundColor Green
exit 0
