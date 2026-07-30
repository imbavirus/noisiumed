#Requires -Version 5.1
<#
.SYNOPSIS
  Bisect golden section-hash parity: one baseline, many Noisiumed JVM flag combos.

.EXAMPLE
  .\Run-ParityBisect.ps1 -RadiusChunks 2 -Seed 12345
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
  [switch]$SkipBaseline
)

$ErrorActionPreference = "Stop"
$base = Join-Path $BenchRoot "server-base"
$jars = Join-Path $BenchRoot "jars"
$results = Join-Path $BenchRoot "results"
$rconPy = Join-Path $BenchRoot "rcon_cmd.py"
$hashPy = Join-Path $BenchRoot "parity_hash.py"
New-Item -ItemType Directory -Force -Path $results | Out-Null

if (-not (Test-Path $Java)) { throw "Java not found: $Java" }

$nfWinArgs = Get-ChildItem (Join-Path $base "libraries\net\neoforged\neoforge") -Recurse -Filter "win_args.txt" -ErrorAction SilentlyContinue |
  Sort-Object FullName -Descending | Select-Object -First 1
if (-not $nfWinArgs) { throw "NeoForge server-base incomplete." }
$nfArgsAt = "@" + (($nfWinArgs.FullName -replace [regex]::Escape($base), "").TrimStart('\','/') -replace '\\','/')

$sparkJar = Join-Path $jars "spark-1.10.124-neoforge.jar"
$noisiumedJar = Join-Path $jars "noisiumed-4.0.0-beta.13-neoforge-1.21.1.jar"
if (-not (Test-Path $noisiumedJar)) {
  $cands = Get-ChildItem $jars -Filter "noisiumed-4.0.0-beta*-neoforge-1.21.1.jar" | Sort-Object Name -Descending
  if ($cands) { $noisiumedJar = $cands[0].FullName }
}
if (-not (Test-Path $sparkJar)) { throw "Missing spark" }
if (-not (Test-Path $noisiumedJar)) { throw "Missing noisiumed jar" }
Write-Host "Using jar: $noisiumedJar" -ForegroundColor Cyan

function Invoke-RconLocal([int]$Port, [string]$Command) {
  & python $rconPy 127.0.0.1 $Port $RconPassword $Command
}

function Prepare-ParityDir {
  param(
    [string]$Name,
    [string[]]$ModJars,
    [int]$Port,
    [int]$RPort,
    [string]$ExtraJvm = ""
  )
  $dir = Join-Path $BenchRoot "run-parity-$Name"
  if (Test-Path $dir) { Remove-Item $dir -Recurse -Force }
  New-Item -ItemType Directory -Force -Path $dir, (Join-Path $dir "mods") | Out-Null
  $libLink = Join-Path $dir "libraries"
  cmd /c mklink /J "`"$libLink`"" "`"$(Join-Path $base 'libraries')`"" | Out-Null

  $jvm = @"
-Xms${XmxGb}G
-Xmx${XmxGb}G
-XX:+UseG1GC
-Dfile.encoding=UTF-8
$ExtraJvm
"@
  Set-Content -Path (Join-Path $dir "user_jvm_args.txt") -Value $jvm.Trim()
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

function Stop-ParityJava {
  Get-CimInstance Win32_Process -Filter "Name='java.exe'" -EA SilentlyContinue |
    Where-Object { $_.CommandLine -match 'run-parity-' } |
    ForEach-Object { Stop-Process -Id $_.ProcessId -Force -EA SilentlyContinue }
}

function Run-ParityGen {
  param(
    [string]$Label,
    [string[]]$ModJars,
    [int]$Port,
    [int]$RPort,
    [string]$ExtraJvm = ""
  )

  Stop-ParityJava
  $dir = Prepare-ParityDir -Name $Label -ModJars $ModJars -Port $Port -RPort $RPort -ExtraJvm $ExtraJvm
  $console = Join-Path $results "parity-$Label-console.log"
  if (Test-Path $console) { Remove-Item $console -Force }

  $argLine = "@user_jvm_args.txt $nfArgsAt nogui"
  $bat = Join-Path $dir "start-parity.cmd"
  @"
@echo off
"$Java" $argLine > "$console" 2>&1
"@ | Set-Content -Path $bat -Encoding ascii

  Write-Host "==== parity $Label :$Port ====" -ForegroundColor Cyan
  if ($ExtraJvm) { Write-Host "JVM extra: $ExtraJvm" -ForegroundColor DarkGray }
  $p = Start-Process -FilePath "cmd.exe" -ArgumentList "/c `"$bat`"" -WorkingDirectory $dir -PassThru -WindowStyle Hidden

  $ready = $false
  $deadline = (Get-Date).AddMinutes(12)
  while ((Get-Date) -lt $deadline) {
    if ($p.HasExited) { break }
    if (Test-Path $console) {
      $text = Get-Content $console -Raw -EA SilentlyContinue
      if ($text -and ($text -match 'Done \(' -or $text -match 'For help, type')) {
        $ready = $true
        break
      }
    }
    Start-Sleep -Milliseconds 400
  }
  if (-not $ready) {
    Stop-ParityJava
    throw "$Label never ready. See $console"
  }
  Write-Host "$Label READY" -ForegroundColor Green
  Start-Sleep -Seconds 2

  $rconOk = $false
  for ($i = 0; $i -lt 40; $i++) {
    try {
      $null = Invoke-RconLocal -Port $RPort -Command "list"
      $rconOk = $true
      break
    } catch { Start-Sleep -Seconds 1 }
  }
  if (-not $rconOk) {
    Stop-ParityJava
    throw "$Label RCON failed"
  }

  $b0 = -16 * $RadiusChunks
  $b1 = 16 * $RadiusChunks + 15
  $cmd = "forceload add $b0 $b0 $b1 $b1"
  Write-Host ">> $cmd"
  try { Invoke-RconLocal -Port $RPort -Command $cmd | Write-Host } catch { Write-Host $_ }

  # Wait until all radius chunks report Status full (poll save + hash).
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
          Write-Host "$Label chunks FULL (radius=$RadiusChunks)" -ForegroundColor Green
          break
        }
        Write-Host ("  wait full: missing={0} not_full={1} errors={2}" -f $probe.missing, $probe.not_full, $probe.errors) -ForegroundColor DarkGray
      }
    }
    Start-Sleep -Seconds 3
  }
  if (-not $fullOk) {
    Write-Host "$Label WARNING: timed out waiting for FULL chunks" -ForegroundColor Yellow
  }

  try { Invoke-RconLocal -Port $RPort -Command "save-all flush" | Out-Null } catch {}
  Start-Sleep -Seconds 3
  try { Invoke-RconLocal -Port $RPort -Command "stop" | Out-Null } catch {}
  $waited = 0
  while (-not $p.HasExited -and $waited -lt 90) {
    Start-Sleep -Seconds 1
    $waited++
  }
  Stop-ParityJava

  $world = Join-Path $dir "world"
  $outJson = Join-Path $results "parity-$Label-hash.json"
  # Suppress python stdout so PowerShell function return is a single path string.
  & python $hashPy $world --radius $RadiusChunks --seed $Seed --out $outJson | Write-Host
  return ,$outJson
}

function Compare-ToBaseline([string]$BaseJson, [string]$CandJson, [string]$Tag) {
  $cmpOut = Join-Path $results "parity-bisect-$Tag-compare.json"
  & python $hashPy --compare-a $BaseJson --compare-b $CandJson | Tee-Object -FilePath $cmpOut | Out-Null
  $cmp = Get-Content $cmpOut -Raw | ConvertFrom-Json
  [pscustomobject]@{
    Tag         = $Tag
    Match       = [bool]$cmp.match
    Matches     = [int]$cmp.matches
    Mismatches  = [int]$cmp.mismatches
    OverallA    = $cmp.overall_a
    OverallB    = $cmp.overall_b
    CompareFile = $cmpOut
  }
}

# --- configs: progressive disable (most aggressive first for isolation) ---
$configs = @(
  @{ Tag = "full";        Extra = "" },
  @{ Tag = "ore_off";     Extra = "-Dnoisiumed.fast.ore=false" },
  @{ Tag = "aq_off";      Extra = "-Dnoisiumed.fast.ore=false`n-Dnoisiumed.aquifer.specialize=false" },
  @{ Tag = "grid_off";    Extra = "-Dnoisiumed.fast.ore=false`n-Dnoisiumed.aquifer.specialize=false`n-Dnoisiumed.cell.density.grid=false" },
  @{ Tag = "spec_off";    Extra = "-Dnoisiumed.fast.ore=false`n-Dnoisiumed.aquifer.specialize=false`n-Dnoisiumed.cell.density.grid=false`n-Dnoisiumed.density.specialize=false" },
  @{ Tag = "l1_off";      Extra = "-Dnoisiumed.l1=false`n-Dnoisiumed.fast.ore=false`n-Dnoisiumed.aquifer.specialize=false`n-Dnoisiumed.cell.density.grid=false`n-Dnoisiumed.density.specialize=false" }
)

Stop-ParityJava

$baseJson = Join-Path $results "parity-baseline-hash.json"
if (-not $SkipBaseline -or -not (Test-Path $baseJson)) {
  $baseJson = Run-ParityGen -Label "baseline" -ModJars @($sparkJar) -Port $ServerPort -RPort $RconPort
} else {
  Write-Host "Reusing baseline $baseJson" -ForegroundColor Yellow
}

$rows = @()
$port = $ServerPort + 1
$rport = $RconPort + 1
foreach ($c in $configs) {
  $label = "bisect-$($c.Tag)"
  try {
    $nJson = Run-ParityGen -Label $label -ModJars @($sparkJar, $noisiumedJar) -Port $port -RPort $rport -ExtraJvm $c.Extra
    $row = Compare-ToBaseline -BaseJson $baseJson -CandJson $nJson -Tag $c.Tag
    $rows += $row
    $color = if ($row.Match) { "Green" } else { "Red" }
    Write-Host ("  {0,-12} match={1}  matches={2}  mismatches={3}" -f $row.Tag, $row.Match, $row.Matches, $row.Mismatches) -ForegroundColor $color
  } catch {
    Write-Host "FAILED $($c.Tag): $_" -ForegroundColor Red
    $rows += [pscustomobject]@{
      Tag = $c.Tag; Match = $false; Matches = -1; Mismatches = -1
      OverallA = ""; OverallB = ""; CompareFile = ""
    }
  }
}

$md = Join-Path $results "PARITY_BISECT_REPORT.md"
$lines = @(
  "# Golden hash parity bisect",
  "",
  "Date: $(Get-Date -Format o)",
  "Seed=$Seed radiusChunks=$RadiusChunks",
  "Jar: $(Split-Path $noisiumedJar -Leaf)",
  "Baseline: spark-only",
  "",
  "| Config | Match | Matches | Mismatches |",
  "|--------|-------|---------|------------|"
)
foreach ($r in $rows) {
  $lines += "| ``$($r.Tag)`` | **$($r.Match)** | $($r.Matches) | $($r.Mismatches) |"
}
$lines += ""
$lines += "### Config meanings"
$lines += "- ``full``: all optims on"
$lines += "- ``ore_off``: ``-Dnoisiumed.fast.ore=false``"
$lines += "- ``aq_off``: + aquifer specialize off"
$lines += "- ``grid_off``: + cell density grid off"
$lines += "- ``spec_off``: + density specializer off"
$lines += "- ``l1_off``: + L1 bulk path off (L0 only)"
$lines += ""
$firstPass = $rows | Where-Object { $_.Match } | Select-Object -First 1
if ($firstPass) {
  $lines += "**First passing config:** ``$($firstPass.Tag)`` - features disabled up to this point restore parity; previous step is the culprit."
} else {
  $lines += "**No config passed.** Residual: L0 palette redirect, surface mixins, biome bulk, or non-noise stages."
}
$lines -join "`n" | Set-Content $md -Encoding UTF8
Write-Host "`nReport: $md" -ForegroundColor Magenta
$rows | Format-Table -AutoSize
if ($rows | Where-Object { $_.Match }) { exit 0 } else { exit 2 }
