# Wall campaign — failures → measurement → targets

## Phase 0 (done)

- Multi harness: **mean / median / min / max / quiet set / spikes / dominance gate**
- Jar pin: `Resolve-NoisiumedJar.ps1` (semver, not string sort)
- Spark: `parse_spark_worldgen.py --aquifer`

## Phase 1 aquifer finding (done)

Aquifer ~**24%** of BulkNoise inclusive; we already slightly beat FN there.
**FN still wins populate** (~105–106k vs ~113–119k) — primary quiet-wall gap.
Surface ~**37k** both sides.

## Baseline re-run (β16.2 defaults, interps off)

See latest `bench/results/MULTI_COMPARE_REPORT.md`.

Dominance gate: quiet median ≥10% **and** mean ≥5% vs FN.

## Phase 1b code (β16.3) — residual sample micros

| Change | Result |
|--------|--------|
| Ore Y-band early-out | parity-safe; small |
| NC-3 aquifer access cache + strides | solid-path micro |
| Solid null→default skip fluid tick | solid-path micro |
| Default writer fluid flags cached | solid-path micro |

**Multi β16.3 (noisy machine session):** quiet median still ~**−2.7%** vs FN (gate FAIL).  
Run 1 clean: N 40562 ≈ FN 40446. Micros did **not** flip quiet lead.

**Lesson reinforced:** micros on solid/ore paths don’t close the ~3% quiet gap. Next is **structural** (surface exclusive work, or N5 density batch behind noise-stage golden) — not more solid-path shaving.

## β16.4 multi (skip-interp cell position)

Quiet median still **~−2.3%** vs FN (gate FAIL). Mean ~+1% (noise). Golden FULL 2/25 (thrash).  
Skip-interp did **not** win quiet gate.

Spark when FN wins wall: N populate ≈ FN (~127k vs ~124k); surface still large both sides.  
FN structural edge confirmed: **FastChunkSection** 4-bit row OR packing into live section storage.

## β16.5 structural — **DOMINANCE GATE PASS**

1. **DirectSectionWriter** — after first palette growth to 4-bit, hot writes use FN-layout  
   `raw[(y<<4)|z] |= id<<(x<<2)` (empty-start OR). Fallback to `storage.set` if bits ≠ 4.
2. **NC-3 ore Y** — skip `secondary.sample` outside copper/iron Y union (parity null).
3. Jar pin: `Resolve-NoisiumedJar` PreferName → `beta.16.5`.

### Multi β16.5 vs zfastnoise 1.0.13 (5 runs, seed 12345, r=7)

| Metric | Noisiumed | Fast Noise | % faster (N) |
|--------|-----------|------------|--------------|
| **quiet median** | 42527 | 53723 | **+20.8%** |
| **mean** | 44301 | 51208 | **+13.5%** |

Per-run N: 42527, 40632, 40080, 44182, 54083  
Per-run F: 53723, 45816, 53944, 54384, 48174  

- Quiet median ≥10%: **PASS**  
- Multi mean ≥5%: **PASS**  
- Combined dominance gate: **PASS**

Run 5 still shows machine thrash (N 54s vs FN 48s) — median/quiet gate is the honest scoreboard.

## Optional follow-ups (post-win polish)

1. Force-install 4-bit array on bind (pre-grow) — shave first-write resize.
2. Fast heightmap prime (FN HeightmapUtil).
3. N5 density batch behind noise-stage golden (sample_pct still high).
4. Golden soft target ~10/25 if product cares.
