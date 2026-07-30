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

### Measurement

1. ~~Re-baseline champion + re-run W1/W4/W6~~ → **done** (hot session 2026-07-30/31; CONTROL_SESSION quiet med 52981).  
2. Optional true-cold **W6 + champ** multi when idle.  
3. Always pair exp with same-session FN and Δ% vs **that session’s champion jar**.  
4. Record **l1_avg_us / sample_pct / write_pct** on the scoreboard row (not only wall).  
5. Optional: single-mod multi without FN when FN dir is locked (N-only wall series).

### Harness

1. ~~Retry/remove console/debug locks~~, ~~PreferName pin~~, ~~ReportSuffix~~ — largely **done** (`106611e`).  
2. Don’t `Remove-Item -Recurse` entire run tree if only mods need refresh.  
3. SpikeFactor on **both** N and F when stdev is huge.

### Code / research

1. **W1**: REJECT as-is. Rework only with secondary-only ore + `cells_speculated` counter if revisit.  
2. **W2**: only worth it if fused with a proven solid fill.  
3. **W3**: score on aquifers-off pack; default OW decision is “no wall impact.”  
4. **W5**: length-bucket scratch already mostly done; win is killing remaining heap in fillArray/specialize.  
5. **W6**: FLAG — cold confirm then stack.  
6. **W7**: aquifer lattice cache only with golden gate — high risk.  
7. **W8**: heightmap prime is real FN edge; independent of sample path.  
8. **W9**: `turbo` = champion + **W6 only** until other FLAG/ADOPT.

### Campaign hygiene

1. Do not force-push `champion-16.5`.  
2. One experiment = one branch from tag.  
3. Document decision on scoreboard before merge.  
4. Never kill non-bench Minecraft processes.

---

## Decision snapshot (post merit rescore 2026-07-31)

| Item | Status |
|------|--------|
| Champion 16.5 | **Keep** — production wall baseline (cold historical +20.8% vs FN) |
| W4 pregrow | **REJECT** on ×5 same-session rescore (−4.4% vs CONTROL_SESSION) |
| W1 solid cells | **REJECT** this impl (−5.4% vs CONTROL_SESSION); rework needs hit-rate + secondary-only ore |
| W6 secondary | **FLAG** — best rescore N (+14.6% vs CONTROL_SESSION); still −2.4% vs same-session FN |
| W2 cell-batch | Reject for wall |
| W3 path MoE | Reject on default OW; aquifers-off later |
| W5 / W8 | Reject |
| W7 / W9 | Design; W9 stack only champion+W6 after cold W6 |

---

## Merit rescore (×5 sequential) — what we learned

1. **Same-session control is mandatory.** Cold CONTROL 42527 ms did not reappear (session champ quiet **52981**); using cold Δ would have falsely punished every exp.
2. **W4’s earlier +2.9% FLAG was underpowered / cold-biased.** ×5 with pinned jars → slower than session champ; do not ship pregrow as default.
3. **W1 is measurable and loses.** High `sample_pct` (~91%) / low `write_pct` (~8%) with worse wall ⇒ solid-cell skip is not paying for itself on thrash seed with current code path.
4. **W6 is the only meritorious isolation win this campaign.** Quiet N 45242 vs session 52981 is large, but FN also accelerated late session — treat as **FLAG**, not ADOPT, until a cold W6 multi still beats CONTROL_SESSION relative or at least matches FN quiet.
5. **Harness hardening worked** (port stop, unique logs, PreferName, ReportSuffix): four full multis completed EXIT=0 without killing foreign MC processes.
6. **Domination vs FN is not free** on a hot box: none of champion/W1/W4/W6 re-cleared quiet≥10% / mean≥5% this session.

### Immediate next

1. True-cold multi ×5 **W6 only** (and optional champ control) when machine is quiet.  
2. If W6 still FLAG → design W9 turbo as **champion + W6 only**.  
3. Do not stack W1/W4 without rework.  
4. Optional: aquifers-off multi for W3; real heightmap prime for W8.

---

## Continue plan (W5–W9) — status after continuation + rescore

| Exp | Done |
|-----|------|
| W5 arena | Implemented + multi → **REJECT** vs cold control (hot session) |
| W6 secondary | Implemented + **merit rescore FLAG** (parity-safe gap skip) |
| W8 heightmap | Implemented + multi → **REJECT** on thrash (no empty chunks) |
| W7 aquifer | Design branch only |
| W9 turbo | Design branch only — stack **W6 only** after cold confirm |

### Additional learnings from continuation

1. **Hot machine + FN spikes** (e.g. 117s FN, 94s N) make isolation tables noisy; document session quality.  
2. **File locks** on `console.log` / `debug.log` from leftover `cmd` redirects — fixed with **unique console log names** + port-based stop.  
3. **W8 empty heightmap skip** is theoretically free but **never fires** when `surface_skip=0` / all chunks have solids.  
4. **W6 gap short-circuit** is the right class of secondary quant analogue: **same RNG order, less DF work**.  
5. Do **not** kill bare `user_jvm_args` java processes without port match — those can be other MC servers.
