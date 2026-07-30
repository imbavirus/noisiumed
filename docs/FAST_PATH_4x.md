# Noisiumed 4.x fast path

## Primary target

**Minecraft 1.21.1 / NeoForge 21.1.x** (Infernos pack pin).

## Tiers

| Tier | Behaviour |
|------|-----------|
| **L0** | Vanilla `populateNoise` + direct palette write (safe after resize) |
| **L1** | Direct palette writes during noise loop (no staging materialize), aquifer fluid ticks kept |
| **Biomes** | Section bulk pack + fixed-biome chunk shortcut |
| **L2** | Defer WG heightmaps until after `buildSurface` (or empty-surface skip) |

## L1 write path (beta.3+)

1. Cell walk still vanilla (`sampleBlockState` / aquifer / interpolators).  
2. Non-air results go to **`DirectSectionWriter`**: `palette.index` → re-read `data` → `storage.set`.  
3. Identity last-state cache for solid runs.  
4. Counts: inline (non-Lithium) or `calculateCounts()` (Lithium).  
5. Optional L2 heightmap deferral (default on).

Legacy `StagingSection` (4→8 adaptive pack + materialize) remains in tree but is **not** on the L1 hot path.

## L2 heightmap deferral

`BulkNoiseFiller.DEFER_HEIGHTMAPS_UNTIL_SURFACE = true` (default):

1. L1 fills blocks, marks `ChunkGenAttachment` heightmaps pending  
2. Surface runs (or is skipped if all empty)  
3. `DeferredHeightmaps.populateAfterNoise` runs once  

## Metrics (huge-win measurement)

`PathMetrics.snapshot()` / `Noisium.pathMetricsSnapshot()`:

- `l0` / `l1` / `l2` / `l1_fail` / `biome` / `surface_skip`
- `l1_avg_us` — mean L1 populate wall time
- `direct_writes` / `sections_touched`
- `l0_reasons{…}` — why L0 was selected / fallthrough

### How we measure “huge” wins

Prefer **forceload wall time** and **exclusive Spark** frames over MSPT-only or inclusive keyword totals.

Harness: `bench/Run-WorldgenSparkCompare.ps1` records boot/forceload/profile wall clocks and Spark URLs; downloads sampler bins when network allows.

### Phase 2A finding (beta.4)

On bare overworld L1, sampled timing shows roughly **~70% sampleBlockState vs ~30% direct writes**. Further huge wins must attack density/NoiseChunk sampling (specializer / cell path), not section packing.

## Density

- **beta.2** — leaf mixins (holder cache, binary/unary/linear/range)
- **beta.5** — **specializer**: after `getActualDensityFunctionImpl`, rewrite Unary/Linear/Binary to monomorphic `SpecDensity` types (Abs, MulConst, Add, …)

## Surface / coverage (beta.6)

- L1 attaches **column bits** + **has-solid**; empty L1 chunks skip `buildSurface` without section scan  
- L1 generator allowlist: `-Dnoisiumed.l1.generator.allowlist=fqcn1,fqcn2`

## beta.16 notes

- Nested `DensityScratch` temps (fix for Spec/Ap2 Add fill under deep trees).
- Clamp → SpecDensity.
- Deep CellCache/interpolator delegate specialize is **opt-in** (`-Dnoisiumed.density.deep=true`) — parity-sensitive.
- Multi vs FN still ~tie; sample still dominates L1 (~69%).

## Next campaign

See **[NOISECHUNK_DOMINATION.md](./NOISECHUNK_DOMINATION.md)** — sample path / BlockStateFiller / cell fillArray / deep specialize parity.

## Out of scope (still)

- Noise sampler / Perlin rewrite  
- Full material-rule surface compiler  
- Aquifer math rewrite without golden parity
