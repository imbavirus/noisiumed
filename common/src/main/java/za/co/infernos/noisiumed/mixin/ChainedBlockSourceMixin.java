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

import java.util.List;

@Mixin(ChainedBlockSource.class)
public abstract class ChainedBlockSourceMixin {
	@Shadow
	@Final
	private List<ChunkNoiseSampler.BlockStateSampler> samplers;

	/**
	 * @author Steveplays28, Infernos
	 * @reason Micro-optimisation: indexed List access, early exit.
	 */
	@Overwrite
	@Nullable
	@SuppressWarnings("ForLoopReplaceableByForEach")
	public BlockState sample(DensityFunction.NoisePos pos) {
		final List<ChunkNoiseSampler.BlockStateSampler> local = this.samplers;
		final int size = local.size();
		for (int i = 0; i < size; i++) {
			BlockState blockState = local.get(i).sample(pos);
			if (blockState != null) {
				return blockState;
			}
		}
		return null;
	}
}
