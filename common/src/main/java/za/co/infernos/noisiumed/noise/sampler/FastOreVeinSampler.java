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
import za.co.infernos.noisiumed.density.special.DensitySpecializer;

/**
 * Monomorphic ore-vein secondary sampler (replaces OreVeinSampler lambda).
 * Bit-identical control flow to vanilla {@code OreVeinSampler} method_40547.
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
		this.veinToggle = DensitySpecializer.specialize(veinToggle);
		this.veinRidged = DensitySpecializer.specialize(veinRidged);
		this.veinGap = DensitySpecializer.specialize(veinGap);
		this.randomSplitter = randomSplitter;
	}

	@Override
	@Nullable
	public BlockState sample(DensityFunction.NoisePos pos) {
		double toggle = this.veinToggle.sample(pos);
		int blockY = pos.blockY();
		OreVeinSampler.VeinType veinType = toggle > 0.0
				? OreVeinSampler.VeinType.COPPER
				: OreVeinSampler.VeinType.IRON;
		double absToggle = Math.abs(toggle);
		int aboveMin = blockY - veinType.minY;
		int belowMax = veinType.maxY - blockY;
		if (aboveMin < 0 || belowMax < 0) {
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
			return null;
		}
		Random random = this.randomSplitter.split(pos.blockX(), blockY, pos.blockZ());
		if (random.nextFloat() > BLOCK_GENERATION_CHANCE) {
			return null;
		}
		if (this.veinRidged.sample(pos) >= 0.0) {
			return null;
		}
		double oreChance = MathHelper.clampedMap(
				absToggle,
				(double) DENSITY_THRESHOLD,
				(double) DENSITY_FOR_MAX_ORE_CHANCE,
				(double) MIN_ORE_CHANCE,
				(double) MAX_ORE_CHANCE
		);
		if ((double) random.nextFloat() < oreChance
				&& this.veinGap.sample(pos) > VEIN_GAP_THRESHOLD) {
			return random.nextFloat() < RAW_ORE_BLOCK_CHANCE
					? veinType.rawOreBlock
					: veinType.ore;
		}
		return veinType.stone;
	}
}
