package za.co.infernos.noisiumed.attach;

import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-chunk generation flags for L2 (heightmap deferral through surface).
 * Uses identity map so chunks are GC-eligible after gen when keys drop.
 */
public final class ChunkGenAttachment {
	private static final Set<Chunk> HEIGHTMAPS_PENDING =
			Collections.newSetFromMap(new ConcurrentHashMap<>());

	private static final Set<Chunk> L1_USED =
			Collections.newSetFromMap(new ConcurrentHashMap<>());

	private ChunkGenAttachment() {}

	public static void markL1Used(@NotNull Chunk chunk) {
		L1_USED.add(chunk);
	}

	public static boolean wasL1Used(@NotNull Chunk chunk) {
		return L1_USED.contains(chunk);
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
	}

	/** Best-effort cleanup if a chunk is discarded mid-gen. */
	public static void discard(@Nullable Chunk chunk) {
		if (chunk != null) {
			clearAll(chunk);
		}
	}
}
