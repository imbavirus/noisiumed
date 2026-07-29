package za.co.infernos.noisiumed.mixin.density;

import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import za.co.infernos.noisiumed.density.DensityScratch;

/**
 * Binary ops (yarn BinaryOperation → Mojmap Ap2).
 * sample/fill remap to compute/fillArray.
 * MIN/MAX short-circuit matches Mojmap ({@code <}/{@code >}).
 */
@Mixin(DensityFunctionTypes.BinaryOperation.class)
public abstract class BinaryOperationMixin implements DensityFunction {
	@Shadow
	@Final
	private DensityFunctionTypes.BinaryOperationLike.Type type;

	@Shadow
	@Final
	private DensityFunction argument1;

	@Shadow
	@Final
	private DensityFunction argument2;

	/**
	 * @author Infernos
	 * @reason Same short-circuits as vanilla, lighter control flow.
	 */
	@Overwrite
	@Override
	public double sample(DensityFunction.NoisePos pos) {
		double a = this.argument1.sample(pos);
		return switch (this.type.ordinal()) {
			case 0 -> a + this.argument2.sample(pos);
			case 1 -> a == 0.0 ? 0.0 : a * this.argument2.sample(pos);
			case 2 -> a < this.argument2.minValue() ? a : Math.min(a, this.argument2.sample(pos));
			case 3 -> a > this.argument2.maxValue() ? a : Math.max(a, this.argument2.sample(pos));
			default -> throw new MatchException(null, null);
		};
	}

	/**
	 * @author Infernos
	 * @reason ThreadLocal temp for ADD fill (no per-call {@code new double[]}).
	 */
	@Overwrite
	@Override
	public void fill(double[] densities, DensityFunction.EachApplier applier) {
		this.argument1.fill(densities, applier);
		int len = densities.length;
		switch (this.type.ordinal()) {
			case 0 -> {
				double[] temp = DensityScratch.fillTemp(len);
				this.argument2.fill(temp, applier);
				for (int i = 0; i < len; i++) {
					densities[i] += temp[i];
				}
			}
			case 1 -> {
				for (int i = 0; i < len; i++) {
					double v = densities[i];
					densities[i] = v == 0.0 ? 0.0 : v * this.argument2.sample(applier.at(i));
				}
			}
			case 2 -> {
				double min2 = this.argument2.minValue();
				for (int i = 0; i < len; i++) {
					double v = densities[i];
					densities[i] = v < min2 ? v : Math.min(v, this.argument2.sample(applier.at(i)));
				}
			}
			case 3 -> {
				double max2 = this.argument2.maxValue();
				for (int i = 0; i < len; i++) {
					double v = densities[i];
					densities[i] = v > max2 ? v : Math.max(v, this.argument2.sample(applier.at(i)));
				}
			}
			default -> throw new MatchException(null, null);
		}
	}
}
