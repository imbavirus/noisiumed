package za.co.infernos.noisiumed.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChunkNoiseSampler.class)
public interface ChunkNoiseSamplerAccessor {
	@Invoker("getHorizontalCellBlockCount")
	int noisiumed$getHorizontalCellBlockCount();

	@Invoker("getVerticalCellBlockCount")
	int noisiumed$getVerticalCellBlockCount();

	@Invoker("sampleBlockState")
	BlockState noisiumed$sampleBlockState();
}
