package za.co.infernos.noisiumed.density.special;

import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import za.co.infernos.noisiumed.config.NoisiumedConfig;
import za.co.infernos.noisiumed.mixin.noisechunk.CellCacheAccess;
import za.co.infernos.noisiumed.mixin.noisechunk.DensityInterpolatorDelegateAccess;
import za.co.infernos.noisiumed.path.PathMetrics;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Rewrites hot vanilla density nodes into monomorphic {@link SpecDensity} implementations.
 * Idempotent: already-specialized nodes are returned unchanged.
 * <p>
 * beta.16.2 deep specialize:
 * <ul>
 *   <li>CellCache + DensityInterpolator delegates (interpolators default on)</li>
 *   <li><b>Per-chunk identity memo</b> so shared subtrees are specialized once (no alloc storm)</li>
 *   <li>Nested fill temps must use {@link za.co.infernos.noisiumed.density.DensityScratch} stack</li>
 * </ul>
 */
public final class DensitySpecializer {
	private DensitySpecializer() {}

	/**
	 * @return specialized form, or {@code function} if no rewrite applies
	 */
	public static @NotNull DensityFunction specialize(@NotNull DensityFunction function) {
		return specialize(function, null);
	}

	/**
	 * Specialize with optional identity memo (deep path). Memo is not used for RETURN specialize
	 * / mapAll so those stay simple; deep install reuses one map per NoiseChunk.
	 */
	public static @NotNull DensityFunction specialize(
			@NotNull DensityFunction function,
			@Nullable Map<DensityFunction, DensityFunction> memo
	) {
		if (!NoisiumedConfig.densitySpecialize()) {
			return function;
		}
		if (function instanceof SpecDensity) {
			return function;
		}
		if (memo != null) {
			DensityFunction hit = memo.get(function);
			if (hit != null) {
				return hit;
			}
		}

		DensityFunction out;
		if (function instanceof DensityFunctionTypes.UnaryOperation unary) {
			out = specializeUnary(unary, memo);
		} else if (function instanceof DensityFunctionTypes.LinearOperation linear) {
			out = specializeLinear(linear, memo);
		} else if (function instanceof DensityFunctionTypes.BinaryOperation binary) {
			out = specializeBinary(binary, memo);
		} else if (function instanceof DensityFunctionTypes.RangeChoice range) {
			out = specializeRange(range, memo);
		} else if (NoisiumedConfig.densityClampSpecialize()
				&& function instanceof DensityFunctionTypes.Clamp clamp) {
			out = specializeClamp(clamp, memo);
		} else {
			out = function;
		}

		if (memo != null && out != function) {
			memo.put(function, out);
		}
		return out;
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
	 * After {@code ChunkNoiseSampler} construction: specialize CellCache + interpolator
	 * delegates in place, sharing Spec subtrees via identity memo.
	 */
	public static void specializeInstalledDelegates(@NotNull List<?> interpolators, @NotNull List<?> caches) {
		if (!NoisiumedConfig.densitySpecialize() || !NoisiumedConfig.densityDeepSpecialize()) {
			return;
		}
		// One memo per NoiseChunk: shared vanilla subtrees → one Spec subtree (no N×alloc).
		Map<DensityFunction, DensityFunction> memo = new IdentityHashMap<>(64);
		int rewritten = 0;
		int specRoots = 0;
		int vanillaRoots = 0;

		//noinspection ForLoopReplaceableByForEach
		for (int i = 0, n = caches.size(); i < n; i++) {
			Object o = caches.get(i);
			if (!(o instanceof CellCacheAccess access)) {
				continue;
			}
			int r = rewriteDelegate(access.noisiumed$getDelegate(), memo, (s) -> access.noisiumed$setDelegate(s));
			if (r == 1) {
				rewritten++;
				specRoots++;
			} else if (r == 2) {
				specRoots++;
			} else if (r == 3) {
				vanillaRoots++;
			}
		}

		if (NoisiumedConfig.densityDeepInterpolators()) {
			//noinspection ForLoopReplaceableByForEach
			for (int i = 0, n = interpolators.size(); i < n; i++) {
				Object o = interpolators.get(i);
				if (!(o instanceof DensityInterpolatorDelegateAccess access)) {
					continue;
				}
				int r = rewriteDelegate(access.noisiumed$getDelegate(), memo, (s) -> access.noisiumed$setDelegate(s));
				if (r == 1) {
					rewritten++;
					specRoots++;
				} else if (r == 2) {
					specRoots++;
				} else if (r == 3) {
					vanillaRoots++;
				}
			}
		}

		if (rewritten > 0) {
			PathMetrics.recordSpecDeep(rewritten);
		}
		PathMetrics.recordSpecCoverage(specRoots, vanillaRoots);
	}

	/** 1 = rewritten to Spec, 2 = already Spec, 3 = still vanilla arith, 0 = other leaf */
	private static int rewriteDelegate(
			DensityFunction d,
			Map<DensityFunction, DensityFunction> memo,
			java.util.function.Consumer<DensityFunction> set
	) {
		if (d instanceof SpecDensity) {
			return 2;
		}
		DensityFunction s = specialize(d, memo);
		if (s != d) {
			set.accept(s);
			return s instanceof SpecDensity ? 1 : 0;
		}
		return isVanillaArithmetic(d) ? 3 : 0;
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
	 * Pure arithmetic trees only. SpecDensity is pure only if its <em>original</em> vanilla
	 * snapshot was pure (Spec nodes may wrap noise/interp leaves after mapAll).
	 */
	public static boolean isPureArithmeticTree(@NotNull DensityFunction f) {
		if (f instanceof SpecDensity spec) {
			return isPureArithmeticTree(spec.original);
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
		if (f instanceof DensityFunctionTypes.BinaryOperation bin) {
			return isPureArithmeticTree(bin.argument1()) && isPureArithmeticTree(bin.argument2());
		}
		if (f instanceof DensityFunctionTypes.RangeChoice range) {
			return isPureArithmeticTree(range.input())
					&& isPureArithmeticTree(range.whenInRange())
					&& isPureArithmeticTree(range.whenOutOfRange());
		}
		if (f instanceof DensityFunctionTypes.Clamp clamp) {
			return isPureArithmeticTree(clamp.input());
		}
		return false;
	}

	private static boolean isVanillaArithmetic(DensityFunction f) {
		return f instanceof DensityFunctionTypes.UnaryOperation
				|| f instanceof DensityFunctionTypes.LinearOperation
				|| f instanceof DensityFunctionTypes.BinaryOperation
				|| f instanceof DensityFunctionTypes.RangeChoice
				|| f instanceof DensityFunctionTypes.Clamp;
	}

	private static DensityFunction specializeUnary(
			DensityFunctionTypes.UnaryOperation unary,
			@Nullable Map<DensityFunction, DensityFunction> memo
	) {
		DensityFunction input = specialize(unary.input(), memo);
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

	private static DensityFunction specializeLinear(
			DensityFunctionTypes.LinearOperation linear,
			@Nullable Map<DensityFunction, DensityFunction> memo
	) {
		DensityFunction input = specialize(linear.input(), memo);
		double arg = linear.argument();
		double min = linear.minValue();
		double max = linear.maxValue();
		PathMetrics.recordSpecialize();
		if (linear.specificType().ordinal() == 0) {
			return new LinearSpecs.MulConst(linear, input, arg, min, max);
		}
		return new LinearSpecs.AddConst(linear, input, arg, min, max);
	}

	private static DensityFunction specializeBinary(
			DensityFunctionTypes.BinaryOperation binary,
			@Nullable Map<DensityFunction, DensityFunction> memo
	) {
		DensityFunction a = specialize(binary.argument1(), memo);
		DensityFunction b = specialize(binary.argument2(), memo);
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

	private static DensityFunction specializeRange(
			DensityFunctionTypes.RangeChoice range,
			@Nullable Map<DensityFunction, DensityFunction> memo
	) {
		DensityFunction input = specialize(range.input(), memo);
		DensityFunction whenIn = specialize(range.whenInRange(), memo);
		DensityFunction whenOut = specialize(range.whenOutOfRange(), memo);
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

	private static DensityFunction specializeClamp(
			DensityFunctionTypes.Clamp clamp,
			@Nullable Map<DensityFunction, DensityFunction> memo
	) {
		DensityFunction input = specialize(clamp.input(), memo);
		PathMetrics.recordSpecialize();
		return new ClampSpecs.Clamp(clamp, input, clamp.minValue(), clamp.maxValue());
	}
}
