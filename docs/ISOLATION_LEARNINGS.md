# Isolation campaign — learnings & improvements

Date: 2026-07-30. Champion: `champion-16.5` / `4.0.0-beta.16.5`.

## What we learned

### Process / measurement

1. **Quiet median vs FN was the right wall gate** — mean and single runs lie. Isolation must also use quiet median, but **Δ vs champion** needs a **same-session re-baseline** when the machine is hot.
2. **Absolute forceload is not stationary.** Clean champion multi sat ~40–42s quiet; later sessions hit ~50–65s for both N and FN. Comparing a hot exp to a cold control overstates regressions.
3. **Harness file locks** broke multis (debug.log / console.log held). Killing *all* Java is wrong (other MC servers). Scoped stop by run-dir + ports is correct but must **retry** deletes and clear log handles.
4. **Default PreferName pin** silently re-ran old jars (16.4 vs 16.5). Experiment jars need exact PreferName + `-wN` regex.
5. **3-run multi is underpowered** for noisy walls; prefer **5 runs** once machine is quiet. Use 3 only for triage.

### Technical (AI-strategy transfers)

| Idea | Lesson |
|------|--------|
| **Quantization** | Primary density is a decision boundary (`> 0`); do not quantize it without a verify band. Secondary-only / LUT is the only safe quant analogue. |
| **KV / reuse** | CellCache + specialize already own the big reuse win. More “cache everything” without fusion did not move wall in isolation. |
| **Speculative decoding (W1)** | Best conceptual ROI (all-solid cells skip aquifer). Implementation exists; **multi contaminated** — not disproven. |
| **Continuous batch / fusion (W2)** | Prefetch density + materialize is correct structure; hot multi showed **no gain** vs FN. May still help on cold re-bench or when fused with W1 solid path. |
| **Path MoE (W3)** | Fluid-tick skip is **dead on default overworld** (always NoiseBasedAquifer). Needs aquifers-off datapack multi to score. |
| **Write path (W4)** | Pregrow 4-bit is the **only clean isolation win** (~+3% quiet med vs cold control). Below ADOPT (+5%) → flag, not default ship. |
| **Champion 16.5** | FN-style OR packing + ore Y already delivered the big wall win vs FN. Isolation is about **beating ourselves**, not FN again. |

### Product

- Stacking unproven branches is how β16 deep-interp died. Isolation rule stays.
- Turbo / non-parity (W9) must be an **explicit preset**, never silent.

---

## What needs improvement

### Measurement (do next cold session)

1. **Re-baseline champion** multi ×5 on a cold machine → freeze CONTROL v2.  
2. **Re-run W1 and W4** ×5 same session (only candidates worth it).  
3. **Pair every exp row with same-session FN** and Δ% vs **that session’s champion jar** if control drifts &gt;5%.  
4. Record **l1_avg_us / sample_pct / write_pct** on the scoreboard row (not only wall).  
5. Optional: single-mod multi without FN when FN dir is locked (N-only wall series).

### Harness

1. Retry/remove `fastnoise-console.log` / `noisiumed-console.log` with stop-first.  
2. Don’t `Remove-Item -Recurse` entire run tree if only mods need refresh.  
3. PreferName always set from built jar name in the multi script (not only default constant).  
4. SpikeFactor on **both** N and F when stdev is huge.

### Code / research

1. **W1**: finish solid-cell path without calling full `sampleBlockState` for ore (secondary-only). Count `cells_speculated` metric.  
2. **W2**: only worth it if fused with W1 solid fill (batch index + no virtual call on solid).  
3. **W3**: score on aquifers-off pack; default OW decision is “no wall impact.”  
4. **W5**: length-bucket scratch already mostly done; win is killing remaining heap in fillArray/specialize.  
5. **W7**: aquifer lattice cache only with golden gate — high risk.  
6. **W8**: heightmap prime is real FN edge; independent of sample path.  
7. **W9**: ship as `turbo` preset combining ADOPT/FLAG opts only.

### Campaign hygiene

1. Do not force-push `champion-16.5`.  
2. One experiment = one branch from tag.  
3. Document decision on scoreboard before merge.  
4. Never kill non-bench Minecraft processes.

---

## Decision snapshot

| Item | Status |
|------|--------|
| Champion 16.5 | **Keep** — production wall baseline |
| W4 pregrow | **FLAG** — small win, re-confirm cold |
| W1 solid cells | **Rework + re-bench** — top idea |
| W2 cell-batch | Reject for wall **this session**; optional cold re-check |
| W3 path MoE | Reject on default OW; aquifers-off later |
| W5–W9 | Continue implement + multi below |

---

## Continue plan (W5–W9) — status after continuation

| Exp | Done |
|-----|------|
| W5 arena | Implemented + multi → **REJECT** vs cold control (hot session) |
| W6 secondary | Implemented + multi → micro; **re-bench cold** (parity-safe gap skip) |
| W8 heightmap | Implemented + multi → **REJECT** on thrash (no empty chunks) |
| W7 aquifer | Design branch only |
| W9 turbo | Design branch only |

### Additional learnings from continuation

1. **Hot machine + FN spikes** (e.g. 117s FN, 94s N) make isolation tables noisy; document session quality.  
2. **File locks** on `console.log` / `debug.log` from leftover `cmd` redirects — fixed with **unique console log names** + port-based stop.  
3. **W8 empty heightmap skip** is theoretically free but **never fires** when `surface_skip=0` / all chunks have solids.  
4. **W6 gap short-circuit** is the right class of secondary quant analogue: **same RNG order, less DF work**.  
5. Do **not** kill bare `user_jvm_args` java processes without port match — those can be other MC servers.

### Immediate next (cold machine)

1. Multi ×5 `champion-16.5` → CONTROL v2.  
2. Multi ×5 W4, W1, W6 on same machine.  
3. If W1 FLAG/ADOPT → implement W9 turbo stacking W4+W1+W6.
