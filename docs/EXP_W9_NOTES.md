# W9 Turbo preset (non-parity product ladder)

## Intent
GGUF-style quality ladder: explicit `turbo` preset stacking only proven/FLAG isolations.

## Proposed stack (after cold re-bench)
1. Champion 16.5 defaults
2. W4 pregrow 4-bit (FLAG +2.9% clean)
3. W1 solid cells (if re-bench ADOPT/FLAG)
4. W6 gap short-circuit (parity-safe micro)

## Config sketch
\\\
-Dnoisiumed.preset=turbo
# implies: w4 pregrow + w1 solid cells + w6 gap skip
\\\

## Gate
Not golden FULL. Smoke + wall multi only. Label clearly non-vanilla.

## Status
**Design branch** — do not ship until W1 re-scored on cold multi.
