package za.co.infernos.noisiumed.path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.LongAdder;

/**
 * Process-wide counters for path hit rate and huge-win diagnostics (C2ME-safe).
 */
public final class PathMetrics {
	private static final Logger LOGGER = LoggerFactory.getLogger("noisiumed");
	private static final int LOG_EVERY_L1 = 128;

	private static final LongAdder L0 = new LongAdder();
	private static final LongAdder L1 = new LongAdder();
	private static final LongAdder L2 = new LongAdder();
	private static final LongAdder L1_FAIL = new LongAdder();
	private static final LongAdder BIOME = new LongAdder();
	private static final LongAdder SURFACE_SKIP = new LongAdder();

	private static final LongAdder L1_NS = new LongAdder();
	private static final LongAdder L1_CHUNKS_TIMED = new LongAdder();
	private static final LongAdder DIRECT_WRITES = new LongAdder();
	private static final LongAdder SECTIONS_TOUCHED = new LongAdder();

	private static final LongAdder[] L0_REASONS = new LongAdder[L0Reason.values().length];

	static {
		for (int i = 0; i < L0_REASONS.length; i++) {
			L0_REASONS[i] = new LongAdder();
		}
	}

	private PathMetrics() {}

	public static void record(PathTier tier) {
		switch (tier) {
			case L0 -> L0.increment();
			case L1 -> {
				L1.increment();
				long n = L1.sum();
				if (n > 0 && (n % LOG_EVERY_L1) == 0) {
					LOGGER.info(snapshot());
				}
			}
			case L2 -> L2.increment();
		}
	}

	public static void recordL0(L0Reason reason) {
		L0.increment();
		L0_REASONS[reason.ordinal()].increment();
	}

	public static void recordBiome() {
		BIOME.increment();
	}

	public static void recordSurfaceSkip() {
		SURFACE_SKIP.increment();
	}

	public static void recordL1Failed(L0Reason reason) {
		L1_FAIL.increment();
		L0_REASONS[reason.ordinal()].increment();
	}

	public static void recordL1Timed(long nanos) {
		L1_NS.add(nanos);
		L1_CHUNKS_TIMED.increment();
	}

	public static void recordDirectWrites(int count) {
		if (count > 0) {
			DIRECT_WRITES.add(count);
		}
	}

	public static void recordSectionsTouched(int count) {
		if (count > 0) {
			SECTIONS_TOUCHED.add(count);
		}
	}

	public static long l0() {
		return L0.sum();
	}

	public static long l1() {
		return L1.sum();
	}

	public static long l2() {
		return L2.sum();
	}

	public static long l1Failed() {
		return L1_FAIL.sum();
	}

	public static long biomes() {
		return BIOME.sum();
	}

	public static long surfaceSkips() {
		return SURFACE_SKIP.sum();
	}

	public static long l1NsTotal() {
		return L1_NS.sum();
	}

	public static long l1ChunksTimed() {
		return L1_CHUNKS_TIMED.sum();
	}

	public static long directWrites() {
		return DIRECT_WRITES.sum();
	}

	public static long sectionsTouched() {
		return SECTIONS_TOUCHED.sum();
	}

	public static long l0Reason(L0Reason reason) {
		return L0_REASONS[reason.ordinal()].sum();
	}

	/**
	 * One-line snapshot for logs / RCON / bench harness.
	 */
	public static String snapshot() {
		StringBuilder sb = new StringBuilder(256);
		sb.append("noisiumed.path l0=").append(l0())
				.append(" l1=").append(l1())
				.append(" l2=").append(l2())
				.append(" l1_fail=").append(l1Failed())
				.append(" biome=").append(biomes())
				.append(" surface_skip=").append(surfaceSkips());
		long timed = l1ChunksTimed();
		if (timed > 0) {
			long avgUs = (l1NsTotal() / timed) / 1000L;
			sb.append(" l1_avg_us=").append(avgUs);
		}
		sb.append(" direct_writes=").append(directWrites())
				.append(" sections_touched=").append(sectionsTouched());
		sb.append(" l0_reasons{");
		boolean first = true;
		for (L0Reason r : L0Reason.values()) {
			long c = l0Reason(r);
			if (c == 0) {
				continue;
			}
			if (!first) {
				sb.append(',');
			}
			first = false;
			sb.append(r.name()).append('=').append(c);
		}
		sb.append('}');
		return sb.toString();
	}

	public static void reset() {
		L0.reset();
		L1.reset();
		L2.reset();
		L1_FAIL.reset();
		BIOME.reset();
		SURFACE_SKIP.reset();
		L1_NS.reset();
		L1_CHUNKS_TIMED.reset();
		DIRECT_WRITES.reset();
		SECTIONS_TOUCHED.reset();
		for (LongAdder a : L0_REASONS) {
			a.reset();
		}
	}
}
