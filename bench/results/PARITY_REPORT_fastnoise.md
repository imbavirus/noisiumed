# Golden section hash parity â€” baseline vs fastnoise

Date: 2026-07-30T04:40:00.8782680+02:00
Seed=12345 radiusChunks=2
Baseline: spark only (vanilla NoiseChunk)
Candidate: zfastnoise-1.0.13+1.21.1+neoforge.jar

| | |
|--|--|
| baseline overall | 5c8e9294c0bb09795b06604cab73fbe61cd81bde44441c2f5c3f5bcfe63b66f1 |
| fastnoise overall | 6df33a925491624dd39d2f93fdd376ae9afcfe149f7a2031b06d6ea765cbd577 |
| chunk matches | 3 |
| mismatches | 22 |
| **PASS** | **False** |

Details: `bench/results/PARITY_COMPARE_fastnoise.json`
