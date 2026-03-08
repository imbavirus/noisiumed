package za.co.infernos.noisiumed.neoforge;

import za.co.infernos.noisiumed.Noisium;
import net.neoforged.fml.common.Mod;

@Mod(Noisium.MOD_ID)
public class NoisiumNeoForge {
    public NoisiumNeoForge() {
        Noisium.initialize();
    }
}
