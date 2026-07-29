package za.co.infernos.noisiumed.mixin.noisechunk;

import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * NC-2: faster cell density fill paths.
 * <ul>
 *   <li>{@code fill} (Mojmap {@code fillAllDirectly}) — tight triple loop, local dims</li>
 *   <li>{@code onSampledCellCorners} — indexed interpolator + cell-cache fills</li>
 * </ul>
 */
@Mixin(ChunkNoiseSampler.class)
public abstract class ChunkNoiseSamplerNc2Mixin {
	@Shadow
	@Final
	private List<?> interpolators;

	@Shadow
	@Final
	private List<?> caches;

	@Shadow
	private int cellBlockX;
	@Shadow
	private int cellBlockY;
	@Shadow
	private int cellBlockZ;
	@Shadow
	private int index;
	@Shadow
	private int startBlockY;
	@Shadow
	private int startBlockZ;
	@Shadow
	private int minimumCellY;
	@Shadow
	private int startCellZ;
	@Shadow
	@Final
	private int horizontalCellBlockCount;
	@Shadow
	@Final
	private int verticalCellBlockCount;
	@Shadow
	private boolean isSamplingForCaches;
	@Shadow
	private long cacheOnceUniqueIndex;

	/** Shared with NC-1 when present; may be null if NC-1 not applied first. */
	@Unique
	private Object[] noisiumed$interpolatorArrayNc2;

	@Unique
	private Object[] noisiumed$cellCacheArray;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void noisiumed$nc2Snapshot(CallbackInfo ci) {
		List<?> interps = this.interpolators;
		this.noisiumed$interpolatorArrayNc2 = interps != null && !interps.isEmpty()
				? interps.toArray()
				: new Object[0];
		List<?> c = this.caches;
		this.noisiumed$cellCacheArray = c != null && !c.isEmpty()
				? c.toArray()
				: new Object[0];
	}

	/**
	 * Yarn: {@link DensityFunction.EachApplier#fill(double[], DensityFunction)}.
	 * Mojmap: {@code fillAllDirectly}.
	 *
	 * @author Infernos
	 * @reason Tighter cell fill loop for CacheAllInCell / bulk density.
	 */
	@Overwrite
	public void fill(double[] densities, DensityFunction function) {
		final ChunkNoiseSampler self = (ChunkNoiseSampler) (Object) this;
		final int cellH = this.verticalCellBlockCount;
		final int cellW = this.horizontalCellBlockCount;
		int idx = 0;
		for (int y = cellH - 1; y >= 0; y--) {
			this.cellBlockY = y;
			for (int x = 0; x < cellW; x++) {
				this.cellBlockX = x;
				for (int z = 0; z < cellW; z++) {
					this.cellBlockZ = z;
					densities[idx++] = function.sample(self);
				}
			}
		}
		this.index = idx;
	}

	/**
	 * @author Infernos
	 * @reason Indexed interpolator corner load + cell-cache bulk fill.
	 */
	@Overwrite
	public void onSampledCellCorners(int cellY, int cellZ) {
		Object[] interps = this.noisiumed$interpolatorArrayNc2;
		if (interps == null) {
			interps = this.interpolators.toArray();
			this.noisiumed$interpolatorArrayNc2 = interps;
		}
		//noinspection ForLoopReplaceableByForEach
		for (int i = 0, n = interps.length; i < n; i++) {
			((DensityInterpolatorAccess) interps[i]).noisiumed$onSampledCellCorners(cellY, cellZ);
		}

		this.isSamplingForCaches = true;
		this.startBlockY = (cellY + this.minimumCellY) * this.verticalCellBlockCount;
		this.startBlockZ = (this.startCellZ + cellZ) * this.horizontalCellBlockCount;
		this.cacheOnceUniqueIndex++;

		Object[] cellCaches = this.noisiumed$cellCacheArray;
		if (cellCaches == null) {
			cellCaches = this.caches.toArray();
			this.noisiumed$cellCacheArray = cellCaches;
		}
		//noinspection ForLoopReplaceableByForEach
		for (int i = 0, n = cellCaches.length; i < n; i++) {
			CellCacheAccess cache = (CellCacheAccess) cellCaches[i];
			cache.noisiumed$getDelegate().fill(
					cache.noisiumed$getCache(),
					(DensityFunction.EachApplier) (Object) this
			);
		}

		this.cacheOnceUniqueIndex++;
		this.isSamplingForCaches = false;
	}
}
