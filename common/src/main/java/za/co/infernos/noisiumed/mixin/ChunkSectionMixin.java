package za.co.infernos.noisiumed.mixin;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSupplier;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.chunk.ReadableContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import za.co.infernos.noisiumed.biome.BulkBiomeFiller;
import za.co.infernos.noisiumed.path.PathMetrics;

@Mixin(ChunkSection.class)
public class ChunkSectionMixin {
	@Shadow
	private ReadableContainer<RegistryEntry<Biome>> biomeContainer;

	/**
	 * @author Steveplays28, Infernos
	 * @reason Bulk biome packing (palette + single-biome fast path) instead of 64× raw swap only.
	 */
	@Overwrite
	public void populateBiomes(BiomeSupplier biomeSupplier, MultiNoiseUtil.MultiNoiseSampler sampler, int x, int y, int z) {
		PalettedContainer<RegistryEntry<Biome>> palettedContainer = this.biomeContainer.slice();
		this.biomeContainer = BulkBiomeFiller.populate(palettedContainer, biomeSupplier, sampler, x, y, z);
		PathMetrics.recordBiome();
	}
}
