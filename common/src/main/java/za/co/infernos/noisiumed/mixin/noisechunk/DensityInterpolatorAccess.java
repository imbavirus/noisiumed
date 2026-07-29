package za.co.infernos.noisiumed.mixin.noisechunk;

import org.spongepowered.asm.mixin.Mixin;
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
}
