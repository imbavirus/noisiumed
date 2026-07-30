package za.co.infernos.noisiumed.noise;

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

	/**
	 * W1: after {@code onSampledCellCorners}, true if every primary density in the cell is &gt; 0
	 * (safe solid-only materialize; skip aquifer per block).
	 */
	default boolean noisiumed$cellAllSolid() {
		return false;
	}
}
