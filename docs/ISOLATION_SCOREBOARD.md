# Isolation scoreboard vs champion β16.5

**No LLM runtime** — systems-strategy experiments only.  
**Rule:** one branch per experiment from tag `champion-16.5`.

**Harness:** multi ×3 triage (prefer ×5 when cold); seed 12345, r=7, profile 100s.  
**Process policy:** stop only bench ports 25570–25576 / run dirs — **never other MC servers**.

## CONTROL (champion-16.5)

| Field | Value |
|-------|--------|
| Tag | `champion-16.5` / `4.0.0-beta.16.5` |
| Quiet median wall_forceload_ms | **42527** |
| Mean wall_forceload_ms | **44301** |
| vs FN (historical ×5) | quiet **+20.8%** / mean **+13.5%** |

Primary: **Δ% quiet median vs CONTROL** (positive = faster than champion).

| Decision | Quiet med Δ% vs champ |
|----------|----------------------|
| ADOPT | ≥ +5% |
| FLAG_DEFAULT_OFF | +2–5% |
| REJECT | &lt; +2% or slower / no-op |
| NEEDS_REWORK | noisy multi / contaminated |
| DEFERRED | design only this session |

### Session caveat (important)

Later isolations often ran **hot** (N walls 50–90s). Absolute Δ vs cold CONTROL overstates regressions. Prefer same-session FN and a **cold CONTROL v2** before shipping.

---

## RESCORE 2026-07-30/31 (multi ×5, sequential, hardened harness)

**Method:** one long session, order champion → W4 → W1 → W6. Same seed/r/profile. PreferName pin + ReportSuffix. Primary score = Δ quiet median N wall **vs session champion jar** (not cold CONTROL). Secondary = same-suite FN.

**CONTROL_SESSION** (`MULTI_COMPARE_REPORT_champion.md`):

| Metric | Noisiumed | Fast Noise | % faster N vs F |
|--------|-----------|------------|-----------------|
| quiet median wall | **52981** | 49436 | **−7.2%** |
| quiet mean | 51274 | 49760 | −3.0% |

Cold historical champion (quiet med **42527**, **+20.8%** vs FN) did **not** reproduce this session — machine/FN thrash. Ship decisions for isolation still use **relative N vs session champ**.

| Exp | Jar | Quiet med N | Δ% vs session champ | Quiet med FN | % N vs FN | Decision |
|-----|-----|-------------|---------------------|--------------|-----------|----------|
| **CONTROL** | β16.5 | **52981** | 0 | 49436 | −7.2% | baseline |
| **W4** | β16.5-w4 | **55306** | **−4.4%** (slower) | 55367 | +0.1% | **REJECT** (rescore) |
| **W1** | β16.5-w1 | **55852** | **−5.4%** (slower) | 55571 | −0.5% | **REJECT** (this impl) |
| **W6** | β16.5-w6 | **45242** | **+14.6%** (faster N) | 44172 | −2.4% | **FLAG** — best N of suite; still ≤ FN |

Δ% = `(CONTROL_SESSION − N) / CONTROL_SESSION` (positive = faster than session champion).

### Rescore interpretation

1. **W4 / W1 do not beat session champion N.** Prior W4 FLAG (+2.9% vs cold control ×3) does **not** hold under ×5 same-session control. Do not default-on pregrow or solid-cell as currently built.
2. **W1 solid-cell** is cleanly measurable now (`sample_pct` ~91%, `write_pct` ~8%) but **adds wall** vs champion — speculation path cost exceeds skip benefit on thrash OW seed, or solid-cell hit rate is too low without secondary-only ore skip.
3. **W6** is the only merit candidate: quiet N **~14.6% under session champ** and lowest N stdev band of the four suites. Caveat: FN also sped up in the W6 window (FN quiet 44.2k vs 49.4k earlier) — absolute non-stationarity. Still **lost to same-session FN by 2.4%**. Keep **FLAG** (parity-safe micro); cold re-confirm before stack.
4. None of the merit jars re-passed the **domination gate vs FN** this session (need quiet ≥10% / mean ≥5%). Historical cold pass remains champion-only evidence.

Reports: `bench/results/MULTI_COMPARE_REPORT_{champion,w4,w1,w6}.md`. Harness commit `106611e`.

---

## W6v2 follow-up 2026-07-31 (branch `exp/w6-v2`)

**Code:** same parity-safe ore-chance→gap short-circuit + opt-in `-Dnoisiumed.oreStats=true` funnel (`FastOreVeinSampler`). Jar `4.0.0-beta.16.5-w6v2`. Commit `ef9acd9`.

**Same-session multi ×5** (champ first, then w6v2):

| Suite | Quiet med N | Quiet med FN | % N vs FN | Δ% N vs session champ |
|-------|-------------|--------------|-----------|------------------------|
| CONTROL (`w6v2ctrl`) | **52251** | 54021 | **+3.3%** | 0 |
| W6v2 | **65391** | 63307 | **−3.3%** | **−25%** (slower abs) |

Samples N: champ `43953…59062` (med 52251); w6v2 `58461…66533` (med 65391). FN also drifted +17% quiet (54021→63307) — **thermal/session thrash**, not a clean isolation signal.

### Decision

| Item | Verdict |
|------|---------|
| W6v2 wall vs champ (this session) | **NO ADOPT** — absolute + relative-to-FN both worse after heat soak |
| W6 code quality | Still **parity-safe free micro**; keep as FLAG candidate |
| oreStats | Usable tooling; default off (static final) |
| Next | Cold **interleaved** champ/w6v2 (or A/B per run) to kill order bias; enable oreStats once for funnel % |

Reports: `MULTI_COMPARE_REPORT_w6v2ctrl.md`, `MULTI_COMPARE_REPORT_w6v2.md`.

---

## Full isolation table (triage history + rescore override)

| Exp | Branch | Quiet med (N) | Δ% vs champ | vs FN (session) | Decision | Notes |
|-----|--------|---------------|-------------|-----------------|----------|--------|
| **W4** | `exp/w4-pregrow-4bit` | rescore **55306** | **−4.4%** sess | +0.1% | **REJECT** rescore | Prior cold ×3 FLAG (+2.9%) superseded by ×5 same-session. |
| **W1** | `exp/w1-spec-cells` | rescore **55852** | **−5.4%** sess | −0.5% | **REJECT** this impl | Clean multi; no wall win. Rework needs secondary-only ore + counters. |
| **W6** | `exp/w6-secondary-lut` / `exp/w6-v2` | rescore **45242** / w6v2 **65391** | **+14.6%** then **−25%** thrash | −2.4% / −3.3% | **FLAG, no ship yet** | Prior rescore promising; w6v2 same-session follow-up thermal — need interleaved cold. |
| **W2** | `exp/w2-cell-batch` | 54458 | −28% cold | −1.7% | **REJECT** (hot) | Density-cache materialize. No proven gain. |
| **W3** | `exp/w3-path-moe` | 52948 | −25% cold | bad | **REJECT** default OW | Fluid-tick MoE no-op with aquifers on. |
| **W5** | `exp/w5-arena-scratch` | 46833 | −10% cold | ~0–13%* | **REJECT** vs control | Scratch buckets. FN spike distorted session. |
| **W8** | `exp/w8-heightmap-prime` | 56141 | −32% cold | −2.2% | **REJECT** thrash seed | Skip empty heightmap prime; `surface_skip=0` → no work. |
| **W7** | `exp/w7-aquifer-cache` | — | — | — | **DEFERRED** | Design notes only (`docs/EXP_W7_NOTES.md`). |
| **W9** | `exp/w9-turbo-preset` | — | — | — | **DEFERRED** | Only stack after W6 cold confirm; do not include W1/W4 as-is. |

\*Older thrash rows retained for history; **rescore rows are authoritative for W1/W4/W6**.

---

## Ranking (post-rescore)

1. **Keep champion 16.5** as production wall baseline (historical cold +20.8% vs FN).  
2. **W6 only FLAG** worth stacking next — parity-safe ore gap short-circuit; cold multi before merge.  
3. **W4 / W1 REJECT** as currently implemented (rescore slower than session champ).  
4. W2/W3/W5/W8: no ship for wall on default thrash overworld.  
5. W7 design; W9 turbo = champion + W6 (if cold OK) only — not W1/W4.

---

## What to improve (see also `docs/ISOLATION_LEARNINGS.md`)

1. ~~Cold re-baseline CONTROL + multi ×5 W1/W4/W6 same session~~ → **done (hot session; CONTROL_SESSION 52981)**. Optional true-cold repeat.  
2. Harness: unique console logs, port-based stop, PreferName, ReportSuffix — **done** (`106611e`).  
3. Cold re-run **W6 only** when machine quiet; if still FLAG vs champ → stack into W9 design.  
4. Aquifers-off multi for W3.  
5. Real heightmap prime (column scan) for W8 — empty skip is dead on thrash seed.  
6. W1 rework only if `cells_speculated` counter shows high hit rate + secondary-only ore.  

---

## Remote branches

| Branch | Content |
|--------|---------|
| `champion-16.5` | tag |
| `exp/w1-spec-cells` | solid cell speculation + harness fixes |
| `exp/w2-cell-batch` | density materialize |
| `exp/w3-path-moe` | fluid-tick MoE |
| `exp/w4-pregrow-4bit` | 4-bit pregrow |
| `exp/w5-arena-scratch` | DensityScratch buckets |
| `exp/w6-secondary-lut` | ore gap short-circuit |
| `exp/w7-aquifer-cache` | design notes |
| `exp/w8-heightmap-prime` | empty heightmap skip |
| `exp/w9-turbo-preset` | turbo design notes |
