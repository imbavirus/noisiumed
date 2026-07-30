package za.co.infernos.noisiumed.density;

/**
 * Thread-local buffers for density {@code fill} paths (C2ME-safe).
 * <p>
 * Nested {@code fill} (e.g. Spec Add inside Spec Add during CellCache bulk fill) must not
 * share one temp array — that clobbers intermediate results and destroys golden parity.
 * Depth-stacked buffers fix that.
 * <p>
 * W5: per-depth length buckets for common fill sizes so we do not thrash realloc when
 * nested fills alternate between a few fixed lengths (cell grid / section fills).
 */
public final class DensityScratch {
	private static final int MAX_DEPTH = 32;

	/**
	 * Common exact lengths seen in noise cell / interpolator fills (must be exact —
	 * {@code DensityFunction#fill} uses {@code densities.length}).
	 */
	private static final int[] BUCKET_LENGTHS = {
			4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096
	};

	private static final ThreadLocal<DepthState> STATE = ThreadLocal.withInitial(DepthState::new);

	/** W5 default on for this experiment. Disable: {@code -Dnoisiumed.exp.w5.arena=false} */
	public static final boolean ARENA_BUCKETS = !"false".equalsIgnoreCase(
			System.getProperty("noisiumed.exp.w5.arena", "true"));

	private DensityScratch() {}

	/**
	 * Borrow a double[] of exactly {@code length} for the current nest level.
	 * Pair with {@link #releaseFillTemp()} in a finally block after the nested fill completes.
	 */
	public static double[] acquireFillTemp(int length) {
		DepthState s = STATE.get();
		int d = s.depth;
		if (d >= MAX_DEPTH) {
			return new double[length];
		}
		double[] buf;
		if (ARENA_BUCKETS) {
			int bi = bucketIndex(length);
			if (bi >= 0) {
				buf = s.buckets[d][bi];
				if (buf == null) {
					buf = new double[length];
					s.buckets[d][bi] = buf;
				}
				s.stack[d] = buf;
				s.depth = d + 1;
				return buf;
			}
		}
		buf = s.stack[d];
		if (buf == null || buf.length != length) {
			buf = new double[length];
			s.stack[d] = buf;
		}
		s.depth = d + 1;
		return buf;
	}

	/** Release the buffer from the matching {@link #acquireFillTemp(int)}. */
	public static void releaseFillTemp() {
		DepthState s = STATE.get();
		if (s.depth > 0) {
			s.depth--;
		}
	}

	/**
	 * @deprecated Use {@link #acquireFillTemp(int)} + {@link #releaseFillTemp()} for nested fills.
	 */
	@Deprecated
	public static double[] fillTemp(int length) {
		DepthState s = STATE.get();
		if (ARENA_BUCKETS) {
			int bi = bucketIndex(length);
			if (bi >= 0) {
				double[] buf = s.buckets[0][bi];
				if (buf == null) {
					buf = new double[length];
					s.buckets[0][bi] = buf;
				}
				return buf;
			}
		}
		double[] buf = s.stack[0];
		if (buf == null || buf.length != length) {
			buf = new double[length];
			s.stack[0] = buf;
		}
		return buf;
	}

	private static int bucketIndex(int length) {
		for (int i = 0; i < BUCKET_LENGTHS.length; i++) {
			if (BUCKET_LENGTHS[i] == length) {
				return i;
			}
		}
		return -1;
	}

	private static final class DepthState {
		final double[][] stack = new double[MAX_DEPTH][];
		/** [depth][bucketIndex] exact-length pooled arrays. */
		final double[][][] buckets = new double[MAX_DEPTH][BUCKET_LENGTHS.length][];
		int depth;
	}
}
