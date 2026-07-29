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

	private NoisiumedConfig() {}

	public static @NotNull Set<String> l1GeneratorAllowlist() {
		return L1_GENERATOR_ALLOWLIST;
	}

	public static boolean isL1GeneratorAllowed(@NotNull Class<?> generatorRuntimeClass) {
		if (L1_GENERATOR_ALLOWLIST.isEmpty()) {
			return false;
		}
		return L1_GENERATOR_ALLOWLIST.contains(generatorRuntimeClass.getName());
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
