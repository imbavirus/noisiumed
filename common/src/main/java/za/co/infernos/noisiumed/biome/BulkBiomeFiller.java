package za.co.infernos.noisiumed.biome;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSupplier;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.chunk.PalettedContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import za.co.infernos.noisiumed.pool.WorldgenScratchPool;

/**
 * Pack 4×4×4 biome samples into a section palette with minimal work.
 * Single-biome sections take a specialized fill path.
 */
public final class BulkBiomeFiller {
	private static final int SLICE = 4;

	private BulkBiomeFiller() {}

	@SuppressWarnings("unchecked")
	public static @NotNull PalettedContainer<RegistryEntry<Biome>> populate(
			@NotNull PalettedContainer<RegistryEntry<Biome>> container,
			@NotNull BiomeSupplier biomeSupplier,
			@Nullable MultiNoiseUtil.MultiNoiseSampler sampler,
			int x,
			int y,
			int z
	) {
		WorldgenScratchPool.Holder pool = WorldgenScratchPool.get();
		RegistryEntry<Biome>[] palette = pool.biomePalette;
		int[] indices = pool.biomeIndices;

		int paletteSize = 0;
		int n = 0;

		for (int posY = 0; posY < SLICE; ++posY) {
			for (int posZ = 0; posZ < SLICE; ++posZ) {
				for (int posX = 0; posX < SLICE; ++posX) {
					RegistryEntry<Biome> biome = biomeSupplier.getBiome(x + posX, y + posY, z + posZ, sampler);
					int id = -1;
					// Linear scan is fine for typical palette sizes (1–8 biomes per section).
					for (int p = 0; p < paletteSize; p++) {
						if (palette[p] == biome) {
							id = p;
							break;
						}
					}
					if (id < 0) {
						id = paletteSize;
						palette[paletteSize++] = biome;
					}
					indices[n++] = id;
				}
			}
		}

		if (paletteSize == 1) {
			RegistryEntry<Biome> only = palette[0];
			for (int posY = 0; posY < SLICE; ++posY) {
				for (int posZ = 0; posZ < SLICE; ++posZ) {
					for (int posX = 0; posX < SLICE; ++posX) {
						container.swapUnsafe(posX, posY, posZ, only);
					}
				}
			}
			return container;
		}

		n = 0;
		for (int posY = 0; posY < SLICE; ++posY) {
			for (int posZ = 0; posZ < SLICE; ++posZ) {
				for (int posX = 0; posX < SLICE; ++posX) {
					container.swapUnsafe(posX, posY, posZ, palette[indices[n++]]);
				}
			}
		}
		return container;
	}
}
