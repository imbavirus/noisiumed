package za.co.infernos.noisiumed.path;

/**
 * Why the selector chose L0 (or why L1 failed into L0).
 */
public enum L0Reason {
	OUTSIDE_GEN_AREA,
	BELOW_ZERO_RETROGEN,
	NON_VANILLA_GENERATOR,
	UNSAFE_SECTIONS,
	SECTION_COUNT,
	PALETTE_OVERFLOW,
	L1_FAILED_OTHER,
	/** L0 taken as the normal fallback path after metrics already recorded a reason. */
	DISPATCH_FALLBACK
}
