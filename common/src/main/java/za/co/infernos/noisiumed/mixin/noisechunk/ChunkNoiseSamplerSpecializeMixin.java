package za.co.infernos.noisiumed.mixin.noisechunk;

import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import za.co.infernos.noisiumed.density.special.DensitySpecializer;

/**
 * After vanilla installs interpolators/caches/holder unwrap, rewrite hot arithmetic
 * nodes to monomorphic specialized implementations (Phase 3).
 */
@Mixin(ChunkNoiseSampler.class)
public abstract class ChunkNoiseSamplerSpecializeMixin {
	@Inject(method = "getActualDensityFunctionImpl", at = @At("RETURN"), cancellable = true)
	private void noisiumed$specialize(DensityFunction function, CallbackInfoReturnable<DensityFunction> cir) {
		DensityFunction out = cir.getReturnValue();
		if (out == null) {
			return;
		}
		DensityFunction specialized = DensitySpecializer.specialize(out);
		if (specialized != out) {
			cir.setReturnValue(specialized);
		}
	}
}
