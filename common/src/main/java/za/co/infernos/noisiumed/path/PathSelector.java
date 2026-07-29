package za.co.infernos.noisiumed.path;

import net.minecraft.SharedConstants;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.SingularPalette;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Chooses an optimisation tier. Prefer high coverage (L1) over all-or-nothing empty-only paths.
 * Does <strong>not</strong> bail solely because aquifers exist.
 */
public final class PathSelector {
	private PathSelector() {}

	public record Decision(@NotNull PathTier tier, @Nullable L0Reason l0Reason) {
		public static Decision l1() {
			return new Decision(PathTier.L1, null);
		}

		public static Decision l0(@NotNull L0Reason reason) {
			return new Decision(PathTier.L0, reason);
		}
	}

	public static @NotNull Decision selectNoisePath(
			@NotNull Class<?> generatorRuntimeClass,
			@NotNull Chunk chunk
	) {
		if (SharedConstants.isOutsideGenerationArea(chunk.getPos())) {
			return Decision.l0(L0Reason.OUTSIDE_GEN_AREA);
		}
		if (chunk.hasBelowZeroRetrogen()) {
			return Decision.l0(L0Reason.BELOW_ZERO_RETROGEN);
		}
		// Exact vanilla noise generator only (subclasses stay L0 until allowlisted).
		if (generatorRuntimeClass != NoiseChunkGenerator.class) {
			return Decision.l0(L0Reason.NON_VANILLA_GENERATOR);
		}
		if (!isBulkFillSafe(chunk)) {
			return Decision.l0(L0Reason.UNSAFE_SECTIONS);
		}
		return Decision.l1();
	}

	/**
	 * True when every non-null section is still empty / singular palette.
	 */
	public static boolean isBulkFillSafe(@NotNull Chunk chunk) {
		ChunkSection[] sections = chunk.getSectionArray();
		for (ChunkSection section : sections) {
			if (section == null || section.isEmpty()) {
				continue;
			}
			if (!(section.blockStateContainer.data.palette instanceof SingularPalette<?>)) {
				return false;
			}
		}
		return true;
	}
}
