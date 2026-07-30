# Isolation scoreboard vs champion β16.5

**No LLM runtime** — systems-strategy experiments only.  
**Rule:** one branch per experiment from tag `champion-16.5`.

**Harness:** multi ×3 (time budget), seed 12345, r=7, profile 100s.  
**Bench process policy:** only stop java under bench run dirs / ports 25570–25576 (never other MC servers).

## CONTROL (champion-16.5)

| Field | Value |
|-------|--------|
| Tag | `champion-16.5` / `4.0.0-beta.16.5` |
| Quiet median wall_forceload_ms | **42527** |
| Mean wall_forceload_ms | **44301** |
| vs FN (historical win multi ×5) | quiet +20.8% / mean +13.5% |
| N per-run (control) | 42527, 40632, 40080, 44182, 54083 |

Primary column: **Δ% quiet median vs CONTROL** (positive = faster than champion).

| Decision | Quiet med Δ% vs champ | Notes |
|----------|----------------------|--------|
| ADOPT | ≥ +5% | ship candidate |
| FLAG_DEFAULT_OFF | +2–5% | keep optional |
| REJECT | &lt; +2% or slower | do not merge for wall |
| NEEDS_REWORK | noisy multi / failed runs | re-bench when machine quiet |

---

## Measured isolations

| Exp | Branch | Quiet med (N) | Mean (N) | Δ% quiet vs champ | vs FN quiet | Decision | Notes |
|-----|--------|---------------|----------|-------------------|-------------|----------|--------|
| **W4** | `exp/w4-pregrow-4bit` | **41277** | 41542 | **+2.9%** | −2.3% (FN ~40k that day) | **FLAG_DEFAULT_OFF** | Pre-grow 4-bit palette on bind. Clean ×3 multi. Small write-path win. |
| **W1** | `exp/w1-spec-cells` | ~53k (n=2 messy) | ~53k | **noisy / worse** | mixed | **NEEDS_REWORK** | All-solid cell skip. File-lock failures mid multi; one clean ~40.5s run. Re-run when quiet. |
| **W2** | `exp/w2-cell-batch` | **54458** | 51206 | **−28%** (slower) | −1.7% | **REJECT** (this session) | Density-cache materialize. Machine hot (~54s both sides); no proven gain. |
| **W3** | `exp/w3-path-moe` | **52948** | 50021 | **−25%** (slower) | −30% | **REJECT** on default OW | Fluid-tick MoE only helps aquifers-**off**; thrash seed always Impl. |

### Session caveat

Later multi sessions (W1–W3) saw **much higher absolute walls** (~50–65s) than champion control (~40–42s quiet). Treat Δ vs control carefully; prefer **same-session FN** and **re-baseline champion** on a cold machine before shipping any ADOPT.

---

## Not fully multi-scored yet (code plan remaining)

| Exp | Branch (planned) | Idea | Status |
|-----|------------------|------|--------|
| W5 | `exp/w5-arena-scratch` | Paged density scratch / zero alloc fill | **pending** implement+multi |
| W6 | `exp/w6-secondary-lut` | Secondary noise / LUT only | **pending** (ore Y already in champion) |
| W7 | `exp/w7-aquifer-cache` | Aquifer status lattice reuse | **pending** high risk |
| W8 | `exp/w8-heightmap-prime` | FN-style heightmap prime | **pending** |
| W9 | `exp/w9-turbo-preset` | Non-parity turbo (loose ε + W1+W4) | **pending** product path |

---

## Ranking so far (honest)

1. **Champion 16.5 remains the wall king** on the clean historical multi.  
2. **W4 pregrow** is the only isolation with a **clean positive** quiet-med vs control (~+3%) → flag default off, re-check on cold multi ×5.  
3. **W1** still the best *idea* for large wins; multi was contaminated — re-run first.  
4. **W2/W3** not wall winners on default overworld thrash seed in this campaign.

## Next actions

1. Cold re-baseline: multi ×5 on `champion-16.5` only.  
2. Re-multi W1 and W4 ×5 on same quiet session.  
3. Implement W5/W8 (medium eng) only after W1 re-score.  
4. Combo only if any exp ≥ FLAG or ADOPT on cold multi.

## Branches on remote

- `champion-16.5` (tag)  
- `exp/w4-pregrow-4bit`  
- `exp/w1-spec-cells`  
- `exp/w2-cell-batch`  
- `exp/w3-path-moe`  
