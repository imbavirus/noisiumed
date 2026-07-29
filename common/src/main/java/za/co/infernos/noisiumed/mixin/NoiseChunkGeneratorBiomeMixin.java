package za.co.infernos.noisiumed.mixin;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import za.co.infernos.noisiumed.path.PathMetrics;
import za.co.infernos.noisiumed.path.PathTier;

/**
 * Fixed single-biome worlds: one supplier sample, then bulk section fill (via {@link ChunkSectionMixin}).
 */
@Mixin(NoiseChunkGenerator.class)
public abstract class NoiseChunkGeneratorBiomeMixin extends ChunkGenerator {
	public NoiseChunkGeneratorBiomeMixin(BiomeSource biomeSource) {
		super(biomeSource);
	}

	@Inject(
			method = "populateBiomes(Lnet/minecraft/world/gen/chunk/Blender;Lnet/minecraft/world/gen/noise/NoiseConfig;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/chunk/Chunk;)V",
			at = @At("HEAD"),
			cancellable = true
	)
	private void noisiumed$populateFixedBiomes(
			Blender blender,
			NoiseConfig noiseConfig,
			StructureAccessor structureAccessor,
			Chunk chunk,
			CallbackInfo ci
	) {
		BiomeSource source = this.getBiomeSource();
		if (!(source instanceof FixedBiomeSource fixed)) {
			return;
		}

		RegistryEntry<Biome> biome = fixed.getBiome(0, 0, 0, null);
		for (ChunkSection section : chunk.getSectionArray()) {
			if (section != null) {
				// Constant supplier + null sampler: BulkBiomeFiller single-biome path.
				section.populateBiomes((qx, qy, qz, s) -> biome, null, 0, 0, 0);
			}
		}
		PathMetrics.record(PathTier.L2);
		ci.cancel();
	}
}
