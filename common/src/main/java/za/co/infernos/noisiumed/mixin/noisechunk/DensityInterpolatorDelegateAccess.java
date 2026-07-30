package za.co.infernos.noisiumed.mixin.noisechunk;

import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Mutable access to interpolator delegate for beta.16 deep specialize.
 */
@Mixin(targets = "net.minecraft.world.gen.chunk.ChunkNoiseSampler$DensityInterpolator")
public interface DensityInterpolatorDelegateAccess {
	@Accessor("delegate")
	DensityFunction noisiumed$getDelegate();

	@Accessor("delegate")
	@Mutable
	void noisiumed$setDelegate(DensityFunction delegate);
}
