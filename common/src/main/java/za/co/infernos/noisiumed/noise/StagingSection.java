package za.co.infernos.noisiumed.noise;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.chunk.ChunkSection;
import org.jetbrains.annotations.NotNull;

/**
 * Adaptive staging buffer for one 16³ section during noise fill.
 * <ul>
 *   <li><b>4-bit mode</b> — up to 16 states, {@code long[256]} packing (one long per y/z row)</li>
 *   <li><b>8-bit mode</b> — up to 256 states, {@code byte[4096]} after automatic upgrade</li>
 * </ul>
 * Identity compare on {@link BlockState}; last-state cache for runs of identical blocks.
 */
public final class StagingSection {
	public static final BlockState AIR = Blocks.AIR.getDefaultState();

	private static final int MODE_4 = 4;
	private static final int MODE_8 = 8;

	private int bits = MODE_4;
	private BlockState[] states = new BlockState[16];
	private int[] materializeIds = new int[16];
	/** 4-bit packed rows: index = (y &lt;&lt; 4) | z */
	private final long[] storage4 = new long[256];
	/** 8-bit flat indices: index = (y &lt;&lt; 8) | (z &lt;&lt; 4) | x — allocated on upgrade */
	private byte[] storage8;

	private int paletteSize;
	private int defaultIdx;
	private int nonEmpty;
	private int nonEmptyFluid;
	private int randomTickable;
	private boolean dirty;

	private BlockState lastState;
	private int lastIdx;

	/** Per-section column occupancy: bit (z&lt;&lt;4)|x */
	private final long[] columnBits = new long[4];

	public StagingSection() {
		reset();
	}

	public void reset() {
		bits = MODE_4;
		if (states.length != 16) {
			states = new BlockState[16];
			materializeIds = new int[16];
		}
		states[0] = AIR;
		for (int i = 1; i < states.length; i++) {
			states[i] = null;
		}
		for (int i = 0; i < 256; i++) {
			storage4[i] = 0L;
		}
		if (storage8 != null) {
			// keep array for reuse but clear
			for (int i = 0; i < 4096; i++) {
				storage8[i] = 0;
			}
		}
		paletteSize = 1;
		defaultIdx = 0;
		nonEmpty = 0;
		nonEmptyFluid = 0;
		randomTickable = 0;
		dirty = false;
		lastState = null;
		lastIdx = 0;
		columnBits[0] = columnBits[1] = columnBits[2] = columnBits[3] = 0L;
	}

	public boolean isDirty() {
		return dirty;
	}

	public int paletteSize() {
		return paletteSize;
	}

	public int bits() {
		return bits;
	}

	public long[] columnBits() {
		return columnBits;
	}

	public void markColumn(int x, int z) {
		int bit = (z << 4) | x;
		columnBits[bit >>> 6] |= 1L << (bit & 63);
	}

	/**
	 * Always succeeds for normal worldgen palettes (up to 256 unique states).
	 */
	public void setBlockState(int x, int y, int z, @NotNull BlockState state) {
		int idx;
		if (state == lastState) {
			idx = lastIdx;
		} else {
			idx = indexOf(state);
			lastState = state;
			lastIdx = idx;
		}
		setId(x, y, z, idx);
	}

	public void setDefaultBlock(int x, int y, int z, @NotNull BlockState defaultBlock) {
		if (defaultIdx == 0) {
			defaultIdx = indexOf(defaultBlock);
			lastState = defaultBlock;
			lastIdx = defaultIdx;
		}
		setId(x, y, z, defaultIdx);
	}

	private void setId(int x, int y, int z, int idx) {
		if (bits == MODE_4) {
			int row = (y << 4) | z;
			int shift = x << 2;
			storage4[row] |= ((long) idx) << shift;
		} else {
			storage8[(y << 8) | (z << 4) | x] = (byte) idx;
		}
		dirty = true;
		if (idx != 0) {
			nonEmpty++;
			BlockState state = states[idx];
			if (!state.getFluidState().isEmpty()) {
				nonEmptyFluid++;
			}
			if (state.hasRandomTicks()) {
				randomTickable++;
			}
			markColumn(x, z);
		}
	}

	private int indexOf(@NotNull BlockState state) {
		if (state == AIR) {
			return 0;
		}
		for (int i = 1; i < paletteSize; i++) {
			if (states[i] == state) {
				return i;
			}
		}
		if (bits == MODE_4 && paletteSize >= 16) {
			upgradeTo8Bit();
		}
		if (paletteSize >= states.length) {
			// Hard fail: caller must abort L1 rather than corrupt the section.
			throw new IllegalStateException(
					"Noisiumed staging palette overflow (>" + states.length + " unique states)"
			);
		}
		int id = paletteSize;
		states[paletteSize++] = state;
		return id;
	}

	private void upgradeTo8Bit() {
		if (bits == MODE_8) {
			return;
		}
		if (storage8 == null) {
			storage8 = new byte[4096];
		} else {
			for (int i = 0; i < 4096; i++) {
				storage8[i] = 0;
			}
		}
		BlockState[] expanded = new BlockState[256];
		System.arraycopy(states, 0, expanded, 0, paletteSize);
		states = expanded;
		materializeIds = new int[256];

		for (int y = 0; y < 16; y++) {
			for (int z = 0; z < 16; z++) {
				long row = storage4[(y << 4) | z];
				if (row == 0L) {
					continue;
				}
				int base = (y << 8) | (z << 4);
				for (int x = 0; x < 16; x++) {
					int idx = (int) ((row >>> (x << 2)) & 0xF);
					if (idx != 0) {
						storage8[base | x] = (byte) idx;
					}
				}
			}
		}
		for (int i = 0; i < 256; i++) {
			storage4[i] = 0L;
		}
		bits = MODE_8;
	}

	/**
	 * Flush staging into the live section palette storage (direct write, no setBlockState).
	 * Re-reads storage after palette growth so singular→array resizes stay valid.
	 */
	public void materialize(@NotNull ChunkSection section, boolean updateCountsInline) {
		if (!dirty) {
			return;
		}

		for (int i = 0; i < paletteSize; i++) {
			section.blockStateContainer.data.palette.index(states[i]);
		}

		var data = section.blockStateContainer.data;
		var palette = data.palette;
		var storageOut = data.storage();

		for (int i = 0; i < paletteSize; i++) {
			materializeIds[i] = palette.index(states[i]);
		}

		if (bits == MODE_4) {
			for (int y = 0; y < 16; y++) {
				int yBase = y << 8;
				for (int z = 0; z < 16; z++) {
					long row = storage4[(y << 4) | z];
					if (row == 0L) {
						continue;
					}
					int zBase = yBase | (z << 4);
					for (int x = 0; x < 16; x++) {
						int idx = (int) ((row >>> (x << 2)) & 0xF);
						if (idx != 0) {
							storageOut.set(zBase | x, materializeIds[idx]);
						}
					}
				}
			}
		} else {
			for (int i = 0; i < 4096; i++) {
				int idx = storage8[i] & 0xFF;
				if (idx != 0) {
					storageOut.set(i, materializeIds[idx]);
				}
			}
		}

		if (updateCountsInline) {
			section.nonEmptyBlockCount += (short) Math.min(nonEmpty, Short.MAX_VALUE);
			section.nonEmptyFluidCount += (short) Math.min(nonEmptyFluid, Short.MAX_VALUE);
			section.randomTickableBlockCount += (short) Math.min(randomTickable, Short.MAX_VALUE);
		}
	}
}
