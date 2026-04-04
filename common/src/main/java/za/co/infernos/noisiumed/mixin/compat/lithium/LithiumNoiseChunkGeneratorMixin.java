package za.co.infernos.noisiumed.mixin.compat.lithium;

import net.minecraft.block.BlockState;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoiseChunkGenerator.class)
public abstract class LithiumNoiseChunkGeneratorMixin extends ChunkGenerator {
	public LithiumNoiseChunkGeneratorMixin(BiomeSource biomeSource) {
		super(biomeSource);
	}

	@Redirect(method = "populateNoise(Lnet/minecraft/world/gen/chunk/Blender;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/gen/noise/NoiseConfig;Lnet/minecraft/world/chunk/Chunk;II)Lnet/minecraft/world/chunk/Chunk;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/ChunkSection;setBlockState(IIILnet/minecraft/block/BlockState;Z)Lnet/minecraft/block/BlockState;"))
	private BlockState noisium$populateNoiseWrapSetBlockStateOperation(@NotNull ChunkSection chunkSection, int chunkSectionBlockPosX, int chunkSectionBlockPosY, int chunkSectionBlockPosZ, @NotNull BlockState blockState, boolean lock) {
		// Set the blockstate in the palette storage directly to improve performance
		var blockStateId = chunkSection.blockStateContainer.data.palette.index(blockState);
		chunkSection.blockStateContainer.data.storage().set(
				chunkSection.blockStateContainer.paletteProvider.computeIndex(chunkSectionBlockPosX, chunkSectionBlockPosY,
						chunkSectionBlockPosZ
				), blockStateId);

		return blockState;
	}

	@Inject(method = "populateNoise(Lnet/minecraft/world/gen/chunk/Blender;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/gen/noise/NoiseConfig;Lnet/minecraft/world/chunk/Chunk;II)Lnet/minecraft/world/chunk/Chunk;", at = @At("TAIL"))
	private void noisium$calculateCounts(Blender blender, StructureAccessor structureAccessor, NoiseConfig noiseConfig, Chunk chunk, int minimumCellY, int cellHeight, CallbackInfoReturnable<Chunk> cir) {
		// Calculate counts for all sections to maintain Lithium compatibility
		for (ChunkSection section : chunk.getSectionArray()) {
			if (section != null) {
				section.calculateCounts();
			}
		}
	}
}
