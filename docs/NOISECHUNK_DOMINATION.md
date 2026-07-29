# Dominating NoiseChunk (post beta.5/6 plan)

## Why this is the next war

After Phase 0–5:

| Area | Status vs Fast Noise (local harness) |
|------|--------------------------------------|
| Section **write** | **Win** (direct palette; PalettedContainer ~3× less) |
| Density **arithmetic** graph | **Win** (specializer; Ap2/transform inclusive collapsed) |
| **Forceload wall** | **Win** (~20% on bare overworld) |
| **NoiseChunk** / sample loop | **Tied or slightly behind** |
| PathMetrics L1 split | **~70% sampleBlockState / ~30% write** |

So further dominance is **not** more palette tricks. It is owning the machine that **produces** states:

```
slice fill (interpolators / fillArray)
  → selectCellYZ + cell caches
  → interpolateY/X/Z (lerp)
  → sampleBlockState / getInterpolatedState
       → finalDensity.compute + aquifer + ore veins (BlockStateFiller chain)
```

Vanilla `NoiseChunk` / yarn `ChunkNoiseSampler` is that machine. FN still walks the same API; we win around it. To **dominate NoiseChunk** we must change cost **inside** that loop.

---

## Evidence base (keep honest)

1. **sample_pct ≈ 70%** of timed L1 block work is `sampleBlockState` (beta.4+).
2. Specializer cut **vanilla DF type** time (Ap2/Mapped/transform names) but **`compute` inclusive still large** — cost moved into monomorphic `SpecDensity` + remaining noise/aquifer/interpolator nodes.
3. Spark **NoiseChunk** keyword ~parity with FN after specializer; wall win is real but not infinite.
4. L1 still does a **vanilla cell walk** in `BulkNoiseFiller` — same number of samples as vanilla/FN.

**Success metric for this campaign:** forceload wall **and** exclusive time under frames named `NoiseChunk` / `sampleBlockState` / aquifer / `fillArray` / interpolator — not MSPT alone.

---

## Threat model / parity constraints

Must preserve:

- Same block states for same seed (aquifers, ore veins, beardifier, blending).
- Same fluid tick scheduling (`needsFluidTick` + post-process marks).
- C2ME: one chunk ownership per thread; no sharing `NoiseChunk` across threads.
- Lithium: count policy unchanged.

Any batching that changes **order** of density evaluation is only OK if results are bit-identical (or proven IEEE-assoc-safe for the ops used — prefer **bit-identical**).

---

## Attack surfaces (ordered by expected ROI)

### N1 — Own the BlockStateFiller chain (highest ROI if sample dominates)

**Today:** `sampleBlockState()` → `blockStateRule.calculate(this)`:

1. Primary density → stone/air (finalDensity + beardifier)
2. Aquifer substance  
3. Optional ore vein filler  

**Ideas:**

| ID | Idea | Parity risk | Effort |
|----|------|-------------|--------|
| N1a | Detect filler list length 1–3 at NoiseChunk ctor; install **specialized monomorphic filler** (no List virtual) | Low | M |
| N1b | Split **aquifers-off** path: skip substance branch entirely when disabled | Low | S |
| N1c | Inline primary density sample into filler when primary is `SpecDensity` | Low | M |
| N1d | **Precompute primary density** for cell (4×N×4 or cellWidth³) into `double[]`, aquifer reads array | **High** (aquifer uses density + pos) | L |
| N1e | Cache last density at same NoisePos if filler re-samples (usually once) | Low | S |

**First spike:** N1a + N1b — measure exclusive `sampleBlockState` before/after.

**Integration:** mixin end of `ChunkNoiseSampler` ctor / where `blockStateSampler` is assigned; replace with `FastBlockStateSampler` holding 1–3 direct refs.

---

### N2 — Cell / slice density fill (second highest if fillArray hot)

**Today:**

- `sampleEndDensity` / `sampleStartDensity` → interpolators `fillArray` on vertical columns of cells  
- `selectCellYZ` → `CacheAllInCell` bulk fill  
- `fillAllDirectly` triple loop sets `inCell*` then `function.compute(this)`

**Ideas:**

| ID | Idea | Notes |
|----|------|-------|
| N2a | Overwrite `fillAllDirectly` with indexed loop + local field copies (no virtual size checks) | Safe micro |
| N2b | When filling a **SpecDensity** tree, call `fill` with a bulk ContextProvider that only sets index (already partial) | Medium |
| N2c | Specialize **NoiseInterpolator.fillArray** when wrapped function is SpecDensity / Noise | Medium |
| N2d | For `CacheAllInCell`, keep values in structure-of-arrays friendly layout | Larger |
| N2e | Reduce `arrayInterpolationCounter` / cache-once invalidation thrash if safe | Needs proof |

**First spike:** N2a + exclusive time on `fillAllDirectly` / interpolator `fillArray`.

---

### N3 — Interpolator update path (already partially done)

**Done (beta.4):** inline lerp on `DensityInterpolator`.

**Remaining:**

| ID | Idea |
|----|------|
| N3a | Replace `List.forEach` in `interpolateY/X/Z` / `onSampledCellCorners` with indexed loop + invoker array |
| N3b | Cache `interpolators` as `DensityInterpolator[]` array once after construction (avoid List get) |
| N3c | Unroll for 1–4 interpolators (common overworld counts) |

Expected gain: **smaller** than N1/N2 once lerp is inlined; still free if N3b is clean.

---

### N4 — Aquifer (large when caves enabled)

**Today:** every non-air/air decision may call `Aquifer.computeSubstance(pos, density)`.

**Ideas:**

| ID | Idea | Risk |
|----|------|------|
| N4a | Profile aquifer-off vs aquifer-on seeds separately | — |
| N4b | Fast-path when density is deep solid / deep air (vanilla already has thresholds — mirror carefully) | Med |
| N4c | ThreadLocal scratch inside aquifer if it allocates | Low |
| N4d | Do **not** rewrite aquifer fluid math without golden grids | High |

Only invest after N1/N2 if aquifer frames ≥15–20% exclusive.

---

### N5 — Structural: “cell batch L1” (moonshot)

Replace per-block sample with:

1. For each cell YZ after corners ready:  
   - bulk primary density for all sub-blocks into `double[cellW*cellH*cellW]`  
2. Second pass: aquifer + materialize from density + pos  

Requires **custom BlockStateFiller** that reads prefilled density — mixin into NoiseChunk construction of the aquifer lambda:

```text
ctx -> aquifer.computeSubstance(ctx, density.compute(ctx))
// becomes
ctx -> aquifer.computeSubstance(ctx, densityGrid[index(ctx)])
```

**Parity:** grid index must match vanilla sample order for CacheOnce/interpolated state.

**Gate:** only after N1a/N2a land and parity harness exists.

---

### N6 — Measurement upgrades (mandatory before moonshots)

| ID | Deliverable |
|----|-------------|
| N6a | Spark parse: exclusive top frames filter `NoiseChunk|Aquifer|sampleBlockState|fillArray|Interpolator|SpecDensity` |
| N6b | PathMetrics: optional `sample_ns` always-on under `-Dnoisiumed.detail.timing=true` |
| N6c | Golden parity: fixed seed, radius 2, hash of section palettes vs vanilla (or vs beta.5) |
| N6d | Separate benches: aquifers on (default), aquifers off datapack, ore veins on |

Without N6c, N1d/N5 are too dangerous.

---

## Recommended campaign (milestones)

### Milestone NC-1 — “Own the sampler” — **shipped beta.7**

1. ~~N1a monomorphic BlockStateFiller (list 1–3)~~ → `FastSingle/Dual/TripleBlockSampler` after `ChunkNoiseSampler` ctor  
2. ~~N3b interpolator array cache~~ → indexed `interpolateY/X/Z`  
3. Metrics: `nc1_s1/s2/s3` (overworld typically **nc1_s2** with ore veins)  
4. Note: aquifers-off short path still TODO (N1b); sample still ~65% of timed L1 block work  

**Exit:** dual-sampler monomorphy active; wall competitive with beta.5; next is NC-2 fillArray / NC-3 density grid.

### Milestone NC-2 — “Own fillArray” — **shipped beta.8**

1. ~~N2a fillAllDirectly~~ → overwrite `ChunkNoiseSampler#fill` (tight triple loop)  
2. ~~Indexed `onSampledCellCorners`~~ → interpolator corners + `CellCache` bulk fills  
3. N2c SpecDensity-aware interpolator fill still optional  

**Exit:** boots clean; `nc1_s2` + NC-2 mixins active; sample still ~70% of L1 (NC-3 next for real sample drop).

### Milestone NC-3 — “Cell density grid” — **shipped beta.9**

1. Capture last `CACHE_ALL_IN_CELL` wrap → `CellCache` double[]  
2. `sampleBlockState` = index cache + `AquiferSampler.apply` + optional ore secondary  
3. Flag: `-Dnoisiumed.cell.density.grid=false` to disable  
4. Result: **sample_pct ~62–63%** (was ~70–77%); wall still FN-competitive  

**Next pressure:** aquifer internals / ore-vein density / remaining fillArray (NC-4).

### Milestone NC-4 — Aquifer polish (only if needed)

N4\* after exclusive profiles demand it.

---

## What not to do

1. More leaf DF mixins without specializer coverage metrics.  
2. Chasing inclusive `NoiseChunk` % while wall regresses.  
3. Copying Fast Noise NoiseChunk rewrites.  
4. Cell-batch without parity harness.  
5. Assuming specializer already “solved” sample — **70% sample_pct remains**.

---

## Concrete code map (current tree)

| Piece | Path |
|-------|------|
| L1 loop | `noise/BulkNoiseFiller.java` |
| sample invoke | `mixin/ChunkNoiseSamplerAccessor` |
| DF specializer | `density/special/*` + `ChunkNoiseSamplerSpecializeMixin` |
| Interpolator lerp | `mixin/noisechunk/DensityInterpolatorMixin` |
| Write | `noise/DirectSectionWriter` |
| Column bits (for later surface/height) | `attach/ChunkGenAttachment` |
| Metrics | `path/PathMetrics` |

**New packages (proposed):**

- `noise/sampler/FastBlockStateSampler.java`  
- `mixin/noisechunk/ChunkNoiseSamplerFillerMixin.java`  
- `mixin/noisechunk/ChunkNoiseSamplerFillAllMixin.java`  
- `bench/parity_hash.py` or Java gametest  

---

## How we know we “dominate”

All of:

1. Forceload wall **≥10% faster than FN** on aquifers-on **and** aquifers-off seeds (not one lucky seed).  
2. Exclusive `sampleBlockState` + `NoiseChunk` **below FN** on the same Spark protocol.  
3. Parity harness green.  
4. `l1_fail=0` and no surface/heightmap regressions.

---

## One-line strategy

**NoiseChunk domination = monomorphic BlockStateFiller + cheaper cell fillArray + (later) density grids for aquifer — measured by exclusive sample time and wall clock, guarded by parity.**
