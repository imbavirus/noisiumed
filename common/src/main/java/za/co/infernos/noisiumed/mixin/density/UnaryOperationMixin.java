package za.co.infernos.noisiumed.mixin.density;

import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Unary transforms (yarn UnaryOperation → Mojmap Mapped).
 * Type is package-private; accesswidener + exact shadow descriptor required at runtime.
 * Yarn {@code apply(double)} remaps to Mojmap {@code transform(double)}.
 */
@Mixin(DensityFunctionTypes.UnaryOperation.class)
public abstract class UnaryOperationMixin {
	@Shadow
	@Final
	private DensityFunctionTypes.UnaryOperation.Type type;

	/**
	 * @author Infernos
	 * @reason Inline unary transforms with exact vanilla math (ordinal match).
	 */
	@Overwrite
	public double apply(double density) {
		return switch (this.type.ordinal()) {
			case 0 -> Math.abs(density);
			case 1 -> density * density;
			case 2 -> density * density * density;
			case 3 -> density > 0.0 ? density : density * 0.5;
			case 4 -> density > 0.0 ? density : density * 0.25;
			case 5 -> {
				double c = MathHelper.clamp(density, -1.0, 1.0);
				yield (c / 2.0) - (c * c * c / 24.0);
			}
			default -> throw new MatchException(null, null);
		};
	}
}
