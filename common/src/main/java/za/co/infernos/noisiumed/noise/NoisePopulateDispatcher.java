package za.co.infernos.noisiumed.noise;

import net.minecraft.block.BlockState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.chunk.GenerationShapeConfig;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import za.co.infernos.noisiumed.attach.ChunkGenAttachment;
import za.co.infernos.noisiumed.path.L0Reason;
import za.co.infernos.noisiumed.path.PathMetrics;
import za.co.infernos.noisiumed.path.PathSelector;
import za.co.infernos.noisiumed.path.PathTier;
import za.co.infernos.noisiumed.pool.WorldgenScratchPool;

/**
 * Shared L0/L1 entry for {@code NoiseChunkGenerator#method_38332} (lock + populate).
 */
public final class NoisePopulateDispatcher {
	@FunctionalInterface
	public interface CreateSampler {
		ChunkNoiseSampler create(Chunk chunk, StructureAccessor structures, Blender blender, NoiseConfig noiseConfig);
	}

	@FunctionalInterface
	public interface VanillaPopulate {
		Chunk populate(
				Blender blender,
				StructureAccessor structureAccessor,
				NoiseConfig noiseConfig,
				Chunk chunk,
				int minimumCellY,
				int cellHeight
		);
	}

	private NoisePopulateDispatcher() {}

	public static @Nullable Chunk dispatch(
			@NotNull Class<?> generatorRuntimeClass,
			@NotNull RegistryEntry<ChunkGeneratorSettings> settings,
			@NotNull CreateSampler createSampler,
			@NotNull VanillaPopulate vanillaPopulate,
			@NotNull Chunk chunk,
			int generationShapeHeightFloorDiv,
			@NotNull GenerationShapeConfig generationShapeConfig,
			int minimumY,
			@NotNull Blender blender,
			@NotNull StructureAccessor structureAccessor,
			@NotNull NoiseConfig noiseConfig,
			int minimumYFloorDiv,
			boolean lithiumCounts
	) {
		PathSelector.Decision decision = PathSelector.selectNoisePath(generatorRuntimeClass, chunk);

		final int startingChunkSectionIndex = chunk.getSectionIndex(
				generationShapeHeightFloorDiv * generationShapeConfig.verticalCellBlockCount() - 1 + minimumY);
		final int minimumYChunkSectionIndex = chunk.getSectionIndex(minimumY);
		@NotNull final ChunkSection[] chunkSections = chunk.getSectionArray();

		for (int chunkSectionIndex = startingChunkSectionIndex; chunkSectionIndex >= minimumYChunkSectionIndex; --chunkSectionIndex) {
			chunkSections[chunkSectionIndex].lock();
		}

		boolean ranL1 = false;
		try {
			if (decision.tier() == PathTier.L1) {
				if (chunkSections.length > WorldgenScratchPool.Holder.MAX_SECTIONS) {
					PathMetrics.recordL1Failed(L0Reason.SECTION_COUNT);
				} else {
					ChunkNoiseSampler sampler = chunk.getOrCreateChunkNoiseSampler(
							c -> createSampler.create(c, structureAccessor, blender, noiseConfig)
					);
					boolean ok = BulkNoiseFiller.populate(
							sampler,
							settings.value().defaultBlock(),
							chunk,
							minimumYFloorDiv,
							generationShapeHeightFloorDiv,
							!lithiumCounts
					);
					if (ok) {
						PathMetrics.record(PathTier.L1);
						ranL1 = true;
						if (lithiumCounts) {
							for (int i = startingChunkSectionIndex; i >= minimumYChunkSectionIndex; --i) {
								chunkSections[i].calculateCounts();
							}
						}
						return chunk;
					}
					PathMetrics.recordL1Failed(L0Reason.L1_FAILED_OTHER);
					ChunkGenAttachment.discard(chunk);
				}
				// Fall through to vanilla L0 after L1 failure.
				PathMetrics.recordL0(L0Reason.DISPATCH_FALLBACK);
			} else {
				PathMetrics.recordL0(decision.l0Reason() != null ? decision.l0Reason() : L0Reason.DISPATCH_FALLBACK);
			}

			return vanillaPopulate.populate(
					blender, structureAccessor, noiseConfig, chunk, minimumYFloorDiv, generationShapeHeightFloorDiv
			);
		} finally {
			for (int chunkSectionIndex = startingChunkSectionIndex; chunkSectionIndex >= minimumYChunkSectionIndex; --chunkSectionIndex) {
				@NotNull final ChunkSection section = chunkSections[chunkSectionIndex];
				if (lithiumCounts && !ranL1) {
					section.calculateCounts();
				}
				section.unlock();
			}
		}
	}
}
