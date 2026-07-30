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
import za.co.infernos.noisiumed.noise.CellGridPositionAccess;
import za.co.infernos.noisiumed.noise.sampler.FastDualBlockSampler;
import za.co.infernos.noisiumed.noise.sampler.FastOreVeinSampler;
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
 * Also implements {@link CellGridPositionAccess}: position-only cell updates without
 * interpolator lerp (primary density is cached; aquifer/ore use block coords).
 * Disable: {@code -Dnoisiumed.cell.density.grid=false}
 */
@Mixin(value = ChunkNoiseSampler.class, priority = 1200)
public abstract class ChunkNoiseSamplerNc3Mixin implements CellGridPositionAccess {
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
	@Shadow
	private int startBlockX;
	@Shadow
	private int startBlockY;
	@Shadow
	private int startBlockZ;
	@Shadow
	private long sampleUniqueIndex;

	/** CellCache for finalDensity+beard only. */
	@Unique
	private DensityFunction noisiumed$primaryCellDensity;

	@Unique
	private double[] noisiumed$primaryDensityCache;

	@Unique
	private @Nullable ChunkNoiseSampler.BlockStateSampler noisiumed$secondarySampler;

	@Unique
	private boolean noisiumed$cellGridSample;

	/** Cached when aquifer is Impl — avoid per-block instanceof on solid early-out. */
	@Unique
	private @Nullable AquiferImplAccess noisiumed$aquiferAccess;

	/** Precomputed cell strides for density index (w, h, w*w). */
	@Unique
	private int noisiumed$cellW;
	@Unique
	private int noisiumed$cellH;
	@Unique
	private int noisiumed$cellStrideY;

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
		this.noisiumed$cellW = w;
		this.noisiumed$cellH = h;
		this.noisiumed$cellStrideY = w * w;

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

		AquiferSampler aquifer = this.aquiferSampler;
		this.noisiumed$aquiferAccess = aquifer instanceof AquiferImplAccess access ? access : null;

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
		if (!this.noisiumed$cellGridSample) {
			return this.blockStateSampler.sample((ChunkNoiseSampler) (Object) this);
		}
		final int w = this.noisiumed$cellW;
		final int h = this.noisiumed$cellH;
		final int i = this.cellBlockX;
		final int j = this.cellBlockY;
		final int k = this.cellBlockZ;
		// Vanilla CellCache falls back to delegate.sample when indices are OOB.
		if (i < 0 || j < 0 || k < 0 || i >= w || j >= h || k >= w) {
			return this.blockStateSampler.sample((ChunkNoiseSampler) (Object) this);
		}
		// Same layout as CacheAllInCell.sample / fillAllDirectly (y high→low, then x, then z).
		final double density = this.noisiumed$primaryDensityCache[
				(h - 1 - j) * this.noisiumed$cellStrideY + i * w + k];
		final ChunkNoiseSampler self = (ChunkNoiseSampler) (Object) this;
		final ChunkNoiseSampler.BlockStateSampler secondary = this.noisiumed$secondarySampler;

		// NC-4: vanilla NoiseBasedAquifer returns null immediately when density > 0 (solid).
		if (density > 0.0) {
			AquiferImplAccess aq = this.noisiumed$aquiferAccess;
			if (aq != null) {
				aq.noisiumed$setNeedsFluidTick(false);
			}
			if (secondary == null) {
				return null;
			}
			// Skip ore DF entirely outside the copper/iron Y union (parity: null).
			int blockY = this.startBlockY + j;
			if (!FastOreVeinSampler.mayHaveVeinAtY(blockY)) {
				return null;
			}
			return secondary.sample(self);
		}

		BlockState state = this.aquiferSampler.apply(self, density);
		if (state != null) {
			return state;
		}
		if (secondary == null) {
			return null;
		}
		int blockY = this.startBlockY + j;
		if (!FastOreVeinSampler.mayHaveVeinAtY(blockY)) {
			return null;
		}
		return secondary.sample(self);
	}

	@Override
	public boolean noisiumed$cellGridActive() {
		return this.noisiumed$cellGridSample;
	}

	@Override
	public double[] noisiumed$primaryDensityCache() {
		return this.noisiumed$primaryDensityCache;
	}

	@Override
	public int noisiumed$cellW() {
		return this.noisiumed$cellW;
	}

	@Override
	public int noisiumed$cellH() {
		return this.noisiumed$cellH;
	}

	@Override
	@Nullable
	public BlockState noisiumed$materializeFromDensity(double density) {
		final ChunkNoiseSampler self = (ChunkNoiseSampler) (Object) this;
		final ChunkNoiseSampler.BlockStateSampler secondary = this.noisiumed$secondarySampler;
		if (density > 0.0) {
			AquiferImplAccess aq = this.noisiumed$aquiferAccess;
			if (aq != null) {
				aq.noisiumed$setNeedsFluidTick(false);
			}
			if (secondary == null) {
				return null;
			}
			int blockY = this.startBlockY + this.cellBlockY;
			if (!FastOreVeinSampler.mayHaveVeinAtY(blockY)) {
				return null;
			}
			return secondary.sample(self);
		}
		BlockState state = this.aquiferSampler.apply(self, density);
		if (state != null) {
			return state;
		}
		if (secondary == null) {
			return null;
		}
		int blockY = this.startBlockY + this.cellBlockY;
		if (!FastOreVeinSampler.mayHaveVeinAtY(blockY)) {
			return null;
		}
		return secondary.sample(self);
	}

	@Override
	public void noisiumed$positionY(int blockY) {
		this.cellBlockY = blockY - this.startBlockY;
	}

	@Override
	public void noisiumed$positionX(int blockX) {
		this.cellBlockX = blockX - this.startBlockX;
	}

	@Override
	public void noisiumed$positionZ(int blockZ) {
		this.cellBlockZ = blockZ - this.startBlockZ;
		this.sampleUniqueIndex++;
	}
}
