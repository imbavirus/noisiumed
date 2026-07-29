package za.co.infernos.noisiumed.mixin.noisechunk;

import net.minecraft.world.gen.ChainedBlockSource;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import za.co.infernos.noisiumed.mixin.ChainedBlockSourceAccessor;
import za.co.infernos.noisiumed.noise.sampler.FastDualBlockSampler;
import za.co.infernos.noisiumed.noise.sampler.FastSingleBlockSampler;
import za.co.infernos.noisiumed.noise.sampler.FastTripleBlockSampler;
import za.co.infernos.noisiumed.path.PathMetrics;

import java.util.List;

/**
 * NC-1: after NoiseChunk construction —
 * <ul>
 *   <li>Replace length-1/2/3 {@link ChainedBlockSource} with monomorphic samplers</li>
 *   <li>Snapshot interpolators into an array for indexed Y/X/Z updates</li>
 * </ul>
 */
@Mixin(ChunkNoiseSampler.class)
public abstract class ChunkNoiseSamplerNc1Mixin {
	@Shadow
	@Final
	@Mutable
	private ChunkNoiseSampler.BlockStateSampler blockStateSampler;

	@Shadow
	@Final
	private List<?> interpolators;

	@Shadow
	private int cellBlockY;
	@Shadow
	private int cellBlockX;
	@Shadow
	private int cellBlockZ;
	@Shadow
	private int startBlockY;
	@Shadow
	private int startBlockX;
	@Shadow
	private int startBlockZ;
	@Shadow
	private long sampleUniqueIndex;

	@Unique
	private Object[] noisiumed$interpolatorArray;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void noisiumed$nc1Finish(CallbackInfo ci) {
		// Monomorphic block-state chain
		if (this.blockStateSampler instanceof ChainedBlockSource chained) {
			List<ChunkNoiseSampler.BlockStateSampler> list =
					((ChainedBlockSourceAccessor) (Object) chained).noisiumed$getSamplers();
			int n = list.size();
			if (n == 1) {
				this.blockStateSampler = new FastSingleBlockSampler(list.get(0));
				PathMetrics.recordNc1Sampler(1);
			} else if (n == 2) {
				this.blockStateSampler = new FastDualBlockSampler(list.get(0), list.get(1));
				PathMetrics.recordNc1Sampler(2);
			} else if (n == 3) {
				this.blockStateSampler = new FastTripleBlockSampler(list.get(0), list.get(1), list.get(2));
				PathMetrics.recordNc1Sampler(3);
			}
		}

		// Interpolator array snapshot (list is fully populated after wrap in ctor)
		List<?> interps = this.interpolators;
		if (interps != null && !interps.isEmpty()) {
			this.noisiumed$interpolatorArray = interps.toArray();
		} else {
			this.noisiumed$interpolatorArray = new Object[0];
		}
	}

	/**
	 * @author Infernos
	 * @reason Indexed interpolator array (no List.forEach).
	 */
	@org.spongepowered.asm.mixin.Overwrite
	public void interpolateY(int blockY, double deltaY) {
		this.cellBlockY = blockY - this.startBlockY;
		Object[] arr = this.noisiumed$interpolatorArray;
		if (arr == null) {
			arr = this.interpolators.toArray();
			this.noisiumed$interpolatorArray = arr;
		}
		//noinspection ForLoopReplaceableByForEach
		for (int i = 0, n = arr.length; i < n; i++) {
			((DensityInterpolatorAccess) arr[i]).noisiumed$interpolateY(deltaY);
		}
	}

	/**
	 * @author Infernos
	 * @reason Indexed interpolator array.
	 */
	@org.spongepowered.asm.mixin.Overwrite
	public void interpolateX(int blockX, double deltaX) {
		this.cellBlockX = blockX - this.startBlockX;
		Object[] arr = this.noisiumed$interpolatorArray;
		if (arr == null) {
			arr = this.interpolators.toArray();
			this.noisiumed$interpolatorArray = arr;
		}
		//noinspection ForLoopReplaceableByForEach
		for (int i = 0, n = arr.length; i < n; i++) {
			((DensityInterpolatorAccess) arr[i]).noisiumed$interpolateX(deltaX);
		}
	}

	/**
	 * @author Infernos
	 * @reason Indexed interpolator array + sample counter.
	 */
	@org.spongepowered.asm.mixin.Overwrite
	public void interpolateZ(int blockZ, double deltaZ) {
		this.cellBlockZ = blockZ - this.startBlockZ;
		this.sampleUniqueIndex++;
		Object[] arr = this.noisiumed$interpolatorArray;
		if (arr == null) {
			arr = this.interpolators.toArray();
			this.noisiumed$interpolatorArray = arr;
		}
		//noinspection ForLoopReplaceableByForEach
		for (int i = 0, n = arr.length; i < n; i++) {
			((DensityInterpolatorAccess) arr[i]).noisiumed$interpolateZ(deltaZ);
		}
	}
}
