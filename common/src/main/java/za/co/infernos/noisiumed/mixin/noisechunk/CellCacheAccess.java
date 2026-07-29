package za.co.infernos.noisiumed.mixin.noisechunk;

import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Yarn CellCache → Mojmap CacheAllInCell.
 */
@Mixin(targets = "net.minecraft.world.gen.chunk.ChunkNoiseSampler$CellCache")
public interface CellCacheAccess {
	@Accessor("delegate")
	DensityFunction noisiumed$getDelegate();

	@Accessor("cache")
	double[] noisiumed$getCache();
}
