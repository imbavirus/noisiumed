package za.co.infernos.noisiumed.noise;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.gen.chunk.AquiferSampler;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import org.jetbrains.annotations.NotNull;
import za.co.infernos.noisiumed.attach.ChunkGenAttachment;
import za.co.infernos.noisiumed.heightmap.DeferredHeightmaps;
import za.co.infernos.noisiumed.mixin.ChunkNoiseSamplerAccessor;
import za.co.infernos.noisiumed.path.PathMetrics;
import za.co.infernos.noisiumed.pool.WorldgenScratchPool;

/**
 * L1 bulk noise fill with <strong>direct section writes</strong> (no staging materialize pass)
 * and optional L2 heightmap deferral until after surface building.
 */
public final class BulkNoiseFiller {
	/** When true, WG heightmaps are filled after surface (L2) instead of immediately after noise. */
	public static volatile boolean DEFER_HEIGHTMAPS_UNTIL_SURFACE = true;

	/**
	 * W1 isolation: when NC-3 cell is entirely solid (all densities &gt; 0), skip per-block
	 * sampleBlockState and write default (+ ore only in vein Y). Default on for exp branch.
	 * Disable: {@code -Dnoisiumed.exp.w1.spec.cells=false}
	 */
	public static final boolean SPEC_SOLID_CELLS = !"false".equalsIgnoreCase(
			System.getProperty("noisiumed.exp.w1.spec.cells", "true"));

	private BulkNoiseFiller() {}

	/**
	 * @return false only on hard structural failure (e.g. too many sections)
	 */
	public static boolean populate(
			@NotNull ChunkNoiseSampler chunkNoiseSampler,
			@NotNull BlockState defaultBlockState,
			@NotNull Chunk chunk,
			int minimumCellY,
			int cellHeight,
			boolean updateCountsInline
	) {
		long t0 = System.nanoTime();
		WorldgenScratchPool.Holder pool = WorldgenScratchPool.get();
		ChunkSection[] sections = chunk.getSectionArray();
		int sectionCount = sections.length;
		if (sectionCount > WorldgenScratchPool.Holder.MAX_SECTIONS) {
			return false;
		}
		pool.releaseWriters(sectionCount);

		try {
			ChunkPos chunkPos = chunk.getPos();
			int chunkStartX = chunkPos.getStartX();
			int chunkStartZ = chunkPos.getStartZ();
			AquiferSampler aquiferSampler = chunkNoiseSampler.getAquiferSampler();
			chunkNoiseSampler.sampleStartDensity();

			ChunkNoiseSamplerAccessor samplerAccess = (ChunkNoiseSamplerAccessor) chunkNoiseSampler;
			int horizontalCellBlockCount = samplerAccess.noisiumed$getHorizontalCellBlockCount();
			int verticalCellBlockCount = samplerAccess.noisiumed$getVerticalCellBlockCount();
			int cellWidth = 16 / horizontalCellBlockCount;
			int minY = chunk.getBottomY();
			// Mutable always available: vanilla checks needsFluidTick() after each sample
			// (flag can flip mid-chunk when aquifers place fluids).
			BlockPos.Mutable mutable = new BlockPos.Mutable();

			// NC-3: primary density from cell cache — skip interpolator lerp; still set cell indices.
			final CellGridPositionAccess cellGrid =
					chunkNoiseSampler instanceof CellGridPositionAccess cgp && cgp.noisiumed$cellGridActive()
							? cgp
							: null;

			final double invHoriz = 1.0 / (double) horizontalCellBlockCount;
			final double invVert = 1.0 / (double) verticalCellBlockCount;

			final double[] horizDeltas = new double[horizontalCellBlockCount];
			for (int i = 0; i < horizontalCellBlockCount; i++) {
				horizDeltas[i] = i * invHoriz;
			}
			final double[] vertDeltas = new double[verticalCellBlockCount];
			for (int i = 0; i < verticalCellBlockCount; i++) {
				vertDeltas[i] = i * invVert;
			}

			int totalWrites = 0;
			int sectionsTouched = 0;
			// Phase 2A: sample every 8th timed L1 chunk (avoids nanoTime tax on every block).
			final boolean detailTiming = (PathMetrics.l1ChunksTimed() & 7L) == 0L;
			long sampleNs = 0L;
			long writeNs = 0L;

			for (int cellX = 0; cellX < cellWidth; cellX++) {
				chunkNoiseSampler.sampleEndDensity(cellX);
				final int baseX = chunkStartX + cellX * horizontalCellBlockCount;

				for (int cellZ = 0; cellZ < cellWidth; cellZ++) {
					final int baseZ = chunkStartZ + cellZ * horizontalCellBlockCount;

					for (int cellY = cellHeight - 1; cellY >= 0; cellY--) {
						chunkNoiseSampler.onSampledCellCorners(cellY, cellZ);
						final int cellBaseY = (minimumCellY + cellY) * verticalCellBlockCount;

						// W1: entire cell solid → no aquifer; write default (+ ore in vein band only).
						if (SPEC_SOLID_CELLS && cellGrid != null && cellGrid.noisiumed$cellAllSolid()) {
							for (int verticalCellBlock = verticalCellBlockCount - 1; verticalCellBlock >= 0; verticalCellBlock--) {
								int blockY = cellBaseY + verticalCellBlock;
								int blockYInSection = blockY & 15;
								int cy = (blockY - minY) >> 4;
								if (cy < 0 || cy >= sectionCount) {
									continue;
								}
								DirectSectionWriter writer = pool.sectionWriters[cy];
								if (writer == null || !writer.isBound()) {
									writer = acquireWriter(pool, cy, sections[cy]);
								}
								final boolean oreY = za.co.infernos.noisiumed.noise.sampler.FastOreVeinSampler.mayHaveVeinAtY(blockY);
								for (int cellBlockX = 0; cellBlockX < horizontalCellBlockCount; cellBlockX++) {
									int blockX = baseX + cellBlockX;
									int blockXInSection = blockX & 15;
									for (int cellBlockZ = 0; cellBlockZ < horizontalCellBlockCount; cellBlockZ++) {
										int blockZ = baseZ + cellBlockZ;
										int blockZInSection = blockZ & 15;
										if (oreY) {
											cellGrid.noisiumed$positionY(blockY);
											cellGrid.noisiumed$positionX(blockX);
											cellGrid.noisiumed$positionZ(blockZ);
											BlockState ore = samplerAccess.noisiumed$sampleBlockState();
											// sampleBlockState on all-solid still runs solid path + secondary
											if (ore != null && !ore.isAir() && ore != defaultBlockState) {
												writer.setBlockState(blockXInSection, blockYInSection, blockZInSection, ore);
											} else {
												writer.setDefaultBlock(blockXInSection, blockYInSection, blockZInSection, defaultBlockState);
											}
										} else {
											writer.setDefaultBlock(blockXInSection, blockYInSection, blockZInSection, defaultBlockState);
										}
									}
								}
							}
							continue;
						}

						for (int verticalCellBlock = verticalCellBlockCount - 1; verticalCellBlock >= 0; verticalCellBlock--) {
							int blockY = cellBaseY + verticalCellBlock;
							int blockYInSection = blockY & 15;
							if (cellGrid != null) {
								cellGrid.noisiumed$positionY(blockY);
							} else {
								chunkNoiseSampler.interpolateY(blockY, vertDeltas[verticalCellBlock]);
							}

							int cy = (blockY - minY) >> 4;
							if (cy < 0 || cy >= sectionCount) {
								continue;
							}

							DirectSectionWriter writer = pool.sectionWriters[cy];
							if (writer == null || !writer.isBound()) {
								writer = acquireWriter(pool, cy, sections[cy]);
							}

							for (int cellBlockX = 0; cellBlockX < horizontalCellBlockCount; cellBlockX++) {
								int blockX = baseX + cellBlockX;
								int blockXInSection = blockX & 15;
								if (cellGrid != null) {
									cellGrid.noisiumed$positionX(blockX);
								} else {
									chunkNoiseSampler.interpolateX(blockX, horizDeltas[cellBlockX]);
								}

								for (int cellBlockZ = 0; cellBlockZ < horizontalCellBlockCount; cellBlockZ++) {
									int blockZ = baseZ + cellBlockZ;
									int blockZInSection = blockZ & 15;
									if (cellGrid != null) {
										cellGrid.noisiumed$positionZ(blockZ);
									} else {
										chunkNoiseSampler.interpolateZ(blockZ, horizDeltas[cellBlockZ]);
									}

									BlockState state;
									if (detailTiming) {
										long tS = System.nanoTime();
										state = samplerAccess.noisiumed$sampleBlockState();
										sampleNs += System.nanoTime() - tS;
									} else {
										state = samplerAccess.noisiumed$sampleBlockState();
									}

									// Vanilla: null → default stone (solid). Solid never schedules fluid ticks
									// (NC-3/vanilla clear needsFluidTick when density > 0). Skip fluid path.
									if (state == null) {
										if (detailTiming) {
											long tW = System.nanoTime();
											writer.setDefaultBlock(
													blockXInSection, blockYInSection, blockZInSection, defaultBlockState
											);
											writeNs += System.nanoTime() - tW;
										} else {
											writer.setDefaultBlock(
													blockXInSection, blockYInSection, blockZInSection, defaultBlockState
											);
										}
										continue;
									}
									if (state.isAir()) {
										continue;
									}

									if (detailTiming) {
										long tW = System.nanoTime();
										if (state == defaultBlockState) {
											writer.setDefaultBlock(
													blockXInSection, blockYInSection, blockZInSection, defaultBlockState
											);
										} else {
											writer.setBlockState(
													blockXInSection, blockYInSection, blockZInSection, state
											);
										}
										writeNs += System.nanoTime() - tW;
									} else if (state == defaultBlockState) {
										writer.setDefaultBlock(
												blockXInSection, blockYInSection, blockZInSection, defaultBlockState
										);
									} else {
										writer.setBlockState(
												blockXInSection, blockYInSection, blockZInSection, state
										);
									}

									// Per-block (vanilla): aquifer flag updates after each non-solid sample.
									if (aquiferSampler.needsFluidTick() && !state.getFluidState().isEmpty()) {
										mutable.set(blockX, blockY, blockZ);
										chunk.markBlockForPostProcessing(mutable);
									}
								}
							}
						}
					}
				}

				chunkNoiseSampler.swapBuffers();
			}

			chunkNoiseSampler.stopInterpolation();

			long[] chunkColumns = pool.columnBits;
			chunkColumns[0] = chunkColumns[1] = chunkColumns[2] = chunkColumns[3] = 0L;

			for (int i = 0; i < sectionCount; i++) {
				DirectSectionWriter writer = pool.sectionWriters[i];
				if (writer != null && writer.isDirty()) {
					sectionsTouched++;
					totalWrites += writer.writes();
					long[] sec = writer.columnBits();
					chunkColumns[0] |= sec[0];
					chunkColumns[1] |= sec[1];
					chunkColumns[2] |= sec[2];
					chunkColumns[3] |= sec[3];
					if (updateCountsInline) {
						writer.applyCountsInline();
					} else {
						sections[i].calculateCounts();
					}
				}
			}

			PathMetrics.recordDirectWrites(totalWrites);
			PathMetrics.recordSectionsTouched(sectionsTouched);
			if (detailTiming) {
				PathMetrics.recordSampleWriteNs(sampleNs, writeNs);
			}

			ChunkGenAttachment.markL1Used(chunk);
			ChunkGenAttachment.setColumnBits(chunk, chunkColumns);
			if (totalWrites > 0) {
				ChunkGenAttachment.markL1HasSolid(chunk);
			}
			if (DEFER_HEIGHTMAPS_UNTIL_SURFACE) {
				ChunkGenAttachment.markHeightmapsPending(chunk);
			} else {
				DeferredHeightmaps.populateAfterNoise(chunk);
			}
			return true;
		} finally {
			PathMetrics.recordL1Timed(System.nanoTime() - t0);
			pool.releaseWriters(sectionCount);
		}
	}

	private static @NotNull DirectSectionWriter acquireWriter(
			@NotNull WorldgenScratchPool.Holder pool,
			int cy,
			@NotNull ChunkSection section
	) {
		DirectSectionWriter w = pool.sectionWriters[cy];
		if (w == null) {
			w = new DirectSectionWriter();
			pool.sectionWriters[cy] = w;
		}
		w.bind(section);
		return w;
	}
}
