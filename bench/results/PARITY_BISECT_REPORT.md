# Golden hash parity bisect

Seed=12345 · radiusChunks=2 (25 chunks) · jar beta.12  
Baseline = spark-only vanilla NoiseChunk · both sides polled to **Status=minecraft:full**

## Round 1 (progressive disable; older wait — treat as directional)

| Config | Matches | Mismatches |
|--------|---------|------------|
| full (all on) | 4 | 21 |
| ore_off | **16** | **9** |
| aq_off (+ore) | **18** | **7** |
| +grid_off | 13 | 12 |
| +spec_off | 10 | 15 |
| +l1_off | 9 | 16 |

**Signal:** FastOre is the largest single hit; aquifer specialize smaller.

## Round 2 (FULL wait; defaults ore/aq **off**)

| Config | Matches | Mismatches | Extra JVM |
|--------|---------|------------|-----------|
| default (L1+spec+grid on) | 10 | 15 | (none) |
| **l1_off** | **19** | **6** | `-Dnoisiumed.l1=false` |
| grid_off | 18 | 7 | `-Dnoisiumed.cell.density.grid=false` |
| spec_off | 3 | 22 | `-Dnoisiumed.density.specialize=false` |
| l1+spec+grid off | 2 | 23 | all three off |

## Culprits (ordered)

1. **FastOreVeinSampler** (re-specialize of ore densities) — default **OFF**  
2. **Aquifer density specialize** — default **OFF**  
3. **L1 bulk path** (`BulkNoiseFiller` / direct write) — largest residual after 1–2 (~9 chunks)  
4. **NC-3 cell density grid** — almost as large as L1 when L1 is on (~8 chunks vs default)  
5. Residual ~6 chunks with L1 off → L0 palette redirect, density *mixins* (not specializer), interpolator, surface  

## Actions taken

| Change | Status |
|--------|--------|
| `-Dnoisiumed.fast.ore` default **false** | shipped |
| `-Dnoisiumed.aquifer.specialize` default **false** | shipped |
| Ore no longer re-specializes wrapped DFs | shipped |
| Kill switches `noisiumed.l1`, `noisiumed.density.specialize` | shipped |
| FULL-status wait in parity harness | shipped |
| `Run-ParityBisect.ps1` | shipped |

## Next code targets (parity, not wall)

1. Diff L1 vs L0 on a single mismatched chunk (section Y palette names)  
2. Fix NC-3 grid index / sample order if grid_off recovers most of L1 gap  
3. Audit L0 `setBlockState` redirect + deferred heightmaps vs vanilla post-process  

```powershell
cd bench
.\Run-ParityBisect.ps1 -RadiusChunks 2 -Seed 12345
.\Run-ParityHash.ps1 -RadiusChunks 2 -Seed 12345
```
