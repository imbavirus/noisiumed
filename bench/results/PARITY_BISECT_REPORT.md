# Golden hash parity bisect

Date: 2026-07-30T03:49:22.8000155+02:00
Seed=12345 radiusChunks=2
Jar: noisiumed-4.0.0-beta.13-neoforge-1.21.1.jar
Baseline: spark-only

| Config | Match | Matches | Mismatches |
|--------|-------|---------|------------|
| `l1_on` | **False** | 6 | 19 |
| `l1_off` | **False** | 5 | 20 |

### Config meanings
- `full`: all optims on
- `ore_off`: `-Dnoisiumed.fast.ore=false`
- `aq_off`: + aquifer specialize off
- `grid_off`: + cell density grid off
- `spec_off`: + density specializer off
- `l1_off`: + L1 bulk path off (L0 only)

**No config passed.** Residual: L0 palette redirect, surface mixins, biome bulk, or non-noise stages.
