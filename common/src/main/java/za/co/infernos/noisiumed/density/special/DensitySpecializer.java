package za.co.infernos.noisiumed.density.special;

import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.jetbrains.annotations.NotNull;
import za.co.infernos.noisiumed.config.NoisiumedConfig;
import za.co.infernos.noisiumed.path.PathMetrics;

/**
 * Rewrites hot vanilla density nodes into monomorphic {@link SpecDensity} implementations.
 * Idempotent: already-specialized nodes are returned unchanged.
 */
public final class DensitySpecializer {
	private DensitySpecializer() {}

	/**
	 * @return specialized form, or {@code function} if no rewrite applies
	 */
	public static @NotNull DensityFunction specialize(@NotNull DensityFunction function) {
		if (!NoisiumedConfig.densitySpecialize()) {
			return function;
		}
		if (function instanceof SpecDensity) {
			return function;
		}

		if (function instanceof DensityFunctionTypes.UnaryOperation unary) {
			return specializeUnary(unary);
		}
		if (function instanceof DensityFunctionTypes.LinearOperation linear) {
			return specializeLinear(linear);
		}
		if (function instanceof DensityFunctionTypes.BinaryOperation binary) {
			return specializeBinary(binary);
		}
		if (function instanceof DensityFunctionTypes.RangeChoice range) {
			return specializeRange(range);
		}
		return function;
	}

	/**
	 * Specialize only if the entire tree is pure arithmetic (no noise / wrap / beard).
	 * Safe for aquifer barrier graphs that are math-only.
	 */
	public static @NotNull DensityFunction specializePure(@NotNull DensityFunction function) {
		if (!NoisiumedConfig.densitySpecialize() || !isPureArithmeticTree(function)) {
			return function;
		}
		return specialize(function);
	}

	/**
	 * Vanilla installs primary density as {@code cacheAllInCell(add(finalDensity, beardifier))}.
	 * True when {@code f} is that add (or specialized Add) containing a beard node.
	 */
	public static boolean isBeardifiedFinalDensity(@NotNull DensityFunction f) {
		if (f instanceof DensityFunctionTypes.BinaryOperation bin
				&& bin.type().ordinal() == 0 /* ADD */) {
			return isBeard(bin.argument1()) || isBeard(bin.argument2());
		}
		if (f instanceof BinarySpecs.Add add) {
			return isBeard(add.left()) || isBeard(add.right());
		}
		return false;
	}

	private static boolean isBeard(DensityFunction f) {
		return f instanceof DensityFunctionTypes.Beardifier
				|| f instanceof DensityFunctionTypes.Beardifying;
	}

	/**
	 * Pure arithmetic trees only (unary/binary/linear/range/constant/spec). Anything with
	 * noise, wrapping, or unknown nodes returns false.
	 */
	public static boolean isPureArithmeticTree(@NotNull DensityFunction f) {
		if (f instanceof SpecDensity) {
			// Spec trees are already monomorphic arithmetic (or pure children).
			return true;
		}
		if (f instanceof DensityFunctionTypes.Constant) {
			return true;
		}
		if (f instanceof DensityFunctionTypes.UnaryOperation unary) {
			return isPureArithmeticTree(unary.input());
		}
		if (f instanceof DensityFunctionTypes.LinearOperation linear) {
			return isPureArithmeticTree(linear.input());
		}
		if (functionBinary(f)) {
			DensityFunctionTypes.BinaryOperation bin = (DensityFunctionTypes.BinaryOperation) f;
			return isPureArithmeticTree(bin.argument1()) && isPureArithmeticTree(bin.argument2());
		}
		if (f instanceof DensityFunctionTypes.RangeChoice range) {
			return isPureArithmeticTree(range.input())
					&& isPureArithmeticTree(range.whenInRange())
					&& isPureArithmeticTree(range.whenOutOfRange());
		}
		return false;
	}

	private static boolean functionBinary(DensityFunction f) {
		return f instanceof DensityFunctionTypes.BinaryOperation;
	}

	private static DensityFunction specializeUnary(DensityFunctionTypes.UnaryOperation unary) {
		DensityFunction input = specialize(unary.input());
		double min = unary.minValue();
		double max = unary.maxValue();
		int ord = unary.type().ordinal();
		DensityFunction out = switch (ord) {
			case 0 -> new UnarySpecs.Abs(unary, input, min, max);
			case 1 -> new UnarySpecs.Square(unary, input, min, max);
			case 2 -> new UnarySpecs.Cube(unary, input, min, max);
			case 3 -> new UnarySpecs.HalfNegative(unary, input, min, max);
			case 4 -> new UnarySpecs.QuarterNegative(unary, input, min, max);
			case 5 -> new UnarySpecs.Squeeze(unary, input, min, max);
			default -> unary;
		};
		if (out != unary) {
			PathMetrics.recordSpecialize();
		}
		return out;
	}

	private static DensityFunction specializeLinear(DensityFunctionTypes.LinearOperation linear) {
		DensityFunction input = specialize(linear.input());
		double arg = linear.argument();
		double min = linear.minValue();
		double max = linear.maxValue();
		PathMetrics.recordSpecialize();
		if (linear.specificType().ordinal() == 0) {
			return new LinearSpecs.MulConst(linear, input, arg, min, max);
		}
		return new LinearSpecs.AddConst(linear, input, arg, min, max);
	}

	private static DensityFunction specializeBinary(DensityFunctionTypes.BinaryOperation binary) {
		DensityFunction a = specialize(binary.argument1());
		DensityFunction b = specialize(binary.argument2());
		double min = binary.minValue();
		double max = binary.maxValue();
		DensityFunction out = switch (binary.type().ordinal()) {
			case 0 -> new BinarySpecs.Add(binary, a, b, min, max);
			case 1 -> new BinarySpecs.Mul(binary, a, b, min, max);
			case 2 -> new BinarySpecs.Min(binary, a, b, min, max);
			case 3 -> new BinarySpecs.Max(binary, a, b, min, max);
			default -> binary;
		};
		if (out != binary) {
			PathMetrics.recordSpecialize();
		}
		return out;
	}

	private static DensityFunction specializeRange(DensityFunctionTypes.RangeChoice range) {
		DensityFunction input = specialize(range.input());
		DensityFunction whenIn = specialize(range.whenInRange());
		DensityFunction whenOut = specialize(range.whenOutOfRange());
		PathMetrics.recordSpecialize();
		return new RangeSpecs.Choice(
				range,
				input,
				range.minInclusive(),
				range.maxExclusive(),
				whenIn,
				whenOut,
				range.minValue(),
				range.maxValue()
		);
	}
}
