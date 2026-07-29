package za.co.infernos.noisiumed.noise.sampler;

import net.minecraft.block.BlockState;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Monomorphic length-1 block-state chain (no List virtual).
 */
public final class FastSingleBlockSampler implements ChunkNoiseSampler.BlockStateSampler {
	private final ChunkNoiseSampler.BlockStateSampler only;

	public FastSingleBlockSampler(@NotNull ChunkNoiseSampler.BlockStateSampler only) {
		this.only = only;
	}

	@Override
	@Nullable
	public BlockState sample(DensityFunction.NoisePos pos) {
		return this.only.sample(pos);
	}
}
