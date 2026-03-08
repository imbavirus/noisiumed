package za.co.infernos.noisiumed.fabric;

import za.co.infernos.noisiumed.Noisium;
import net.fabricmc.api.ModInitializer;

public class NoisiumFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Noisium.initialize();
    }
}


