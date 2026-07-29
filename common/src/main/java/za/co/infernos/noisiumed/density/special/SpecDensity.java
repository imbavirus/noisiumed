package za.co.infernos.noisiumed.density.special;

import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.jetbrains.annotations.NotNull;

/**
 * Base for monomorphic specialized density nodes. Delegates codec / visitor to the
 * original vanilla node so serialization and mapAll stay correct.
 */
public abstract class SpecDensity implements DensityFunction {
	protected final DensityFunction original;
	protected final double minValue;
	protected final double maxValue;

	protected SpecDensity(@NotNull DensityFunction original, double minValue, double maxValue) {
		this.original = original;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	@Override
	public double minValue() {
		return this.minValue;
	}

	@Override
	public double maxValue() {
		return this.maxValue;
	}

	@Override
	public DensityFunction apply(DensityFunctionVisitor visitor) {
		return DensitySpecializer.specialize(this.original.apply(visitor));
	}

	@Override
	public CodecHolder<? extends DensityFunction> getCodecHolder() {
		return this.original.getCodecHolder();
	}
}
