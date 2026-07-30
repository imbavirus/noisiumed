package za.co.infernos.noisiumed.density;

/**
 * Thread-local buffers for density {@code fill} paths (C2ME-safe).
 * <p>
 * Nested {@code fill} (e.g. Spec Add inside Spec Add during CellCache bulk fill) must not
 * share one temp array — that clobbers intermediate results and destroys golden parity.
 * Depth-stacked buffers fix that.
 */
public final class DensityScratch {
	private static final int MAX_DEPTH = 32;

	private static final ThreadLocal<DepthState> STATE = ThreadLocal.withInitial(DepthState::new);

	private DensityScratch() {}

	/**
	 * Borrow a double[] of exactly {@code length} for the current nest level.
	 * Pair with {@link #releaseFillTemp()} in a finally block after the nested fill completes.
	 */
	public static double[] acquireFillTemp(int length) {
		DepthState s = STATE.get();
		int d = s.depth;
		if (d >= MAX_DEPTH) {
			// Fallback: heap alloc (should never hit for vanilla DF depth)
			return new double[length];
		}
		double[][] stack = s.stack;
		double[] buf = stack[d];
		if (buf == null || buf.length != length) {
			buf = new double[length];
			stack[d] = buf;
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
	 * Kept for call sites that cannot nest (single level only).
	 */
	@Deprecated
	public static double[] fillTemp(int length) {
		DepthState s = STATE.get();
		// Use slot 0 without bumping depth — unsafe if nested; prefer acquire/release.
		double[] buf = s.stack[0];
		if (buf == null || buf.length != length) {
			buf = new double[length];
			s.stack[0] = buf;
		}
		return buf;
	}

	private static final class DepthState {
		final double[][] stack = new double[MAX_DEPTH][];
		int depth;
	}
}
