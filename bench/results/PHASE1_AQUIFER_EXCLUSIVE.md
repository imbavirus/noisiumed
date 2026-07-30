# Phase 1: Aquifer exclusive (Spark inclusive ms) — 2026-07-30

Parsed with `bench/parse_spark_worldgen.py --aquifer`.

## β15 pair (HIZKSMOjQN vs kJhdhwjEOT)

| Frame | Noisiumed | Fast Noise |
|-------|-----------|------------|
| Aquifer sum (related) | **28512** | 31276 |
| computeSubstance | 17828 | 18872 |
| getAquiferStatus | 5216 | 6056 |
| computeFluid | 5192 | 6016 |
| sampleBlockState (N only) | 39516 | — |
| getInterpolatedState | 39824 | 42240 |
| populate (Bulk / Fast) | **118724** | **106048** |
| buildSurface | 39228 | 37960 |
| SurfaceSystem.buildSurface | 19336 | 18964 |

## β16.2 multi run1 (UiR7Kz3pEl vs NEnlzCTP8l)

| Frame | Noisiumed | Fast Noise |
|-------|-----------|------------|
| Aquifer sum | **26672** | 30696 |
| computeSubstance | 16656 | 18572 |
| sampleBlockState | 37856 | — |
| populate | **112660** | **104824** |
| buildSurface | 37192 | 37192 |

## Findings

1. **Aquifer is real (~24% of BulkNoiseFiller inclusive)** — worth polish, not the whole wall gap.
2. **We already beat FN slightly on aquifer sum** (solid early-out / NC-3 helping).
3. **FN still wins populate inclusive** (~106k vs ~113–119k) — that matches quiet forceload behind FN.
4. **buildSurface ≈ 37–39k both** — second wall slice; surface_skip still 0 on thrash seed.
5. **sampleBlockState ~38–40k** of which aquifer ~17–18k (~45% of sample frame) — rest is ore + glue.

## Next wall actions (priority)

| Priority | Action | Expected wall |
|----------|--------|----------------|
| P1a | Aquifer: reduce getAquiferStatus/computeFluid alloc (profile objects) | small (−1–2%) |
| P1b | Do **not** expect aquifer alone to hit −10% | — |
| P2 | Surface exclusive / skip strategy | medium if ≥15% exclusive real |
| P3 | Residual sample after aquifer (ore + grid path) | medium |

Phase 0 harness now reports quiet median so aquifer wins aren’t masked by FN spikes.
