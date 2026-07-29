package za.co.infernos.noisiumed.density.special;

import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.jetbrains.annotations.NotNull;

/**
 * Monomorphic range-choice (yarn RangeChoice).
 * Branch semantics match vanilla: {@code minInclusive <= v < maxExclusive}.
 */
public final class RangeSpecs {
	private RangeSpecs() {}

	public static final class Choice extends SpecDensity {
		private final DensityFunction input;
		private final double minInclusive;
		private final double maxExclusive;
		private final DensityFunction whenInRange;
		private final DensityFunction whenOutOfRange;

		public Choice(
				@NotNull DensityFunction original,
				@NotNull DensityFunction input,
				double minInclusive,
				double maxExclusive,
				@NotNull DensityFunction whenInRange,
				@NotNull DensityFunction whenOutOfRange,
				double min,
				double max
		) {
			super(original, min, max);
			this.input = input;
			this.minInclusive = minInclusive;
			this.maxExclusive = maxExclusive;
			this.whenInRange = whenInRange;
			this.whenOutOfRange = whenOutOfRange;
		}

		@Override
		public double sample(NoisePos pos) {
			double v = this.input.sample(pos);
			return (v >= this.minInclusive && v < this.maxExclusive)
					? this.whenInRange.sample(pos)
					: this.whenOutOfRange.sample(pos);
		}

		@Override
		public void fill(double[] densities, EachApplier applier) {
			this.input.fill(densities, applier);
			final double min = this.minInclusive;
			final double max = this.maxExclusive;
			final DensityFunction in = this.whenInRange;
			final DensityFunction out = this.whenOutOfRange;
			for (int i = 0, n = densities.length; i < n; i++) {
				double v = densities[i];
				densities[i] = (v >= min && v < max)
						? in.sample(applier.at(i))
						: out.sample(applier.at(i));
			}
		}
	}
}
