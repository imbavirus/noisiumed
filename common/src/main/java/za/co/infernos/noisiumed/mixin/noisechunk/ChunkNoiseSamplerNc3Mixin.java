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
import za.co.infernos.noisiumed.mixin.ChainedBlockSourceAccessor;
import za.co.infernos.noisiumed.noise.sampler.FastDualBlockSampler;
import za.co.infernos.noisiumed.noise.sampler.FastSingleBlockSampler;
import za.co.infernos.noisiumed.noise.sampler.FastTripleBlockSampler;
import za.co.infernos.noisiumed.path.PathMetrics;

import java.util.List;

/**
 * NC-3: {@code sampleBlockState} reads primary density from the {@code CellCache}
 * produced by the last {@code CACHE_ALL_IN_CELL} wrap (finalDensity+beardifier),
 * then {@code AquiferSampler.apply}.
 * <p>
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

	/** Last CACHE_ALL_IN_CELL wrap result (CellCache), if uniquely identified. */
	@Unique
	private DensityFunction noisiumed$capturedCellDensity;

	/** How many CACHE_ALL_IN_CELL wraps we saw; >1 means capture is ambiguous — refuse NC-3. */
	@Unique
	private int noisiumed$cellCacheWrapCount;

	@Unique
	private double[] noisiumed$primaryDensityCache;

	@Unique
	private @Nullable ChunkNoiseSampler.BlockStateSampler noisiumed$secondarySampler;

	@Unique
	private boolean noisiumed$cellGridSample;

	/**
	 * Track CellCache instances as the router / finalDensity graph is wrapped.
	 * Only safe when exactly one CACHE_ALL_IN_CELL exists for this NoiseChunk
	 * (otherwise "last capture" can be the wrong density branch).
	 */
	@Inject(method = "getActualDensityFunctionImpl", at = @At("RETURN"))
	private void noisiumed$captureCellCache(
			DensityFunction function,
			CallbackInfoReturnable<DensityFunction> cir
	) {
		if (function instanceof DensityFunctionTypes.Wrapping wrapping
				&& wrapping.type() == DensityFunctionTypes.Wrapping.Type.CACHE_ALL_IN_CELL) {
			this.noisiumed$cellCacheWrapCount++;
			this.noisiumed$capturedCellDensity = cir.getReturnValue();
		}
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void noisiumed$nc3WireCellGrid(CallbackInfo ci) {
		if (!NoisiumedConfig.cellDensityGrid()) {
			return;
		}
		// Ambiguous multi-cache graphs: fall back to vanilla sampleBlockState (accuracy first).
		if (this.noisiumed$cellCacheWrapCount != 1) {
			return;
		}
		DensityFunction cell = this.noisiumed$capturedCellDensity;
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
			// Triple chains are rare and not modeled; stay on vanilla path.
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
	 * @reason Cell-cache density + solid early-out + monomorphic aquifer; optional ore veins.
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
			// Skip the full aquifer implementation; clear fluid-tick flag for parity.
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

