package za.co.infernos.noisiumed.density.special;

import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.jetbrains.annotations.NotNull;
import za.co.infernos.noisiumed.density.DensityScratch;

/**
 * Monomorphic binary ops (yarn BinaryOperation / Mojmap Ap2).
 * MIN/MAX short-circuit matches Mojmap ({@code <}/{@code >}).
 */
public final class BinarySpecs {
	private BinarySpecs() {}

	public static final class Add extends SpecDensity {
		private final DensityFunction a;
		private final DensityFunction b;

		public Add(DensityFunction original, DensityFunction a, DensityFunction b, double min, double max) {
			super(original, min, max);
			this.a = a;
			this.b = b;
		}

		public DensityFunction left() {
			return this.a;
		}

		public DensityFunction right() {
			return this.b;
		}

		@Override
		public double sample(NoisePos pos) {
			return this.a.sample(pos) + this.b.sample(pos);
		}

		@Override
		public void fill(double[] densities, EachApplier applier) {
			this.a.fill(densities, applier);
			int len = densities.length;
			double[] temp = DensityScratch.acquireFillTemp(len);
			try {
				this.b.fill(temp, applier);
				for (int i = 0; i < len; i++) {
					densities[i] += temp[i];
				}
			} finally {
				DensityScratch.releaseFillTemp();
			}
		}
	}

	public static final class Mul extends SpecDensity {
		private final DensityFunction a;
		private final DensityFunction b;

		public Mul(DensityFunction original, DensityFunction a, DensityFunction b, double min, double max) {
			super(original, min, max);
			this.a = a;
			this.b = b;
		}

		@Override
		public double sample(NoisePos pos) {
			double v = this.a.sample(pos);
			return v == 0.0 ? 0.0 : v * this.b.sample(pos);
		}

		@Override
		public void fill(double[] densities, EachApplier applier) {
			this.a.fill(densities, applier);
			// Keep vanilla short-circuit: do not sample B when A is 0 (CacheOnce / order parity).
			DensityFunction b = this.b;
			for (int i = 0, n = densities.length; i < n; i++) {
				double v = densities[i];
				densities[i] = v == 0.0 ? 0.0 : v * b.sample(applier.at(i));
			}
		}
	}

	public static final class Min extends SpecDensity {
		private final DensityFunction a;
		private final DensityFunction b;

		public Min(DensityFunction original, DensityFunction a, DensityFunction b, double min, double max) {
			super(original, min, max);
			this.a = a;
			this.b = b;
		}

		@Override
		public double sample(NoisePos pos) {
			double v = this.a.sample(pos);
			return v < this.b.minValue() ? v : Math.min(v, this.b.sample(pos));
		}

		@Override
		public void fill(double[] densities, EachApplier applier) {
			this.a.fill(densities, applier);
			double min2 = this.b.minValue();
			DensityFunction b = this.b;
			for (int i = 0, n = densities.length; i < n; i++) {
				double v = densities[i];
				densities[i] = v < min2 ? v : Math.min(v, b.sample(applier.at(i)));
			}
		}
	}

	public static final class Max extends SpecDensity {
		private final DensityFunction a;
		private final DensityFunction b;

		public Max(DensityFunction original, DensityFunction a, DensityFunction b, double min, double max) {
			super(original, min, max);
			this.a = a;
			this.b = b;
		}

		@Override
		public double sample(NoisePos pos) {
			double v = this.a.sample(pos);
			return v > this.b.maxValue() ? v : Math.max(v, this.b.sample(pos));
		}

		@Override
		public void fill(double[] densities, EachApplier applier) {
			this.a.fill(densities, applier);
			double max2 = this.b.maxValue();
			DensityFunction b = this.b;
			for (int i = 0, n = densities.length; i < n; i++) {
				double v = densities[i];
				densities[i] = v > max2 ? v : Math.max(v, b.sample(applier.at(i)));
			}
		}
	}
}
