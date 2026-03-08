package za.co.infernos.noisiumed.forge;

import za.co.infernos.noisiumed.Noisium;
import net.minecraftforge.fml.common.Mod;

@Mod(Noisium.MOD_ID)
public class NoisiumForge {
    public NoisiumForge() {
        Noisium.initialize();
    }
}
