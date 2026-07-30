package za.co.infernos.noisiumed.density.special;

import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.jetbrains.annotations.NotNull;

/**
 * Monomorphic clamp (yarn {@code DensityFunctionTypes.Clamp} / Mojmap Clamp).
 * Matches vanilla: {@code MathHelper.clamp(v, min, max)}.
 */
public final class ClampSpecs {
	private ClampSpecs() {}

	public static final class Clamp extends SpecDensity {
		private final DensityFunction input;
		private final double clampMin;
		private final double clampMax;

		public Clamp(
				@NotNull DensityFunction original,
				@NotNull DensityFunction input,
				double clampMin,
				double clampMax
		) {
			super(original, clampMin, clampMax);
			this.input = input;
			this.clampMin = clampMin;
			this.clampMax = clampMax;
		}

		@Override
		public double sample(NoisePos pos) {
			return MathHelper.clamp(this.input.sample(pos), this.clampMin, this.clampMax);
		}

		@Override
		public void fill(double[] densities, EachApplier applier) {
			this.input.fill(densities, applier);
			final double lo = this.clampMin;
			final double hi = this.clampMax;
			for (int i = 0, n = densities.length; i < n; i++) {
				densities[i] = MathHelper.clamp(densities[i], lo, hi);
			}
		}
	}
}
