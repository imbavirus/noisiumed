package za.co.infernos.noisiumed.mixin.noisechunk;

import net.minecraft.util.math.random.RandomSplitter;
import net.minecraft.world.gen.OreVeinSampler;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import za.co.infernos.noisiumed.noise.sampler.FastOreVeinSampler;
import za.co.infernos.noisiumed.path.PathMetrics;

/**
 * NC-5 / A3: replace OreVeinSampler lambda with monomorphic {@link FastOreVeinSampler}.
 */
@Mixin(OreVeinSampler.class)
public abstract class OreVeinSamplerMixin {
	@Inject(method = "create", at = @At("HEAD"), cancellable = true)
	private static void noisiumed$fastOre(
			DensityFunction veinToggle,
			DensityFunction veinRidged,
			DensityFunction veinGap,
			RandomSplitter randomSplitter,
			CallbackInfoReturnable<ChunkNoiseSampler.BlockStateSampler> cir
	) {
		cir.setReturnValue(new FastOreVeinSampler(veinToggle, veinRidged, veinGap, randomSplitter));
		PathMetrics.recordNc5Ore();
	}
}
