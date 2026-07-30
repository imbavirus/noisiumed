# Isolation scoreboard vs champion β16.5

**No LLM runtime** — systems-strategy experiments only.  
**Rule:** one branch per experiment from tag `champion-16.5`. Do not stack until scored.

## CONTROL (champion-16.5)

| Field | Value |
|-------|--------|
| Tag / version | `champion-16.5` / `4.0.0-beta.16.5` |
| Multi date | 2026-07-30 (historical win multi) |
| Quiet median wall_forceload_ms | **42527** |
| Mean wall_forceload_ms | **44301** |
| vs FN quiet median | +20.8% (FN 53723) |
| vs FN mean | +13.5% (FN 51208) |
| Golden FULL (soft) | thrash ~2/25 (known) |
| N per-run | 42527, 40632, 40080, 44182, 54083 |
| F per-run | 53723, 45816, 53944, 54384, 48174 |

Primary gate for experiments: **Δ% quiet median vs this CONTROL**, not only vs FN.

| Decision | Quiet med Δ% vs champ | Mean Δ% | Golden |
|----------|----------------------|---------|--------|
| ADOPT | ≥ +5% | ≥ +3% | not freefall |
| FLAG_DEFAULT_OFF | +2–5% | +1–3% | not freefall |
| REJECT | &lt; +2% or slower | regress | freefall / crash |

## Experiments

| Exp | Branch | Quiet med Δ% vs champ | Mean Δ% | Golden | sample_pct | vs FN quiet | Decision |
|-----|--------|----------------------|---------|--------|------------|-------------|----------|
| W4 | `exp/w4-pregrow-4bit` | | | | | | pending |
| W1 | `exp/w1-spec-cells` | | | | | | pending |
| W2 | `exp/w2-cell-batch` | | | | | | pending |
| W5 | `exp/w5-arena-scratch` | | | | | | pending |
| W7 | `exp/w7-aquifer-cache` | | | | | | pending |
| W8 | `exp/w8-heightmap-prime` | | | | | | pending |
| W3 | `exp/w3-path-moe` | | | | | | pending |
| W6 | `exp/w6-secondary-lut` | | | | | | pending |
| W9 | `exp/w9-turbo-preset` | | | | | | pending |

## Harness notes

- Multi: `Run-WorldgenSparkCompareMulti.ps1 -Runs 5` (seed 12345, r=7, 100s)
- **Do not kill unrelated Minecraft/Java server processes** — only stop servers this harness started, if anything
- Pin jar PreferName to the experiment version on each branch
