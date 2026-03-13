package net.midget807.smollib.rendering;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class CubeRender {
    public static final int MAX_RADIUS = 29999984;
    public final Vec3d origin;
    public int age;
    public final int maxAge;
    public final int size;
    public final int color;
    private boolean isRendered = false;

    /** If {@link #maxAge} equals -1, {@link #age} will not tick.
     *
     */
    public CubeRender(Vec3d origin, int maxAge, int size, int color) {
        this.origin = origin;
        this.size = size;
        this.color = color;
        this.age = 0;
        this.maxAge = maxAge;
    }

    public static CubeRender createSimple(Vec3d origin, int maxAge, int size, int color) {
        return new CubeRender(origin, maxAge, size, color);
    }

    public double getSouthEdge() {
        return MathHelper.clamp(this.origin.z + this.size / 2.0, -MAX_RADIUS, MAX_RADIUS);
    }
    public double getNorthEdge() {
        return MathHelper.clamp(this.origin.z - this.size / 2.0, -MAX_RADIUS, MAX_RADIUS);
    }
    public double getEastEdge() {
        return MathHelper.clamp(this.origin.x + this.size / 2.0, -MAX_RADIUS, MAX_RADIUS);
    }
    public double getWestEdge() {
        return MathHelper.clamp(this.origin.x - this.size / 2.0, -MAX_RADIUS, MAX_RADIUS);
    }
    public double getUpEdge() {
        return MathHelper.clamp(this.origin.y + this.size / 2.0, -MAX_RADIUS, MAX_RADIUS);
    }
    public double getDownEdge() {
        return MathHelper.clamp(this.origin.y - this.size / 2.0, -MAX_RADIUS, MAX_RADIUS);
    }

    public double getDistanceRelativeToEdge(double x, double y, double z) {
        double dzn = z - this.getNorthEdge();
        double dzp = this.getSouthEdge() - z;
        double dxn = x - this.getWestEdge();
        double dxp = this.getEastEdge() - x;
        double dyn = y - this.getDownEdge();
        double dyp = this.getUpEdge() - y;
        double check = Math.min(dxn, dxp);
        check = Math.min(check, dzn);
        check = Math.min(check, dzp);
        check = Math.min(check, dyn);
        return Math.min(check, dzp);
    }

    public boolean isRendered() {
        return this.isRendered;
    }
    public void setRendered(boolean isRendered) {
        this.isRendered = isRendered;
    }
}
