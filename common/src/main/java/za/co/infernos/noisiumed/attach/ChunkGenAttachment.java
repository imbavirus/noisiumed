package za.co.infernos.noisiumed.attach;

import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-chunk generation flags for L1/L2 (heightmap deferral, surface skip, column occupancy).
 * Identity keys so chunks stay GC-eligible after gen.
 */
public final class ChunkGenAttachment {
	private static final Set<Chunk> HEIGHTMAPS_PENDING =
			Collections.newSetFromMap(new ConcurrentHashMap<>());

	private static final Set<Chunk> L1_USED =
			Collections.newSetFromMap(new ConcurrentHashMap<>());

	/** L1 wrote at least one non-air block. */
	private static final Set<Chunk> L1_HAS_SOLID =
			Collections.newSetFromMap(new ConcurrentHashMap<>());

	/** Chunk-local column occupancy: 4 longs, bit (z&lt;&lt;4)|x. Present only for L1 chunks. */
	private static final Map<Chunk, long[]> COLUMN_BITS = new ConcurrentHashMap<>();

	private ChunkGenAttachment() {}

	public static void markL1Used(@NotNull Chunk chunk) {
		L1_USED.add(chunk);
	}

	public static boolean wasL1Used(@NotNull Chunk chunk) {
		return L1_USED.contains(chunk);
	}

	public static void markL1HasSolid(@NotNull Chunk chunk) {
		L1_HAS_SOLID.add(chunk);
	}

	public static boolean l1HasSolid(@NotNull Chunk chunk) {
		return L1_HAS_SOLID.contains(chunk);
	}

	/**
	 * Stores a copy of the 4-long column occupancy mask (OR of all section writers).
	 */
	public static void setColumnBits(@NotNull Chunk chunk, @NotNull long[] bits4) {
		if (bits4.length < 4) {
			return;
		}
		COLUMN_BITS.put(chunk, new long[] { bits4[0], bits4[1], bits4[2], bits4[3] });
	}

	public static @Nullable long[] getColumnBits(@NotNull Chunk chunk) {
		return COLUMN_BITS.get(chunk);
	}

	/** True if any column bit is set (any solid in vertical column). */
	public static boolean hasAnyColumnSolid(@NotNull Chunk chunk) {
		long[] bits = COLUMN_BITS.get(chunk);
		if (bits == null) {
			return l1HasSolid(chunk);
		}
		return bits[0] != 0L || bits[1] != 0L || bits[2] != 0L || bits[3] != 0L;
	}

	public static void markHeightmapsPending(@NotNull Chunk chunk) {
		HEIGHTMAPS_PENDING.add(chunk);
	}

	public static boolean isHeightmapsPending(@NotNull Chunk chunk) {
		return HEIGHTMAPS_PENDING.contains(chunk);
	}

	public static void clearHeightmapsPending(@NotNull Chunk chunk) {
		HEIGHTMAPS_PENDING.remove(chunk);
	}

	public static void clearAll(@NotNull Chunk chunk) {
		HEIGHTMAPS_PENDING.remove(chunk);
		L1_USED.remove(chunk);
		L1_HAS_SOLID.remove(chunk);
		COLUMN_BITS.remove(chunk);
	}

	/** Best-effort cleanup if a chunk is discarded mid-gen. */
	public static void discard(@Nullable Chunk chunk) {
		if (chunk != null) {
			clearAll(chunk);
		}
	}
}
