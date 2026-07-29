package za.co.infernos.noisiumed.mixin.noisechunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Per-block interpolator math (yarn DensityInterpolator → Mojmap NoiseInterpolator).
 * Inline lerp to avoid {@code MathHelper.lerp} call overhead on the hottest path
 * (every block × every interpolator × 1–4 lerps).
 */
@Mixin(targets = "net.minecraft.world.gen.chunk.ChunkNoiseSampler$DensityInterpolator")
public abstract class DensityInterpolatorMixin {
	@Shadow
	private double x0y0z0;
	@Shadow
	private double x0y0z1;
	@Shadow
	private double x1y0z0;
	@Shadow
	private double x1y0z1;
	@Shadow
	private double x0y1z0;
	@Shadow
	private double x0y1z1;
	@Shadow
	private double x1y1z0;
	@Shadow
	private double x1y1z1;
	@Shadow
	private double x0z0;
	@Shadow
	private double x1z0;
	@Shadow
	private double x0z1;
	@Shadow
	private double x1z1;
	@Shadow
	private double z0;
	@Shadow
	private double z1;
	@Shadow
	private double result;

	/**
	 * @author Infernos
	 * @reason Inline Y-axis lerp (parity: a + t*(b-a)).
	 */
	@Overwrite
	public void interpolateY(double deltaY) {
		this.x0z0 = this.x0y0z0 + deltaY * (this.x0y1z0 - this.x0y0z0);
		this.x1z0 = this.x1y0z0 + deltaY * (this.x1y1z0 - this.x1y0z0);
		this.x0z1 = this.x0y0z1 + deltaY * (this.x0y1z1 - this.x0y0z1);
		this.x1z1 = this.x1y0z1 + deltaY * (this.x1y1z1 - this.x1y0z1);
	}

	/**
	 * @author Infernos
	 * @reason Inline X-axis lerp.
	 */
	@Overwrite
	public void interpolateX(double deltaX) {
		this.z0 = this.x0z0 + deltaX * (this.x1z0 - this.x0z0);
		this.z1 = this.x0z1 + deltaX * (this.x1z1 - this.x0z1);
	}

	/**
	 * @author Infernos
	 * @reason Inline Z-axis lerp.
	 */
	@Overwrite
	public void interpolateZ(double deltaZ) {
		this.result = this.z0 + deltaZ * (this.z1 - this.z0);
	}
}
