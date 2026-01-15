![Noisiumed icon](docs/assets/icon/icon_128x128.png)

# Noisiumed

**A maintained fork of [Noisium](https://github.com/Steveplays28/noisium) by [Steveplays28](https://github.com/Steveplays28)**

Noisiumed aims to improve world generation performance by optimizing internal Minecraft functions.

## What It Does

This mod targets specific world generation functions to reduce overhead during chunk creation:

- Optimized block state placement during noise population
- Improved biome population efficiency
- Cached generation shape calculations
- Streamlined chunk section handling

**World Generation Parity**: This mod produces identical worlds to vanilla Minecraft—no blocks, biomes, or structures are changed.

## Dependencies

### Required

None.

## Compatibility

Noisiumed should be compatible with most popular optimization mods, including:

- **C2ME**: Recommended for additional world generation threading improvements
- **Lithium**: Full compatibility
- **Sodium/Nvidium**: No conflicts
- **Distant Horizons**: Works with LOD generation
- **ReTerraForged**: Compatible

### Known Incompatibilities

- Biospherical Expansion (BioX)

## Download

![Fabric](https://github.com/intergrav/devins-badges/raw/2dc967fc44dc73850eee42c133a55c8ffc5e30cb/assets/compact/supported/fabric_vector.svg)
![Quilt](https://github.com/intergrav/devins-badges/raw/2dc967fc44dc73850eee42c133a55c8ffc5e30cb/assets/compact/supported/quilt_vector.svg)
![NeoForge](docs/assets/badges/compact/supported/neoforge_vector.svg)

See the version info in the filename for supported Minecraft versions.  
**Server-side only** — clients do not need to install this mod (but it works on singleplayer/LAN).

## FAQ

**Q: Does this mod work in multiplayer?**  
A: Yes, but only the server benefits from the optimizations.

**Q: Do clients need this mod installed?**  
A: No, only the server needs it. Works on singleplayer and LAN hosts too.

## Credits

This mod is a fork of **Noisium** by **Steveplays28**. All original optimization concepts and core implementations are credited to them.

### Original Contributors
- [Builderb0y](https://modrinth.com/user/Builderb0y) — initial guidance and issue resolution
- [ishland](https://github.com/ishland) — C2ME compatibility work
- [Uniter](https://github.com/Uniter343) and [raccoonman2](https://github.com/racoonman2) — testing

## License

This project is licensed under LGPLv3, see [LICENSE](LICENSE).
