package net.midget807.smollib.rendering;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;

public class TexturedSquareRender {
    public static final int MAX_RADIUS = 29999984;
    public final Vec3d origin;
    public final Direction direction;
    public int age;
    public final int maxAge;
    public final int size;
    public final int color;
    public final float textureSize;
    public final boolean animated;
    public final float animationAngle;
    public List<Quaternionf> TRANSFORMATIONS = new ArrayList<>();

    /** If {@link #maxAge} equals -1, {@link #age} will not tick.
     *
     */
    public TexturedSquareRender(Vec3d origin, Direction direction, int maxAge, int size, int color, float textureSize, float animationAngle) {
        this.origin = origin;
        this.direction = direction;
        this.size = size;
        this.color = color;
        this.textureSize = textureSize;
        this.animationAngle = animationAngle;
        this.animated = animationAngle >= 0;
        this.age = 0;
        this.maxAge = maxAge;
    }
    public TexturedSquareRender(Vec3d origin, Direction direction, int maxAge, int size, int color, float textureSize) {
        this.origin = origin;
        this.direction = direction;
        this.size = size;
        this.color = color;
        this.textureSize = textureSize;
        this.animationAngle = -1.0f;
        this.animated = false;
        this.age = 0;
        this.maxAge = maxAge;
    }

    public Direction.Axis getAxis() {
        if (this.direction == Direction.UP || this.direction == Direction.DOWN) {
            return Direction.Axis.Y;
        } else if (this.direction == Direction.EAST || this.direction == Direction.WEST) {
            return Direction.Axis.X;
        } else {
            return Direction.Axis.Z;
        }
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

    public double getCenterX() {
        return this.origin.x;
    }
    public double getCenterY() {
        return this.origin.y;
    }
    public double getCenterZ() {
        return this.origin.z;
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

    /**
     * Transformations (rotations) are handled in the order they are added.
     * <br>
     * Use {@link net.minecraft.util.math.RotationAxis} class to create the necessary {@link Quaternionf}.
     * */
    public void addTransformation(Quaternionf rotation) {
        this.TRANSFORMATIONS.add(rotation);
    }
}
