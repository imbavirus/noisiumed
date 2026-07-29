package za.co.infernos.noisiumed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import za.co.infernos.noisiumed.config.NoisiumedConfig;
import za.co.infernos.noisiumed.noise.BulkNoiseFiller;
import za.co.infernos.noisiumed.path.PathMetrics;

public class Noisium {
	public static final String MOD_ID = "noisiumed";
	public static final String MOD_NAME = "Noisiumed";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static void initialize() {
		// Touch config early so allowlist is logged at boot.
		NoisiumedConfig.l1GeneratorAllowlist();
		// L2: defer WG heightmaps until after surface (or empty-surface skip).
		BulkNoiseFiller.DEFER_HEIGHTMAPS_UNTIL_SURFACE = true;
		LOGGER.info(
				"Loading {} (L0–L2 + density specializer + surface/coverage). Metrics: PathMetrics.snapshot()",
				MOD_NAME
		);
	}

	/** Bench / debug: process-wide path counters. */
	public static String pathMetricsSnapshot() {
		return PathMetrics.snapshot();
	}
}
