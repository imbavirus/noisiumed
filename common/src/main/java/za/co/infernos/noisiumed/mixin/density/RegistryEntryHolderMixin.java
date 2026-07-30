package za.co.infernos.noisiumed.mixin.density;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Cache RegistryEntry unwrap for density sampling
 * (yarn RegistryEntryHolder → Mojmap HolderHolder).
 */
@Mixin(DensityFunctionTypes.RegistryEntryHolder.class)
public abstract class RegistryEntryHolderMixin implements DensityFunction {
	@Shadow
	@Final
	private RegistryEntry<DensityFunction> function;

	@Unique
	private DensityFunction noisiumed$resolved;

	@Unique
	private DensityFunction noisiumed$resolve() {
		DensityFunction resolved = this.noisiumed$resolved;
		if (resolved == null) {
			// Cache unwrap only. Do not specialize shared holder graphs (can diverge from
			// per-NoiseChunk wrap order / golden hashes).
			resolved = this.function.value();
			this.noisiumed$resolved = resolved;
		}
		return resolved;
	}

	/**
	 * @author Infernos
	 * @reason Cache RegistryEntry#value for every density sample.
	 */
	@Overwrite
	@Override
	public double sample(DensityFunction.NoisePos pos) {
		return this.noisiumed$resolve().sample(pos);
	}

	/**
	 * @author Infernos
	 * @reason Cache RegistryEntry#value for bulk fill.
	 */
	@Overwrite
	@Override
	public void fill(double[] densities, DensityFunction.EachApplier applier) {
		this.noisiumed$resolve().fill(densities, applier);
	}

	/**
	 * @author Infernos
	 * @reason Cache for minValue during graph setup.
	 */
	@Overwrite
	@Override
	public double minValue() {
		return this.function.hasKeyAndValue() ? this.noisiumed$resolve().minValue() : Double.NEGATIVE_INFINITY;
	}

	/**
	 * @author Infernos
	 * @reason Cache for maxValue during graph setup.
	 */
	@Overwrite
	@Override
	public double maxValue() {
		return this.function.hasKeyAndValue() ? this.noisiumed$resolve().maxValue() : Double.POSITIVE_INFINITY;
	}
}
