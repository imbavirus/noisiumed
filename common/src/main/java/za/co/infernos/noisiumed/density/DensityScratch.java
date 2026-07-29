package za.co.infernos.noisiumed.density;

/**
 * Thread-local buffers for density {@code fill} paths (C2ME-safe).
 */
public final class DensityScratch {
	private static final ThreadLocal<double[]> FILL_TEMP = ThreadLocal.withInitial(() -> new double[0]);

	private DensityScratch() {}

	/**
	 * Returns a double[] of at least {@code length} elements, exactly sized when growing
	 * so vanilla-style {@code fill} that writes every index stays correct.
	 */
	public static double[] fillTemp(int length) {
		double[] buf = FILL_TEMP.get();
		if (buf.length != length) {
			buf = new double[length];
			FILL_TEMP.set(buf);
		}
		return buf;
	}
}
