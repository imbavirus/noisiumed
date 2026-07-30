package za.co.infernos.noisiumed.mixin.noisechunk;

import net.minecraft.block.BlockState;
import net.minecraft.world.gen.ChainedBlockSource;
import net.minecraft.world.gen.chunk.AquiferSampler;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import za.co.infernos.noisiumed.config.NoisiumedConfig;
import za.co.infernos.noisiumed.density.special.DensitySpecializer;
import za.co.infernos.noisiumed.mixin.ChainedBlockSourceAccessor;
import za.co.infernos.noisiumed.noise.sampler.FastDualBlockSampler;
import za.co.infernos.noisiumed.noise.sampler.FastSingleBlockSampler;
import za.co.infernos.noisiumed.noise.sampler.FastTripleBlockSampler;
import za.co.infernos.noisiumed.path.PathMetrics;

import java.util.List;

/**
 * NC-3/4: accurate fast path for primary block-state density.
 * <p>
 * Vanilla installs {@code cacheAllInCell(add(finalDensity, beardifier))} as the density
 * sampled by the aquifer lambda. We capture <strong>only that</strong> CellCache (not
 * every CACHE_ALL_IN_CELL in the router), then:
 * <ul>
 *   <li>density &gt; 0 → solid early-out (skip full aquifer), optional ore secondary</li>
 *   <li>else → {@code aquifer.apply(pos, density)} then ore</li>
 * </ul>
 * Disable: {@code -Dnoisiumed.cell.density.grid=false}
 */
@Mixin(value = ChunkNoiseSampler.class, priority = 1200)
public abstract class ChunkNoiseSamplerNc3Mixin {
	@Shadow
	@Final
	private AquiferSampler aquiferSampler;

	@Shadow
	@Final
	private ChunkNoiseSampler.BlockStateSampler blockStateSampler;

	@Shadow
	@Final
	private int horizontalCellBlockCount;

	@Shadow
	@Final
	private int verticalCellBlockCount;

	@Shadow
	private int cellBlockX;
	@Shadow
	private int cellBlockY;
	@Shadow
	private int cellBlockZ;

	/** CellCache for finalDensity+beard only. */
	@Unique
	private DensityFunction noisiumed$primaryCellDensity;

	@Unique
	private double[] noisiumed$primaryDensityCache;

	@Unique
	private @Nullable ChunkNoiseSampler.BlockStateSampler noisiumed$secondarySampler;

	@Unique
	private boolean noisiumed$cellGridSample;

	/**
	 * Capture CellCache for {@code cacheAllInCell(add(final, beard))} only — matches the
	 * density vanilla binds into the primary block-state sampler.
	 */
	@Inject(method = "getActualDensityFunctionImpl", at = @At("RETURN"))
	private void noisiumed$capturePrimaryCellCache(
			DensityFunction function,
			CallbackInfoReturnable<DensityFunction> cir
	) {
		if (!(function instanceof DensityFunctionTypes.Wrapping wrapping)) {
			return;
		}
		if (wrapping.type() != DensityFunctionTypes.Wrapping.Type.CACHE_ALL_IN_CELL) {
			return;
		}
		if (!DensitySpecializer.isBeardifiedFinalDensity(wrapping.wrapped())) {
			return;
		}
		this.noisiumed$primaryCellDensity = cir.getReturnValue();
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void noisiumed$nc3WireCellGrid(CallbackInfo ci) {
		if (!NoisiumedConfig.cellDensityGrid()) {
			return;
		}
		DensityFunction cell = this.noisiumed$primaryCellDensity;
		if (cell == null) {
			return;
		}
		double[] cache;
		try {
			cache = ((CellCacheAccess) cell).noisiumed$getCache();
		} catch (ClassCastException ex) {
			return;
		}
		int w = this.horizontalCellBlockCount;
		int h = this.verticalCellBlockCount;
		if (cache == null || cache.length != w * w * h) {
			return;
		}
		this.noisiumed$primaryDensityCache = cache;

		ChunkNoiseSampler.BlockStateSampler sampler = this.blockStateSampler;
		if (sampler instanceof FastDualBlockSampler dual) {
			this.noisiumed$secondarySampler = dual.second();
		} else if (sampler instanceof FastTripleBlockSampler) {
			this.noisiumed$primaryDensityCache = null;
			return;
		} else if (sampler instanceof FastSingleBlockSampler) {
			this.noisiumed$secondarySampler = null;
		} else if (sampler instanceof ChainedBlockSource chained) {
			List<ChunkNoiseSampler.BlockStateSampler> list =
					((ChainedBlockSourceAccessor) (Object) chained).noisiumed$getSamplers();
			if (list.size() >= 3) {
				this.noisiumed$primaryDensityCache = null;
				return;
			}
			if (list.size() == 2) {
				this.noisiumed$secondarySampler = list.get(1);
			}
		}

		this.noisiumed$cellGridSample = true;
		PathMetrics.recordNc3CellGrid();
	}

	/**
	 * @author Infernos
	 * @reason Cell-cache density for finalDensity+beard + solid early-out + monomorphic aquifer.
	 */
	@Overwrite
	@Nullable
	public BlockState sampleBlockState() {
		if (this.noisiumed$cellGridSample) {
			final int w = this.horizontalCellBlockCount;
			final int h = this.verticalCellBlockCount;
			final int i = this.cellBlockX;
			final int j = this.cellBlockY;
			final int k = this.cellBlockZ;
			// Vanilla CellCache falls back to delegate.sample when indices are OOB.
			if (i < 0 || j < 0 || k < 0 || i >= w || j >= h || k >= w) {
				return this.blockStateSampler.sample((ChunkNoiseSampler) (Object) this);
			}
			final double[] cache = this.noisiumed$primaryDensityCache;
			// Same layout as CacheAllInCell.sample / fillAllDirectly (y high→low, then x, then z).
			final double density = cache[((h - 1 - j) * w + i) * w + k];
			final ChunkNoiseSampler self = (ChunkNoiseSampler) (Object) this;

			// NC-4: vanilla NoiseBasedAquifer returns null immediately when density > 0 (solid).
			if (density > 0.0) {
				AquiferSampler aquifer = this.aquiferSampler;
				if (aquifer instanceof AquiferImplAccess access) {
					access.noisiumed$setNeedsFluidTick(false);
				}
				ChunkNoiseSampler.BlockStateSampler secondary = this.noisiumed$secondarySampler;
				return secondary != null ? secondary.sample(self) : null;
			}

			BlockState state = this.aquiferSampler.apply(self, density);
			if (state != null) {
				return state;
			}
			ChunkNoiseSampler.BlockStateSampler secondary = this.noisiumed$secondarySampler;
			if (secondary != null) {
				return secondary.sample(self);
			}
			return null;
		}
		return this.blockStateSampler.sample((ChunkNoiseSampler) (Object) this);
	}
}
