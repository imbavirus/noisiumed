# Golden section hash — baseline vs Fast Noise 1.0.13

Date: 2026-07-30  
Seed=12345 · radiusChunks=2 (25 chunks) · Status=minecraft:full both sides  

| | |
|--|--|
| Baseline | spark only (vanilla) |
| Candidate | `zfastnoise-1.0.13+1.21.1+neoforge.jar` |
| chunk matches | **2 / 25** |
| mismatches | **23** |
| **PASS** | **false** |

Same protocol as Noisiumed (`Run-ParityHash.ps1 -Candidate fastnoise`).

## Head-to-head (same protocol)

| Candidate | Matches / 25 | Mismatches | Notes |
|-----------|--------------|------------|--------|
| Fast Noise 1.0.13 | **2** | **23** | this run |
| Noisiumed (defaults, ore/aq off) | ~2–10 | ~15–23 | prior FULL runs |
| Noisiumed L1 off | **19** | **6** | best Noisiumed config so far |

FN does **not** match vanilla section digests either under this FULL-world harness.

## Important caveat (both mods)

Vanilla **baseline overall hashes are not stable across separate server runs** with the same seed (different `parity-baseline-hash.json` overalls in the log history). FULL chunks include **features / decoration / multi-threaded gen order**, so this test is **not pure NoiseChunk bit-identity**.

Implications:

1. FN and Noisiumed both “fail” vanilla FULL hashes — that is expected if the harness is noisy.  
2. Relative bisect **within one session** (same baseline file, progressive flags) is more trustworthy than absolute 25/25 PASS.  
3. For true noise parity we need either:  
   - hash after **noise fill only** (before features), or  
   - single-threaded gen + fixed step order, or  
   - compare Noisiumed L1 vs L0 in-process without features.

## Command

```powershell
cd bench
.\Run-ParityHash.ps1 -RadiusChunks 2 -Seed 12345 -Candidate fastnoise
.\Run-ParityHash.ps1 -RadiusChunks 2 -Seed 12345 -Candidate noisiumed
```
