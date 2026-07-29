package za.co.infernos.noisiumed.noise.sampler;

import net.minecraft.block.BlockState;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Monomorphic length-3 chain (rare; keeps List out of the hot path).
 */
public final class FastTripleBlockSampler implements ChunkNoiseSampler.BlockStateSampler {
	private final ChunkNoiseSampler.BlockStateSampler a;
	private final ChunkNoiseSampler.BlockStateSampler b;
	private final ChunkNoiseSampler.BlockStateSampler c;

	public FastTripleBlockSampler(
			@NotNull ChunkNoiseSampler.BlockStateSampler a,
			@NotNull ChunkNoiseSampler.BlockStateSampler b,
			@NotNull ChunkNoiseSampler.BlockStateSampler c
	) {
		this.a = a;
		this.b = b;
		this.c = c;
	}

	@Override
	@Nullable
	public BlockState sample(DensityFunction.NoisePos pos) {
		BlockState state = this.a.sample(pos);
		if (state != null) {
			return state;
		}
		state = this.b.sample(pos);
		return state != null ? state : this.c.sample(pos);
	}
}
