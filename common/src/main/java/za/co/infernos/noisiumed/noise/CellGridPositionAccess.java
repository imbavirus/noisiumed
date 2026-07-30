package za.co.infernos.noisiumed.noise;

import net.minecraft.block.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * When NC-3 cell density grid is active, primary density does not use interpolator lerps.
 * Aquifer/ore only need correct {@code blockX/Y/Z} (= start + cell) and sample index.
 * Skipping interpolator updates removes the largest per-block cost on solid-dominated maps.
 */
public interface CellGridPositionAccess {
	boolean noisiumed$cellGridActive();

	/** Set cellBlockY only (no interpolator Y lerp). */
	void noisiumed$positionY(int blockY);

	/** Set cellBlockX only. */
	void noisiumed$positionX(int blockX);

	/** Set cellBlockZ + bump sampleUniqueIndex (CacheOnce / pos identity). */
	void noisiumed$positionZ(int blockZ);

	/** W2: primary CellCache doubles for the current cell, or null. */
	default @Nullable double[] noisiumed$primaryDensityCache() {
		return null;
	}

	default int noisiumed$cellW() {
		return 0;
	}

	default int noisiumed$cellH() {
		return 0;
	}

	/**
	 * W2: materialize block state from a prefetched density at the current cell position
	 * (same logic as NC-3 sampleBlockState hot path).
	 */
	default @Nullable BlockState noisiumed$materializeFromDensity(double density) {
		return null;
	}
}
