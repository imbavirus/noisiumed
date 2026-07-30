# Multi-run Spark worldgen A/B (honest scoreboard)

Date: 2026-07-30T22:11:55.9220279+02:00
Runs=3 seed=12345 radiusChunks=7 profileSeconds=100
SpikeFactor=1.5 (values > factor x median flagged / dropped from quiet set)
Jar: `noisiumed-4.0.0-beta.16.5-w5-neoforge-1.21.1.jar` vs zfastnoise 1.0.13

## Per-run wall_forceload_ms

| Run | Noisiumed | Fast Noise | Noisiumed Spark | FN Spark |
|-----|-----------|------------|-----------------|----------|
| 1 | 54486 | 53930 | https://spark.lucko.me/b6hktm89g2 | https://spark.lucko.me/YoVdbDcfiX |
| 2 | 40166 | 40529 | https://spark.lucko.me/r306HLBbuF | https://spark.lucko.me/DEOq47BzRx |
| 3 | 46833 | 117947 | https://spark.lucko.me/FxrOEcNa6P | https://spark.lucko.me/A96ixnMFqr |

## Wall forceload summary

| Metric | Noisiumed | Fast Noise | Delta (N-F) | % faster (N vs F) |
|--------|-----------|------------|-------------|-------------------|
| **mean** | 47162 | 70802 | -23640 | 33,4% |
| **median** | 46833 | 53930 | -7097 | 13,2% |
| min | 40166 | 40529 | | |
| max | 54486 | 117947 | | |
| stdev | 7166 | 41375 | | |
| **quiet mean** (no spikes) | 47162 | 47230 | -68 | 0,1% |
| **quiet median** | 46833 | 47230 | -397 | 0,8% |
| wall_boot_ms avg | 27995 | 33559 | | |
| wall_total_ms avg | 227865 | 258653 | | |
| l1_avg_us avg | 74316 | n/a | | |
| sample_pct avg | 79,0 | n/a | | |
| write_pct avg | 20,0 | n/a | | |

Positive **% faster** means Noisiumed forceload is lower (better). **Quiet** drops runs > SpikeFactor x median.

## Spikes

- Noisiumed: none
- Fast Noise: run3=117947 (>1,5x median 53930)

## Successful forceload samples
- Noisiumed n=3: 54486, 40166, 46833
- Fast Noise n=3: 53930, 40529, 117947
- Quiet N n=3: 54486, 40166, 46833
- Quiet F n=2: 53930, 40529

## Dominance gate (plan)

- Quiet median â‰¥10% faster: **FAIL** (0,8%)
- Multi mean â‰¥5% faster: **PASS** (33,4%)
- Combined dominance gate: **FAIL**

