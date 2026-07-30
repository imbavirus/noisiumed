package za.co.infernos.noisiumed.config;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Lightweight config (system properties / env) for coverage and diagnostics.
 * <p>
 * Generator allowlist (Phase 5): FQCN of {@code NoiseChunkGenerator} subclasses safe for L1.
 * <ul>
 *   <li>System property: {@code noisiumed.l1.generator.allowlist=com.foo.A,com.bar.B}</li>
 *   <li>Env: {@code NOISIUMED_L1_GENERATOR_ALLOWLIST} (same CSV)</li>
 * </ul>
 */
public final class NoisiumedConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger("noisiumed");

	private static final Set<String> L1_GENERATOR_ALLOWLIST = parseAllowlist();

	/**
	 * NC-3: sampleBlockState reads primary density from CellCache array (same as vanilla
	 * CacheAllInCell) instead of re-entering density.sample. Disable with
	 * {@code -Dnoisiumed.cell.density.grid=false}.
	 */
	private static final boolean CELL_DENSITY_GRID = parseBool("noisiumed.cell.density.grid", "NOISIUMED_CELL_DENSITY_GRID", true);

	/**
	 * NC-5 monomorphic ore sampler. Default <strong>off</strong> until golden hash PASS
	 * (bisect: largest single parity delta). Enable: {@code -Dnoisiumed.fast.ore=true}.
	 */
	private static final boolean FAST_ORE = parseBool("noisiumed.fast.ore", "NOISIUMED_FAST_ORE", false);

	/**
	 * Specialize aquifer density nodes (barrier/floodedness/spread/type/erosion/depth).
	 * Default <strong>off</strong> until golden hash PASS. Enable:
	 * {@code -Dnoisiumed.aquifer.specialize=true}.
	 */
	private static final boolean AQUIFER_SPECIALIZE = parseBool(
			"noisiumed.aquifer.specialize", "NOISIUMED_AQUIFER_SPECIALIZE", false);

	/**
	 * L1 bulk noise fill. Disable with {@code -Dnoisiumed.l1=false} (force L0 vanilla loop).
	 */
	private static final boolean L1_ENABLED = parseBool("noisiumed.l1", "NOISIUMED_L1", true);

	/**
	 * Density arithmetic specializer (SpecDensity). Disable with
	 * {@code -Dnoisiumed.density.specialize=false}.
	 */
	private static final boolean DENSITY_SPECIALIZE = parseBool(
			"noisiumed.density.specialize", "NOISIUMED_DENSITY_SPECIALIZE", true);

	private NoisiumedConfig() {}

	public static @NotNull Set<String> l1GeneratorAllowlist() {
		return L1_GENERATOR_ALLOWLIST;
	}

	public static boolean cellDensityGrid() {
		return CELL_DENSITY_GRID;
	}

	public static boolean fastOre() {
		return FAST_ORE;
	}

	public static boolean aquiferSpecialize() {
		return AQUIFER_SPECIALIZE;
	}

	public static boolean l1Enabled() {
		return L1_ENABLED;
	}

	public static boolean densitySpecialize() {
		return DENSITY_SPECIALIZE;
	}

	public static boolean isL1GeneratorAllowed(@NotNull Class<?> generatorRuntimeClass) {
		if (L1_GENERATOR_ALLOWLIST.isEmpty()) {
			return false;
		}
		return L1_GENERATOR_ALLOWLIST.contains(generatorRuntimeClass.getName());
	}

	private static boolean parseBool(String prop, String env, boolean defaultValue) {
		String raw = System.getProperty(prop);
		if (raw == null || raw.isBlank()) {
			raw = System.getenv(env);
		}
		if (raw == null || raw.isBlank()) {
			return defaultValue;
		}
		return !raw.equalsIgnoreCase("false") && !raw.equals("0") && !raw.equalsIgnoreCase("off");
	}

	private static Set<String> parseAllowlist() {
		String raw = System.getProperty("noisiumed.l1.generator.allowlist");
		if (raw == null || raw.isBlank()) {
			raw = System.getenv("NOISIUMED_L1_GENERATOR_ALLOWLIST");
		}
		if (raw == null || raw.isBlank()) {
			return Set.of();
		}
		Set<String> set = new LinkedHashSet<>();
		for (String part : raw.split(",")) {
			String name = part.trim();
			if (!name.isEmpty()) {
				set.add(name);
			}
		}
		if (!set.isEmpty()) {
			LOGGER.info("L1 generator allowlist: {}", set);
		}
		return Collections.unmodifiableSet(set);
	}
}
