package za.co.infernos.noisiumed.density.special;

import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.jetbrains.annotations.NotNull;

/**
 * Monomorphic mul/add-by-constant (yarn LinearOperation / Mojmap MulOrAdd).
 */
public final class LinearSpecs {
	private LinearSpecs() {}

	public static final class MulConst extends SpecDensity {
		private final DensityFunction input;
		private final double k;

		public MulConst(DensityFunction original, DensityFunction input, double k, double min, double max) {
			super(original, min, max);
			this.input = input;
			this.k = k;
		}

		@Override
		public double sample(NoisePos pos) {
			return this.input.sample(pos) * this.k;
		}

		@Override
		public void fill(double[] densities, EachApplier applier) {
			this.input.fill(densities, applier);
			double k = this.k;
			if (k == 0.0) {
				java.util.Arrays.fill(densities, 0.0);
				return;
			}
			if (k == 1.0) {
				return;
			}
			for (int i = 0, n = densities.length; i < n; i++) {
				densities[i] *= k;
			}
		}
	}

	public static final class AddConst extends SpecDensity {
		private final DensityFunction input;
		private final double k;

		public AddConst(DensityFunction original, DensityFunction input, double k, double min, double max) {
			super(original, min, max);
			this.input = input;
			this.k = k;
		}

		@Override
		public double sample(NoisePos pos) {
			return this.input.sample(pos) + this.k;
		}

		@Override
		public void fill(double[] densities, EachApplier applier) {
			this.input.fill(densities, applier);
			double k = this.k;
			if (k == 0.0) {
				return;
			}
			for (int i = 0, n = densities.length; i < n; i++) {
				densities[i] += k;
			}
		}
	}
}
