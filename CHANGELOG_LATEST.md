### Fixed

- NeoForge chunk load failures from a broken `ChainedBlockSource` constructor mixin inject
- Forge/NeoForge "No refMap loaded" crashes on `NoiseChunkGeneratorMixin` (plugin + refmap packaging)
- Fabric jar incorrectly loadable via Sinytra Connector (now breaks Connector; use native Forge/NeoForge jar)
- Modrinth/CurseForge version tags mixing Minecraft ranges / incomplete game versions
