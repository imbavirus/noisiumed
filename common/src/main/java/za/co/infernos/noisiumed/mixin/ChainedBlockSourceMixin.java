package za.co.infernos.noisiumed.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.world.gen.ChainedBlockSource;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChainedBlockSource.class)
public abstract class ChainedBlockSourceMixin {
	@Shadow
	@Final
	private ChunkNoiseSampler.BlockStateSampler[] samplers;

	/**
	 * @author Steveplays28
	 * @reason Micro-optimisation
	 */
	@Overwrite
	@Nullable
	public BlockState sample(DensityFunction.NoisePos pos) {
		for (ChunkNoiseSampler.BlockStateSampler sampler : this.samplers) {
			BlockState blockState = sampler.sample(pos);
			if (blockState != null) {
				return blockState;
			}
		}

		return null;
	}
}
