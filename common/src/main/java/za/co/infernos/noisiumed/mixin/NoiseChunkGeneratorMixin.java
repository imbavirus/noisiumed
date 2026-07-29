package za.co.infernos.noisiumed.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.*;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import za.co.infernos.noisiumed.noise.NoisePopulateDispatcher;

@Mixin(NoiseChunkGenerator.class)
public abstract class NoiseChunkGeneratorMixin extends ChunkGenerator {
	@Shadow
	@Final
	private RegistryEntry<ChunkGeneratorSettings> settings;

	@Shadow
	protected abstract Chunk populateNoise(Blender blender, StructureAccessor structureAccessor, NoiseConfig noiseConfig, Chunk chunk, int minimumCellY, int cellHeight);

	@Shadow
	protected abstract ChunkNoiseSampler createChunkNoiseSampler(Chunk chunk, StructureAccessor structureAccessor, Blender blender, NoiseConfig noiseConfig);

	public NoiseChunkGeneratorMixin(BiomeSource biomeSource) {
		super(biomeSource);
	}

	/**
	 * L0 path: direct palette write inside vanilla populateNoise.
	 * Re-reads storage after palette.index so singular→array resize stays valid.
	 */
	@Redirect(method = "populateNoise(Lnet/minecraft/world/gen/chunk/Blender;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/gen/noise/NoiseConfig;Lnet/minecraft/world/chunk/Chunk;II)Lnet/minecraft/world/chunk/Chunk;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/ChunkSection;setBlockState(IIILnet/minecraft/block/BlockState;Z)Lnet/minecraft/block/BlockState;"))
	private BlockState noisium$populateNoiseWrapSetBlockStateOperation(@NotNull ChunkSection chunkSection, int chunkSectionBlockPosX, int chunkSectionBlockPosY, int chunkSectionBlockPosZ, @NotNull BlockState blockState, boolean lock) {
		// May resize container when first non-air is written to a singular palette.
		int blockStateId = chunkSection.blockStateContainer.data.palette.index(blockState);
		// Fresh data after possible resize:
		var data = chunkSection.blockStateContainer.data;
		data.storage().set(
				chunkSection.blockStateContainer.paletteProvider.computeIndex(
						chunkSectionBlockPosX, chunkSectionBlockPosY, chunkSectionBlockPosZ
				),
				blockStateId
		);

		chunkSection.nonEmptyBlockCount += 1;
		if (!blockState.getFluidState().isEmpty()) {
			chunkSection.nonEmptyFluidCount += 1;
		}
		if (blockState.hasRandomTicks()) {
			chunkSection.randomTickableBlockCount += 1;
		}

		return blockState;
	}

	/**
	 * @author Steveplays28, Infernos
	 * @reason L1 bulk noise path when safe; else L0 lock + populateNoise with palette redirect.
	 */
	@Overwrite
	public @Nullable Chunk method_38332(@NotNull Chunk chunk, int generationShapeHeightFloorDiv, @NotNull GenerationShapeConfig generationShapeConfig, int minimumY, @NotNull Blender blender, @NotNull StructureAccessor structureAccessor, @NotNull NoiseConfig noiseConfig, int minimumYFloorDiv) {
		return NoisePopulateDispatcher.dispatch(
				this.getClass(),
				this.settings,
				this::createChunkNoiseSampler,
				this::populateNoise,
				chunk,
				generationShapeHeightFloorDiv,
				generationShapeConfig,
				minimumY,
				blender,
				structureAccessor,
				noiseConfig,
				minimumYFloorDiv,
				false
		);
	}
}
