package za.co.infernos.noisiumed.pool;

import net.minecraft.block.BlockState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.NotNull;
import za.co.infernos.noisiumed.noise.DirectSectionWriter;
import za.co.infernos.noisiumed.noise.StagingSection;

/**
 * Thread-local reuse for C2ME / parallel worldgen (one thread owns a chunk at a time).
 */
public final class WorldgenScratchPool {
	private static final ThreadLocal<Holder> HOLDERS = ThreadLocal.withInitial(Holder::new);

	private WorldgenScratchPool() {}

	public static @NotNull Holder get() {
		return HOLDERS.get();
	}

	@SuppressWarnings("unchecked")
	public static final class Holder {
		/** Max sections in a 1.21 world (~384 height / 16 = 24; pad for custom dims). */
		public static final int MAX_SECTIONS = 64;

		/** Legacy staging (kept for optional fallback / tooling). */
		public final StagingSection[] stagingSections = new StagingSection[MAX_SECTIONS];
		/** L1 direct palette writers (primary path). */
		public final DirectSectionWriter[] sectionWriters = new DirectSectionWriter[MAX_SECTIONS];

		public final BlockState[] paletteScratch = new BlockState[16];
		public final long[] storageScratch = new long[256];

		/** Biome packing scratch (4×4×4 = 64 cells). */
		public final RegistryEntry<Biome>[] biomePalette = (RegistryEntry<Biome>[]) new RegistryEntry[64];
		public final int[] biomeIndices = new int[64];

		/** Non-air columns across a chunk: bit i = (z &lt;&lt; 4) | x */
		public final long[] columnBits = new long[4];

		public void clearColumnBits() {
			columnBits[0] = 0L;
			columnBits[1] = 0L;
			columnBits[2] = 0L;
			columnBits[3] = 0L;
		}

		public void markColumn(int localX, int localZ) {
			int bit = (localZ << 4) | localX;
			columnBits[bit >>> 6] |= 1L << (bit & 63);
		}

		public void releaseStaging(int sectionCount) {
			int n = Math.min(sectionCount, stagingSections.length);
			for (int i = 0; i < n; i++) {
				StagingSection s = stagingSections[i];
				if (s != null) {
					s.reset();
				}
			}
		}

		public void releaseWriters(int sectionCount) {
			int n = Math.min(sectionCount, sectionWriters.length);
			for (int i = 0; i < n; i++) {
				DirectSectionWriter w = sectionWriters[i];
				if (w != null) {
					w.reset();
				}
			}
		}
	}
}
