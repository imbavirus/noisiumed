package za.co.infernos.noisiumed.mixin.noisechunk;

import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import za.co.infernos.noisiumed.density.special.DensitySpecializer;

import java.util.List;

/**
 * After vanilla installs interpolators/caches/holder unwrap, rewrite hot arithmetic
 * nodes to monomorphic specialized implementations (Phase 3 + beta.16 deep delegates).
 */
@Mixin(value = ChunkNoiseSampler.class, priority = 1100)
public abstract class ChunkNoiseSamplerSpecializeMixin {
	@Shadow
	@Final
	private List<?> interpolators;

	@Shadow
	@Final
	private List<?> caches;

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

	/**
	 * CellCache / DensityInterpolator roots are not rewritten by RETURN specialize — their
	 * arithmetic lives in {@code delegate}. Fix that once construction finishes.
	 */
	@Inject(method = "<init>", at = @At("RETURN"))
	private void noisiumed$specializeDelegates(CallbackInfo ci) {
		List<?> interps = this.interpolators;
		List<?> cellCaches = this.caches;
		if (interps == null || cellCaches == null) {
			return;
		}
		DensitySpecializer.specializeInstalledDelegates(interps, cellCaches);
	}
}
