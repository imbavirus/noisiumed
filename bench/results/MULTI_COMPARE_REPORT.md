# Multi-run Spark worldgen A/B (honest scoreboard)

Date: 2026-07-30T18:29:16.6189673+02:00
Runs=5 seed=12345 radiusChunks=7 profileSeconds=100
SpikeFactor=1.5 (values > factor x median flagged / dropped from quiet set)
Jar: `noisiumed-4.0.0-beta.16.5-neoforge-1.21.1.jar` vs zfastnoise 1.0.13

## Per-run wall_forceload_ms

| Run | Noisiumed | Fast Noise | Noisiumed Spark | FN Spark |
|-----|-----------|------------|-----------------|----------|
| 1 | 42527 | 53723 | https://spark.lucko.me/czqQIlZldo | https://spark.lucko.me/lWBFgncv2c |
| 2 | 40632 | 45816 | https://spark.lucko.me/rQQrpqyPDH | https://spark.lucko.me/pQe0NWNBq8 |
| 3 | 40080 | 53944 | https://spark.lucko.me/kMT5pZmpyJ | https://spark.lucko.me/XWFAuQRA9J |
| 4 | 44182 | 54384 | https://spark.lucko.me/CLfNoBOSuW | https://spark.lucko.me/OJZRzKhHOV |
| 5 | 54083 | 48174 | https://spark.lucko.me/2P2gGBxLEi | https://spark.lucko.me/KlNHWgtyB1 |

## Wall forceload summary

| Metric | Noisiumed | Fast Noise | Delta (N-F) | % faster (N vs F) |
|--------|-----------|------------|-------------|-------------------|
| **mean** | 44301 | 51208 | -6907 | 13,5% |
| **median** | 42527 | 53723 | -11196 | 20,8% |
| min | 40080 | 45816 | | |
| max | 54083 | 54384 | | |
| stdev | 5704 | 3943 | | |
| **quiet mean** (no spikes) | 44301 | 51208 | -6907 | 13,5% |
| **quiet median** | 42527 | 53723 | -11196 | 20,8% |
| wall_boot_ms avg | 26254 | 27713 | | |
| wall_total_ms avg | 184871 | 192717 | | |
| l1_avg_us avg | 69869 | n/a | | |
| sample_pct avg | 71,6 | n/a | | |
| write_pct avg | 27,4 | n/a | | |

Positive **% faster** means Noisiumed forceload is lower (better). **Quiet** drops runs > SpikeFactor x median.

## Spikes

- Noisiumed: none
- Fast Noise: none

## Successful forceload samples
- Noisiumed n=5: 42527, 40632, 40080, 44182, 54083
- Fast Noise n=5: 53723, 45816, 53944, 54384, 48174
- Quiet N n=5: 42527, 40632, 40080, 44182, 54083
- Quiet F n=5: 53723, 45816, 53944, 54384, 48174

## Dominance gate (plan)

- Quiet median â‰¥10% faster: **PASS** (20,8%)
- Multi mean â‰¥5% faster: **PASS** (13,5%)
- Combined dominance gate: **PASS**

