package za.co.infernos.noisiumed.mixin.density;

import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Linear mul/add (yarn LinearOperation → Mojmap MulOrAdd).
 * Yarn {@code apply(double)} → Mojmap {@code transform(double)}.
 */
@Mixin(DensityFunctionTypes.LinearOperation.class)
public abstract class LinearOperationMixin {
	@Shadow
	@Final
	private DensityFunctionTypes.LinearOperation.SpecificType specificType;

	@Shadow
	@Final
	private double argument;

	/**
	 * @author Infernos
	 * @reason Direct mul/add (ordinal 0 = MUL, 1 = ADD).
	 */
	@Overwrite
	public double apply(double density) {
		return this.specificType.ordinal() == 0
				? density * this.argument
				: density + this.argument;
	}
}
