package za.co.infernos.noisiumed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Noisium {
	public static final String MOD_ID = "noisiumed";
	public static final String MOD_NAME = "Noisiumed";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static void initialize() {
		LOGGER.info("Loading {}.", MOD_NAME);
	}
}


