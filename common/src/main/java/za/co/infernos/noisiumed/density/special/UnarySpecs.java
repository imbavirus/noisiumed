package za.co.infernos.noisiumed.density.special;

import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.jetbrains.annotations.NotNull;

/**
 * Monomorphic unary transforms (yarn UnaryOperation / Mojmap Mapped).
 */
public final class UnarySpecs {
	private UnarySpecs() {}

	abstract static class Unary extends SpecDensity {
		protected final DensityFunction input;

		Unary(@NotNull DensityFunction original, @NotNull DensityFunction input, double min, double max) {
			super(original, min, max);
			this.input = input;
		}

		abstract double map(double v);

		@Override
		public double sample(NoisePos pos) {
			return map(this.input.sample(pos));
		}

		@Override
		public void fill(double[] densities, EachApplier applier) {
			this.input.fill(densities, applier);
			for (int i = 0, n = densities.length; i < n; i++) {
				densities[i] = map(densities[i]);
			}
		}
	}

	public static final class Abs extends Unary {
		public Abs(DensityFunction original, DensityFunction input, double min, double max) {
			super(original, input, min, max);
		}

		@Override
		double map(double v) {
			return Math.abs(v);
		}
	}

	public static final class Square extends Unary {
		public Square(DensityFunction original, DensityFunction input, double min, double max) {
			super(original, input, min, max);
		}

		@Override
		double map(double v) {
			return v * v;
		}
	}

	public static final class Cube extends Unary {
		public Cube(DensityFunction original, DensityFunction input, double min, double max) {
			super(original, input, min, max);
		}

		@Override
		double map(double v) {
			return v * v * v;
		}
	}

	public static final class HalfNegative extends Unary {
		public HalfNegative(DensityFunction original, DensityFunction input, double min, double max) {
			super(original, input, min, max);
		}

		@Override
		double map(double v) {
			return v > 0.0 ? v : v * 0.5;
		}
	}

	public static final class QuarterNegative extends Unary {
		public QuarterNegative(DensityFunction original, DensityFunction input, double min, double max) {
			super(original, input, min, max);
		}

		@Override
		double map(double v) {
			return v > 0.0 ? v : v * 0.25;
		}
	}

	public static final class Squeeze extends Unary {
		public Squeeze(DensityFunction original, DensityFunction input, double min, double max) {
			super(original, input, min, max);
		}

		@Override
		double map(double v) {
			double c = MathHelper.clamp(v, -1.0, 1.0);
			return (c / 2.0) - (c * c * c / 24.0);
		}
	}
}
