package za.co.infernos.noisiumed.path;

/**
 * Worldgen optimisation tier selected per chunk.
 * <ul>
 *   <li>{@link #L0} — legacy Noisium redirect on vanilla populateNoise</li>
 *   <li>{@link #L1} — bulk staging palette fill with deferred heightmaps (aquifer-safe)</li>
 *   <li>{@link #L2} — reserved for fused noise→surface staging (4.1+)</li>
 * </ul>
 */
public enum PathTier {
	L0,
	L1,
	L2
}
