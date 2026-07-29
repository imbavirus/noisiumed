package za.co.infernos.noisiumed.mixin;

import net.minecraft.world.ChunkRegion;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import za.co.infernos.noisiumed.attach.ChunkGenAttachment;
import za.co.infernos.noisiumed.heightmap.DeferredHeightmaps;
import za.co.infernos.noisiumed.path.PathMetrics;
import za.co.infernos.noisiumed.path.PathTier;

/**
 * L2 surface path:
 * <ul>
 *   <li>Skip {@code buildSurface} when every section is empty</li>
 *   <li>After surface (or skip), flush deferred WG heightmaps from L1 noise fill</li>
 * </ul>
 */
@Mixin(NoiseChunkGenerator.class)
public class NoiseChunkGeneratorSurfaceMixin {
	@Inject(
			method = "buildSurface(Lnet/minecraft/world/ChunkRegion;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/gen/noise/NoiseConfig;Lnet/minecraft/world/chunk/Chunk;)V",
			at = @At("HEAD"),
			cancellable = true
	)
	private void noisiumed$skipEmptySurface(
			ChunkRegion region,
			StructureAccessor structures,
			NoiseConfig noiseConfig,
			Chunk chunk,
			CallbackInfo ci
	) {
		ChunkSection[] sections = chunk.getSectionArray();
		boolean allEmpty = true;
		for (ChunkSection section : sections) {
			if (section != null && !section.isEmpty()) {
				allEmpty = false;
				break;
			}
		}
		if (allEmpty) {
			PathMetrics.recordSurfaceSkip();
			PathMetrics.record(PathTier.L2);
			finalizeDeferredHeightmaps(chunk);
			ci.cancel();
		}
	}

	@Inject(
			method = "buildSurface(Lnet/minecraft/world/ChunkRegion;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/gen/noise/NoiseConfig;Lnet/minecraft/world/chunk/Chunk;)V",
			at = @At("RETURN")
	)
	private void noisiumed$afterSurface(
			ChunkRegion region,
			StructureAccessor structures,
			NoiseConfig noiseConfig,
			Chunk chunk,
			CallbackInfo ci
	) {
		if (ChunkGenAttachment.wasL1Used(chunk)) {
			PathMetrics.record(PathTier.L2);
		}
		finalizeDeferredHeightmaps(chunk);
	}

	private static void finalizeDeferredHeightmaps(Chunk chunk) {
		if (ChunkGenAttachment.isHeightmapsPending(chunk)) {
			DeferredHeightmaps.populateAfterNoise(chunk);
			ChunkGenAttachment.clearHeightmapsPending(chunk);
		}
		ChunkGenAttachment.clearAll(chunk);
	}
}
