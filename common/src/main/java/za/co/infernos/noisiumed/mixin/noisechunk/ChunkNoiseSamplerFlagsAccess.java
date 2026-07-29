package za.co.infernos.noisiumed.mixin.noisechunk;

import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Minimal flag access for DensityInterpolator.fill (avoid conflicting with
 * {@link za.co.infernos.noisiumed.mixin.ChunkNoiseSamplerAccessor} invokers).
 */
@Mixin(ChunkNoiseSampler.class)
public interface ChunkNoiseSamplerFlagsAccess {
	@Accessor("isSamplingForCaches")
	boolean noisiumed$isSamplingForCaches();
}
