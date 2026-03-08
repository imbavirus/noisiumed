package za.co.infernos.noisiumed.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.world.gen.ChainedBlockSource;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChainedBlockSource.class)
public abstract class ChainedBlockSourceMixin {
	@Unique
	private ChunkNoiseSampler.BlockStateSampler[] noisium$samplersArray;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void noisium$onInit(List<ChunkNoiseSampler.BlockStateSampler> samplers, CallbackInfo ci) {
		this.noisium$samplersArray = samplers.toArray(new ChunkNoiseSampler.BlockStateSampler[0]);
	}

	/**
	 * @author Steveplays28
	 * @reason Micro-optimisation using an array instead of a List
	 */
	@Overwrite
	@Nullable
	public BlockState sample(DensityFunction.NoisePos pos) {
		for (ChunkNoiseSampler.BlockStateSampler sampler : this.noisium$samplersArray) {
			BlockState blockState = sampler.sample(pos);
			if (blockState != null) {
				return blockState;
			}
		}

		return null;
	}
}


