package za.co.infernos.noisiumed.util.fabric;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Implements {@link za.co.infernos.noisiumed.util.ModUtil}.
 */
@SuppressWarnings("unused")
public class ModUtilImpl {
	/**
	 * Checks if a mod is present during loading.
	 */
	public static boolean isModPresent(String id) {
		return FabricLoader.getInstance().isModLoaded(id);
	}
}


