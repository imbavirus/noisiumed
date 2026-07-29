package za.co.infernos.noisiumed.heightmap;

import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

/**
 * Rebuild worldgen heightmaps once after bulk noise (and optionally after surface — L2).
 * Skips per-block {@code trackUpdate} in the noise hot loop.
 */
public final class DeferredHeightmaps {
	private static final EnumSet<Heightmap.Type> NOISE_HEIGHTMAPS = EnumSet.of(
			Heightmap.Type.OCEAN_FLOOR_WG,
			Heightmap.Type.WORLD_SURFACE_WG
	);

	private DeferredHeightmaps() {}

	public static void populateAfterNoise(@NotNull Chunk chunk) {
		Heightmap.populateHeightmaps(chunk, NOISE_HEIGHTMAPS);
	}

	/**
	 * After surface rules, vanilla also maintains motion-blocking maps; WG maps may need refresh
	 * if surface changed top blocks. Rebuild the same WG set for parity.
	 */
	public static void populateAfterSurface(@NotNull Chunk chunk) {
		Heightmap.populateHeightmaps(chunk, NOISE_HEIGHTMAPS);
	}
}
