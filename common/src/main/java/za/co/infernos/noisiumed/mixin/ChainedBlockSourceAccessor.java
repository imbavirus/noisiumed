package za.co.infernos.noisiumed.mixin;

import net.minecraft.world.gen.ChainedBlockSource;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ChainedBlockSource.class)
public interface ChainedBlockSourceAccessor {
	@Accessor("samplers")
	List<ChunkNoiseSampler.BlockStateSampler> noisiumed$getSamplers();
}
