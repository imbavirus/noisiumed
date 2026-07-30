package za.co.infernos.noisiumed.noise;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.util.collection.PaletteStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * L1 section writer: direct palette storage writes during the noise loop.
 * <p>
 * On empty (singular) sections, after the first palette growth settles on a 4-bit
 * {@link PaletteStorage}, hot solid writes use FN-style row packing:
 * {@code storage[(y&lt;&lt;4)|z] |= id &lt;&lt; (x&lt;&lt;2)} — only valid while cells are still 0
 * (empty start). Falls back to {@code storage.set} when bits ≠ 4 or after palette resize.
 * <p>
 * Identity last-state cache avoids repeated palette scans on solid runs.
 */
public final class DirectSectionWriter {
	public static final BlockState AIR = Blocks.AIR.getDefaultState();

	private @Nullable ChunkSection section;
	private boolean dirty;
	private int nonEmpty;
	private int nonEmptyFluid;
	private int randomTickable;
	private int writes;

	private @Nullable BlockState lastState;
	private int lastId;
	private int defaultId;
	private boolean hasDefault;
	private boolean defaultHasFluid;
	private boolean defaultRandomTick;

	/** 4-bit packed row storage from the live container (FN layout). */
	private @Nullable long[] raw4;
	private boolean fast4;

	/** Per-section column occupancy: bit (z&lt;&lt;4)|x — for future surface/heightmap use. */
	private final long[] columnBits = new long[4];

	public void bind(@NotNull ChunkSection section) {
		this.section = section;
		this.dirty = false;
		this.nonEmpty = 0;
		this.nonEmptyFluid = 0;
		this.randomTickable = 0;
		this.writes = 0;
		this.lastState = null;
		this.lastId = 0;
		this.defaultId = 0;
		this.hasDefault = false;
		this.defaultHasFluid = false;
		this.defaultRandomTick = false;
		this.raw4 = null;
		this.fast4 = false;
		columnBits[0] = columnBits[1] = columnBits[2] = columnBits[3] = 0L;
	}

	public void reset() {
		this.section = null;
		this.dirty = false;
		this.nonEmpty = 0;
		this.nonEmptyFluid = 0;
		this.randomTickable = 0;
		this.writes = 0;
		this.lastState = null;
		this.lastId = 0;
		this.defaultId = 0;
		this.hasDefault = false;
		this.defaultHasFluid = false;
		this.defaultRandomTick = false;
		this.raw4 = null;
		this.fast4 = false;
		columnBits[0] = columnBits[1] = columnBits[2] = columnBits[3] = 0L;
	}

	public boolean isBound() {
		return this.section != null;
	}

	public boolean isDirty() {
		return dirty;
	}

	public int writes() {
		return writes;
	}

	public long[] columnBits() {
		return columnBits;
	}

	public void setBlockState(int x, int y, int z, @NotNull BlockState state) {
		if (state == AIR) {
			return;
		}
		int id;
		if (state == lastState) {
			id = lastId;
		} else {
			id = index(state);
			lastState = state;
			lastId = id;
		}
		writeId(x, y, z, id, state);
	}

	public void setDefaultBlock(int x, int y, int z, @NotNull BlockState defaultBlock) {
		if (!hasDefault) {
			defaultId = index(defaultBlock);
			hasDefault = true;
			defaultHasFluid = !defaultBlock.getFluidState().isEmpty();
			defaultRandomTick = defaultBlock.hasRandomTicks();
			lastState = defaultBlock;
			lastId = defaultId;
		}
		// Hot solid path: skip per-block fluid/randomTick queries (cached from first default).
		writeIdCached(x, y, z, defaultId, defaultHasFluid, defaultRandomTick);
	}

	private int index(@NotNull BlockState state) {
		//noinspection DataFlowIssue
		PalettedContainer<BlockState> container = section.blockStateContainer;
		int id = container.data.palette.index(state);
		// Palette growth may replace storage — re-probe fast path.
		captureFast4(container);
		return id;
	}

	private void captureFast4(@NotNull PalettedContainer<BlockState> container) {
		PaletteStorage storage = container.data.storage();
		if (storage.getElementBits() == 4) {
			long[] data = storage.getData();
			// 4 bits × 4096 cells = 2048 bytes = 256 longs (FN row packing).
			if (data != null && data.length >= 256) {
				this.raw4 = data;
				this.fast4 = true;
				return;
			}
		}
		this.raw4 = null;
		this.fast4 = false;
	}

	private void writeId(int x, int y, int z, int id, @NotNull BlockState state) {
		writeIdCached(x, y, z, id, !state.getFluidState().isEmpty(), state.hasRandomTicks());
	}

	private void writeIdCached(int x, int y, int z, int id, boolean fluid, boolean randomTick) {
		//noinspection DataFlowIssue
		if (fast4 && raw4 != null && id >= 0 && id < 16) {
			// Same packing as vanilla PackedIntegerArray(4,4096) for BLOCK_STATE index order:
			// storageIndex = (y<<8)|(z<<4)|x → long row (y<<4)|z, nibble at x*4.
			// OR is valid only while the nibble is still 0 (empty L1 start).
			raw4[(y << 4) | z] |= ((long) id) << (x << 2);
		} else {
			PalettedContainer<BlockState> container = section.blockStateContainer;
			var data = container.data;
			int storageIndex = container.paletteProvider.computeIndex(x, y, z);
			data.storage().set(storageIndex, id);
			// set() may be used after a mid-section resize; keep fast probe honest.
			if (fast4) {
				captureFast4(container);
			}
		}

		dirty = true;
		writes++;
		nonEmpty++;
		if (fluid) {
			nonEmptyFluid++;
		}
		if (randomTick) {
			randomTickable++;
		}
		int bit = (z << 4) | x;
		columnBits[bit >>> 6] |= 1L << (bit & 63);
	}

	/**
	 * Apply section non-empty counts when not deferring to {@code calculateCounts()}.
	 */
	public void applyCountsInline() {
		if (!dirty || section == null) {
			return;
		}
		section.nonEmptyBlockCount += (short) Math.min(nonEmpty, Short.MAX_VALUE);
		section.nonEmptyFluidCount += (short) Math.min(nonEmptyFluid, Short.MAX_VALUE);
		section.randomTickableBlockCount += (short) Math.min(randomTickable, Short.MAX_VALUE);
	}
}
