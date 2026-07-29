# Golden section hash parity

Date: 2026-07-30 (beta.12 harness)
Seed=12345 radiusChunks=2 (25 chunks around origin)
Baseline: spark only (vanilla NoiseChunk)
Candidate: noisiumed-4.0.0-beta.12-neoforge-1.21.1.jar

| | |
|--|--|
| baseline overall | `7634c315384a3f75c179195fc0d15f76225d11cda1d4c8810c3175cace8def57` |
| noisiumed overall | `b60d3dbc81db4e487031096beee36711229aa96e6704ac7486f89044aa402b88` |
| chunk matches | **8** |
| mismatches | **17** |
| **PASS** | **false** |

Hashes cover overworld section `block_states` palette + packed data only (not biomes/entities).

### How to re-run

```powershell
cd bench
pip install nbtlib
.\Run-ParityHash.ps1 -RadiusChunks 2 -Seed 12345
```

### Bisect flags (candidate JVM args in Prepare-ParityDir)

| Flag | Meaning |
|------|---------|
| `-Dnoisiumed.fast.ore=false` | vanilla ore lambda |
| `-Dnoisiumed.aquifer.specialize=false` | vanilla aquifer density nodes |
| `-Dnoisiumed.cell.density.grid=false` | disable NC-3 cell grid sample |

### Notes

- 8/25 exact matches rules out total chaos; residual likely L1 write / surface / ore / aquifer edge.
- Gate for any future `sampleDensity` rewrite: this report must PASS.
