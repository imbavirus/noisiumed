package za.co.infernos.noisiumed.noise.sampler;

import net.minecraft.block.BlockState;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Monomorphic length-2 block-state chain (aquifer+density then ore veins).
 */
public final class FastDualBlockSampler implements ChunkNoiseSampler.BlockStateSampler {
	private final ChunkNoiseSampler.BlockStateSampler first;
	private final ChunkNoiseSampler.BlockStateSampler second;

	public FastDualBlockSampler(
			@NotNull ChunkNoiseSampler.BlockStateSampler first,
			@NotNull ChunkNoiseSampler.BlockStateSampler second
	) {
		this.first = first;
		this.second = second;
	}

	@Override
	@Nullable
	public BlockState sample(DensityFunction.NoisePos pos) {
		BlockState state = this.first.sample(pos);
		return state != null ? state : this.second.sample(pos);
	}
}
