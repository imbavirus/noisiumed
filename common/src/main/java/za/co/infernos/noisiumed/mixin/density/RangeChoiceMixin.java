package za.co.infernos.noisiumed.mixin.density;

import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Range-choice branching. sample/fill → compute/fillArray.
 */
@Mixin(DensityFunctionTypes.RangeChoice.class)
public abstract class RangeChoiceMixin implements DensityFunction {
	@Shadow
	@Final
	private DensityFunction input;

	@Shadow
	@Final
	private double minInclusive;

	@Shadow
	@Final
	private double maxExclusive;

	@Shadow
	@Final
	private DensityFunction whenInRange;

	@Shadow
	@Final
	private DensityFunction whenOutOfRange;

	/**
	 * @author Infernos
	 * @reason Same branch as vanilla, hoisted fields.
	 */
	@Overwrite
	@Override
	public double sample(DensityFunction.NoisePos pos) {
		double v = this.input.sample(pos);
		return (v >= this.minInclusive && v < this.maxExclusive)
				? this.whenInRange.sample(pos)
				: this.whenOutOfRange.sample(pos);
	}

	/**
	 * @author Infernos
	 * @reason Batch input then branch with hoisted bounds/branches.
	 */
	@Overwrite
	@Override
	public void fill(double[] densities, DensityFunction.EachApplier applier) {
		this.input.fill(densities, applier);
		double min = this.minInclusive;
		double max = this.maxExclusive;
		DensityFunction in = this.whenInRange;
		DensityFunction out = this.whenOutOfRange;
		for (int i = 0, n = densities.length; i < n; i++) {
			double v = densities[i];
			densities[i] = (v >= min && v < max)
					? in.sample(applier.at(i))
					: out.sample(applier.at(i));
		}
	}
}
