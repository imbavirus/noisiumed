package za.co.infernos.noisiumed.mixin.noisechunk;

import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import za.co.infernos.noisiumed.config.NoisiumedConfig;
import za.co.infernos.noisiumed.density.special.DensitySpecializer;
import za.co.infernos.noisiumed.path.PathMetrics;

/**
 * Open-aquifer path: specialize barrier / floodedness / spread / type / erosion / depth
 * density graphs once at aquifer construction (same values, monomorphic arithmetic).
 * Disable: {@code -Dnoisiumed.aquifer.specialize=false}
 */
@Mixin(targets = "net.minecraft.world.gen.chunk.AquiferSampler$Impl")
public abstract class AquiferImplSpecializeMixin {
	@Shadow
	@Final
	@Mutable
	private DensityFunction barrierNoise;

	@Shadow
	@Final
	@Mutable
	private DensityFunction fluidLevelFloodednessNoise;

	@Shadow
	@Final
	@Mutable
	private DensityFunction fluidLevelSpreadNoise;

	@Shadow
	@Final
	@Mutable
	private DensityFunction fluidTypeNoise;

	@Shadow
	@Final
	@Mutable
	private DensityFunction erosionDensityFunction;

	@Shadow
	@Final
	@Mutable
	private DensityFunction depthDensityFunction;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void noisiumed$specializeAquiferDensities(CallbackInfo ci) {
		if (!NoisiumedConfig.aquiferSpecialize()) {
			return;
		}
		this.barrierNoise = DensitySpecializer.specialize(this.barrierNoise);
		this.fluidLevelFloodednessNoise = DensitySpecializer.specialize(this.fluidLevelFloodednessNoise);
		this.fluidLevelSpreadNoise = DensitySpecializer.specialize(this.fluidLevelSpreadNoise);
		this.fluidTypeNoise = DensitySpecializer.specialize(this.fluidTypeNoise);
		this.erosionDensityFunction = DensitySpecializer.specialize(this.erosionDensityFunction);
		this.depthDensityFunction = DensitySpecializer.specialize(this.depthDensityFunction);
		PathMetrics.recordAquiferSpecialize();
	}
}
