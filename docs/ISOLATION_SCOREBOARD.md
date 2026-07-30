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

## Full isolation table

| Exp | Branch | Quiet med (N) | Δ% vs champ | vs FN (session) | Decision | Notes |
|-----|--------|---------------|-------------|-----------------|----------|--------|
| **W4** | `exp/w4-pregrow-4bit` | **41277** | **+2.9%** | −2.3% | **FLAG** | Clean ×3. Pregrow 4-bit palette. Best isolation win. |
| **W1** | `exp/w1-spec-cells` | messy | noisy | mixed | **NEEDS_REWORK** | Solid-cell speculation. Best idea; multi locks. Re-run cold. |
| **W2** | `exp/w2-cell-batch` | 54458 | −28% | −1.7% | **REJECT** (hot) | Density-cache materialize. No proven gain. |
| **W3** | `exp/w3-path-moe` | 52948 | −25% | bad | **REJECT** default OW | Fluid-tick MoE no-op with aquifers on. |
| **W5** | `exp/w5-arena-scratch` | 46833 | −10% | ~0–13%* | **REJECT** vs control | Scratch buckets. FN spike distorted session. |
| **W6** | `exp/w6-secondary-lut` | ~45–48k quiet* | ~−6% | **+17%** quiet* | **FLAG micro / re-bench** | Skip veinGap when ore-chance fails (parity). Hot session; same-session FN win. |
| **W8** | `exp/w8-heightmap-prime` | 56141 | −32% | −2.2% | **REJECT** thrash seed | Skip empty heightmap prime; `surface_skip=0` → no work. |
| **W7** | `exp/w7-aquifer-cache` | — | — | — | **DEFERRED** | Design notes only (`docs/EXP_W7_NOTES.md`). |
| **W9** | `exp/w9-turbo-preset` | — | — | — | **DEFERRED** | Design notes only (`docs/EXP_W9_NOTES.md`). |

\*W5/W6 quiet numbers from thrashy sessions; do not ship on control Δ alone.

---

## Ranking (honest)

1. **Keep champion 16.5** as production wall baseline.  
2. **W4** only clean FLAG vs cold control — re-confirm ×5 cold, then optional default-on.  
3. **W1** still top ROI idea — **highest priority re-bench**.  
4. **W6** parity-safe micro — likely worth stacking after cold confirm.  
5. W2/W3/W5/W8: no ship for wall on default thrash overworld.  
6. W7/W9: next engineering, not multi-ready.

---

## What to improve (see also `docs/ISOLATION_LEARNINGS.md`)

1. Cold re-baseline CONTROL + multi ×5 W1/W4/W6 same session.  
2. Harness: unique console logs (done), port-based stop (done), avoid `Remove-Item` throw on lock.  
3. PreferName always from built jar.  
4. Aquifers-off multi for W3.  
5. Real heightmap prime (column scan) for W8 — empty skip is dead on thrash seed.  

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
