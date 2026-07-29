package za.co.infernos.noisiumed.mixin.noisechunk;

import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Invoker for package-private DensityInterpolator update methods.
 * Calls the (possibly overwrite-optimized) interpolateY/X/Z implementations.
 */
@Mixin(targets = "net.minecraft.world.gen.chunk.ChunkNoiseSampler$DensityInterpolator")
public interface DensityInterpolatorAccess {
	@Invoker("interpolateY")
	void noisiumed$interpolateY(double deltaY);

	@Invoker("interpolateX")
	void noisiumed$interpolateX(double deltaX);

	@Invoker("interpolateZ")
	void noisiumed$interpolateZ(double deltaZ);

	@Invoker("onSampledCellCorners")
	void noisiumed$onSampledCellCorners(int cellY, int cellZ);

	@Invoker("fill")
	void noisiumed$fill(double[] densities, DensityFunction.EachApplier applier);

	@Accessor("startDensityBuffer")
	double[][] noisiumed$getStartDensityBuffer();

	@Accessor("endDensityBuffer")
	double[][] noisiumed$getEndDensityBuffer();

	@Accessor("delegate")
	DensityFunction noisiumed$getDelegate();
}
