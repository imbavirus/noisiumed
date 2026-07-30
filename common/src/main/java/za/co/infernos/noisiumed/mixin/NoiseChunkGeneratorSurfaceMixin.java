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
 *   <li>Skip {@code buildSurface} when L1 wrote no solids (column mask) or every section is empty</li>
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
		// L1 defers WG heightmaps during the sample loop for speed. Vanilla surface rules
		// expect OCEAN_FLOOR_WG / WORLD_SURFACE_WG to already reflect noise fill — flush
		// them here BEFORE surface runs (not only after). Skipping this made surface wrong.
		if (ChunkGenAttachment.isHeightmapsPending(chunk)) {
			DeferredHeightmaps.populateAfterNoise(chunk);
			// Keep pending so RETURN can rebuild after surface mutates top blocks.
		}

		// Fast path: L1 already knows whether any non-air was written.
		if (ChunkGenAttachment.wasL1Used(chunk) && !ChunkGenAttachment.l1HasSolid(chunk)) {
			PathMetrics.recordSurfaceSkip();
			PathMetrics.record(PathTier.L2);
			finalizeDeferredHeightmaps(chunk);
			ci.cancel();
			return;
		}

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
			// Surface may have replaced top blocks; rebuild WG heightmaps from final sections
			// (parity with vanilla per-block trackUpdate during surface).
			if (ChunkGenAttachment.isHeightmapsPending(chunk)
					|| ChunkGenAttachment.l1HasSolid(chunk)) {
				DeferredHeightmaps.populateAfterSurface(chunk);
				ChunkGenAttachment.clearHeightmapsPending(chunk);
			}
		}
		ChunkGenAttachment.clearAll(chunk);
	}

	private static void finalizeDeferredHeightmaps(Chunk chunk) {
		if (ChunkGenAttachment.isHeightmapsPending(chunk)) {
			DeferredHeightmaps.populateAfterNoise(chunk);
			ChunkGenAttachment.clearHeightmapsPending(chunk);
		}
		ChunkGenAttachment.clearAll(chunk);
	}
}
