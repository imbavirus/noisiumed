package za.co.infernos.noisiumed.mixin.noisechunk;

import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessors for interpolator slice fill (sampleDensity / DensityInterpolator.fill).
 */
@Mixin(ChunkNoiseSampler.class)
public interface ChunkNoiseSamplerFlagsAccess {
	@Accessor("isSamplingForCaches")
	boolean noisiumed$isSamplingForCaches();

	@Accessor("interpolationEachApplier")
	DensityFunction.EachApplier noisiumed$getInterpolationEachApplier();

	@Accessor("horizontalCellCount")
	int noisiumed$getHorizontalCellCount();

	@Accessor("startCellZ")
	int noisiumed$getStartCellZ();

	@Accessor("startBlockX")
	void noisiumed$setStartBlockX(int v);

	@Accessor("startBlockZ")
	void noisiumed$setStartBlockZ(int v);

	@Accessor("cellBlockX")
	void noisiumed$setCellBlockX(int v);

	@Accessor("cellBlockZ")
	void noisiumed$setCellBlockZ(int v);

	@Accessor("cacheOnceUniqueIndex")
	long noisiumed$getCacheOnceUniqueIndex();

	@Accessor("cacheOnceUniqueIndex")
	void noisiumed$setCacheOnceUniqueIndex(long v);

	@Accessor("horizontalCellBlockCount")
	int noisiumed$getHorizontalCellBlockCount();
}
