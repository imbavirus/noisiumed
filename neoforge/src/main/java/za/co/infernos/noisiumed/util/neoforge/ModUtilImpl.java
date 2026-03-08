package za.co.infernos.noisiumed.util.neoforge;

import net.neoforged.fml.loading.LoadingModList;

/**
 * Implements {@link za.co.infernos.noisiumed.util.ModUtil}.
 */
@SuppressWarnings("unused")
public class ModUtilImpl {
	/**
	 * Checks if a mod is present during loading.
	 */
	public static boolean isModPresent(String id) {
		return LoadingModList.get().getModFileById(id) != null;
	}
}


