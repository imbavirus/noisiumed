package za.co.infernos.noisiumed.noise;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PalettedContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * L1 section writer: direct palette {@code index} + {@code storage.set} during the noise loop
 * (no separate staging materialize pass). Re-reads container data after each {@code index}
 * so singular→array (and further) palette resizes stay valid — same fix as L0 redirect.
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
			lastState = defaultBlock;
			lastId = defaultId;
		}
		writeId(x, y, z, defaultId, defaultBlock);
	}

	private int index(@NotNull BlockState state) {
		//noinspection DataFlowIssue
		PalettedContainer<BlockState> container = section.blockStateContainer;
		return container.data.palette.index(state);
	}

	private void writeId(int x, int y, int z, int id, @NotNull BlockState state) {
		//noinspection DataFlowIssue
		PalettedContainer<BlockState> container = section.blockStateContainer;
		// Fresh data after possible resize inside palette.index:
		var data = container.data;
		int storageIndex = container.paletteProvider.computeIndex(x, y, z);
		data.storage().set(storageIndex, id);

		dirty = true;
		writes++;
		nonEmpty++;
		if (!state.getFluidState().isEmpty()) {
			nonEmptyFluid++;
		}
		if (state.hasRandomTicks()) {
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
