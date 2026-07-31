package za.co.infernos.noisiumed.noise.sampler;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.RandomSplitter;
import net.minecraft.world.gen.OreVeinSampler;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.LongAdder;

/**
 * Monomorphic ore-vein secondary sampler (replaces OreVeinSampler lambda).
 * Bit-identical control flow to vanilla {@code OreVeinSampler} method_40547.
 * <p>
 * Does <strong>not</strong> re-specialize density inputs (they are already wrapped by
 * NoiseChunk); extra specialize broke golden hashes (bisect: ore_off 16/25 vs full 4/25).
 * <p>
 * <b>W6:</b> sample {@code veinGap} only after the ore-chance roll succeeds (vanilla AND
 * short-circuit). Opt-in counters: {@code -Dnoisiumed.oreStats=true} (zero cost when off).
 */
public final class FastOreVeinSampler implements ChunkNoiseSampler.BlockStateSampler {
	// Vanilla constants (1.21.1 OreVeinSampler)
	private static final float DENSITY_THRESHOLD = 0.4f;
	private static final double LIMINAL_DENSITY_REDUCTION = 0.2d;
	private static final float BLOCK_GENERATION_CHANCE = 0.7f;
	private static final float MIN_ORE_CHANCE = 0.1f;
	private static final float MAX_ORE_CHANCE = 0.3f;
	private static final float DENSITY_FOR_MAX_ORE_CHANCE = 0.6f;
	private static final float RAW_ORE_BLOCK_CHANCE = 0.02f;
	private static final float VEIN_GAP_THRESHOLD = -0.3f;
	private static final double MAX_DENSITY_INTRUSION = 20.0d;

	/** Union of copper/iron vein Y bands — outside this, sample is always null (no DF work). */
	private static final int VEIN_Y_MIN;
	private static final int VEIN_Y_MAX;

	/** Opt-in path stats; static final false → JIT dead-strips increment sites. */
	private static final boolean ORE_STATS = Boolean.getBoolean("noisiumed.oreStats");
	private static final LongAdder STAT_ENTER = new LongAdder();
	private static final LongAdder STAT_Y_OUTSIDE_TYPE = new LongAdder();
	private static final LongAdder STAT_DENSITY_REJECT = new LongAdder();
	private static final LongAdder STAT_BLOCK_CHANCE_REJECT = new LongAdder();
	private static final LongAdder STAT_RIDGED_REJECT = new LongAdder();
	private static final LongAdder STAT_ORE_CHANCE_SKIP_GAP = new LongAdder();
	private static final LongAdder STAT_GAP_SAMPLED = new LongAdder();
	private static final LongAdder STAT_GAP_REJECT = new LongAdder();
	private static final LongAdder STAT_ORE_PLACED = new LongAdder();
	private static final LongAdder STAT_STONE = new LongAdder();

	static {
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for (OreVeinSampler.VeinType type : OreVeinSampler.VeinType.values()) {
			min = Math.min(min, type.minY);
			max = Math.max(max, type.maxY);
		}
		VEIN_Y_MIN = min;
		VEIN_Y_MAX = max;
		if (ORE_STATS) {
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				System.out.println("[noisiumed oreStats] " + statsSummary());
			}, "noisiumed-oreStats"));
		}
	}

	/** NC-3 solid path: skip secondary.sample when Y is outside every vein band. */
	public static boolean mayHaveVeinAtY(int blockY) {
		return blockY >= VEIN_Y_MIN && blockY <= VEIN_Y_MAX;
	}

	/** Human-readable reject funnel (only meaningful when oreStats is on). */
	public static String statsSummary() {
		long enter = STAT_ENTER.sum();
		long gapSkip = STAT_ORE_CHANCE_SKIP_GAP.sum();
		long gapSamp = STAT_GAP_SAMPLED.sum();
		double skipPct = (gapSkip + gapSamp) == 0 ? 0.0
				: (100.0 * gapSkip) / (double) (gapSkip + gapSamp);
		return "enter=" + enter
				+ " yTypeOut=" + STAT_Y_OUTSIDE_TYPE.sum()
				+ " densRej=" + STAT_DENSITY_REJECT.sum()
				+ " blkChanceRej=" + STAT_BLOCK_CHANCE_REJECT.sum()
				+ " ridgedRej=" + STAT_RIDGED_REJECT.sum()
				+ " oreChanceSkipGap=" + gapSkip
				+ " gapSampled=" + gapSamp
				+ " gapRej=" + STAT_GAP_REJECT.sum()
				+ " orePlaced=" + STAT_ORE_PLACED.sum()
				+ " stone=" + STAT_STONE.sum()
				+ String.format(" gapSkipRate=%.1f%%", skipPct);
	}

	private final DensityFunction veinToggle;
	private final DensityFunction veinRidged;
	private final DensityFunction veinGap;
	private final RandomSplitter randomSplitter;

	public FastOreVeinSampler(
			@NotNull DensityFunction veinToggle,
			@NotNull DensityFunction veinRidged,
			@NotNull DensityFunction veinGap,
			@NotNull RandomSplitter randomSplitter
	) {
		this.veinToggle = veinToggle;
		this.veinRidged = veinRidged;
		this.veinGap = veinGap;
		this.randomSplitter = randomSplitter;
	}

	@Override
	@Nullable
	public BlockState sample(DensityFunction.NoisePos pos) {
		int blockY = pos.blockY();
		// Wall: skip veinToggle/ridged/gap when Y is outside every vein band (parity: null).
		if (blockY < VEIN_Y_MIN || blockY > VEIN_Y_MAX) {
			return null;
		}
		if (ORE_STATS) {
			STAT_ENTER.increment();
		}

		double toggle = this.veinToggle.sample(pos);
		OreVeinSampler.VeinType veinType = toggle > 0.0
				? OreVeinSampler.VeinType.COPPER
				: OreVeinSampler.VeinType.IRON;
		double absToggle = Math.abs(toggle);
		int aboveMin = blockY - veinType.minY;
		int belowMax = veinType.maxY - blockY;
		if (aboveMin < 0 || belowMax < 0) {
			if (ORE_STATS) {
				STAT_Y_OUTSIDE_TYPE.increment();
			}
			return null;
		}
		int edge = Math.min(belowMax, aboveMin);
		double liminal = MathHelper.clampedMap(
				(double) edge,
				0.0,
				MAX_DENSITY_INTRUSION,
				-LIMINAL_DENSITY_REDUCTION,
				0.0
		);
		if (absToggle + liminal < DENSITY_THRESHOLD) {
			if (ORE_STATS) {
				STAT_DENSITY_REJECT.increment();
			}
			return null;
		}
		Random random = this.randomSplitter.split(pos.blockX(), blockY, pos.blockZ());
		if (random.nextFloat() > BLOCK_GENERATION_CHANCE) {
			if (ORE_STATS) {
				STAT_BLOCK_CHANCE_REJECT.increment();
			}
			return null;
		}
		if (this.veinRidged.sample(pos) >= 0.0) {
			if (ORE_STATS) {
				STAT_RIDGED_REJECT.increment();
			}
			return null;
		}
		double oreChance = MathHelper.clampedMap(
				absToggle,
				(double) DENSITY_THRESHOLD,
				(double) DENSITY_FOR_MAX_ORE_CHANCE,
				(double) MIN_ORE_CHANCE,
				(double) MAX_ORE_CHANCE
		);
		// W6: sample gap DF only when the ore-chance roll succeeds (parity: stone otherwise).
		// Vanilla AND-short-circuits the same way; avoids a full DF sample on most vein blocks.
		if ((double) random.nextFloat() >= oreChance) {
			if (ORE_STATS) {
				STAT_ORE_CHANCE_SKIP_GAP.increment();
				STAT_STONE.increment();
			}
			return veinType.stone;
		}
		if (ORE_STATS) {
			STAT_GAP_SAMPLED.increment();
		}
		if (this.veinGap.sample(pos) > VEIN_GAP_THRESHOLD) {
			if (ORE_STATS) {
				STAT_ORE_PLACED.increment();
			}
			return random.nextFloat() < RAW_ORE_BLOCK_CHANCE
					? veinType.rawOreBlock
					: veinType.ore;
		}
		if (ORE_STATS) {
			STAT_GAP_REJECT.increment();
			STAT_STONE.increment();
		}
		return veinType.stone;
	}
}
