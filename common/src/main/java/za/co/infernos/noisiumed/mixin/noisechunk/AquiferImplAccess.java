package za.co.infernos.noisiumed.mixin.noisechunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Yarn {@code AquiferSampler.Impl} → Mojmap {@code Aquifer.NoiseBasedAquifer}.
 */
@Mixin(targets = "net.minecraft.world.gen.chunk.AquiferSampler$Impl")
public interface AquiferImplAccess {
	@Accessor("needsFluidTick")
	@Mutable
	void noisiumed$setNeedsFluidTick(boolean value);
}
