# Multi-run Spark worldgen A/B (honest scoreboard)

Date: 2026-07-30T20:53:15.2722696+02:00
Runs=3 seed=12345 radiusChunks=7 profileSeconds=100
SpikeFactor=1.5 (values > factor x median flagged / dropped from quiet set)
Jar: `noisiumed-4.0.0-beta.16.5-w1-neoforge-1.21.1.jar` vs zfastnoise 1.0.13

## Per-run wall_forceload_ms

| Run | Noisiumed | Fast Noise | Noisiumed Spark | FN Spark |
|-----|-----------|------------|-----------------|----------|
| 1 | FAIL | FAIL | | |
| 2 | 65654 | 54823 | https://spark.lucko.me/ZKhcJvdN4h | https://spark.lucko.me/Uhc0qThg8f |
| 3 | 40509 | 44070 | https://spark.lucko.me/q3qNue01Im | https://spark.lucko.me/JhFjub6yP5 |

## Wall forceload summary

| Metric | Noisiumed | Fast Noise | Delta (N-F) | % faster (N vs F) |
|--------|-----------|------------|-------------|-------------------|
| **mean** | 53082 | 49447 | 3635 | -7,4% |
| **median** | 53082 | 49447 | 3635 | -7,4% |
| min | 40509 | 44070 | | |
| max | 65654 | 54823 | | |
| stdev | 17780 | 7604 | | |
| **quiet mean** (no spikes) | 53082 | 49447 | 3635 | -7,4% |
| **quiet median** | 53082 | 49447 | 3635 | -7,4% |
| wall_boot_ms avg | 31360 | 26689 | | |
| wall_total_ms avg | 197463 | 189546 | | |
| l1_avg_us avg | 81823 | n/a | | |
| sample_pct avg | 94,0 | n/a | | |
| write_pct avg | 5,0 | n/a | | |

Positive **% faster** means Noisiumed forceload is lower (better). **Quiet** drops runs > SpikeFactor x median.

## Spikes

- Noisiumed: none
- Fast Noise: none

## Successful forceload samples
- Noisiumed n=2: 65654, 40509
- Fast Noise n=2: 54823, 44070
- Quiet N n=2: 65654, 40509
- Quiet F n=2: 54823, 44070

## Dominance gate (plan)

- Quiet median â‰¥10% faster: **FAIL** (-7,4%)
- Multi mean â‰¥5% faster: **FAIL** (-7,4%)
- Combined dominance gate: **FAIL**

