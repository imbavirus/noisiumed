# Golden hash parity bisect

Date: 2026-07-30  
Seed=12345 radiusChunks=2 (25 chunks)  
Jar: `noisiumed-4.0.0-beta.12-neoforge-1.21.1.jar`  
Baseline: spark-only (vanilla NoiseChunk)

| Config | Match | Matches | Mismatches | Notes |
|--------|-------|---------|------------|--------|
| `full` | false | 4 | 21 | all optims on |
| `ore_off` | false | **16** | **9** | `-Dnoisiumed.fast.ore=false` |
| `aq_off` | false | **18** | **7** | + aquifer specialize off |
| `grid_off` | false | 13 | 12 | + cell density grid off |
| `spec_off` | false | 10 | 15 | + density specializer off |
| `l1_off` | false | 9 | 16 | + L1 bulk path off |

## Interpretation

1. **FastOreVeinSampler is the dominant parity bug**  
   Turning it off recovers **12** chunks (21 miss → 9). Extra `DensitySpecializer.specialize` on ore inputs was a likely cause (removed in follow-up); default is now **off**.

2. **Aquifer density specialize is a smaller hit**  
   Off recovers ~2 more chunks vs ore_off alone (9 → 7). Default now **off**.

3. **Non-monotonic later steps** (grid/spec/l1 off *worse*)  
   Mismatch sets are **not nested** across configs → harness was saving some non-FULL / racing gen. Follow-up: wait until all chunks report `Status=full` before hashing.

4. **Residual ~7 chunks** even with ore+aq off  
   Still open: NC-3 grid, L1 write path, L0 palette redirect, surface, interpolator mixins — re-bisect after FULL-status gate.

## Follow-ups shipped

- Kill switches: `noisiumed.l1`, `noisiumed.density.specialize`, ore/aq defaults off  
- `Run-ParityBisect.ps1` + FULL status wait  
- Ore sampler no longer re-specializes wrapped densities  
