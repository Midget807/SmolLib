package net.midget807.smollib.rendering;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;

public class TexturedCircleRender {
    public static final int MAX_RADIUS = 29999984;
    public final Vec3d origin;
    public int age;
    public final int maxAge;
    public final double size;
    public final double centerOffset;
    public final int color;
    public List<Quaternionf> TRANSFORMATIONS = new ArrayList<>();

    /**
     * @param maxAge If {@link #maxAge} equals -1, {@link #age} will not tick.<br>
     * @param size The diameter of the circle.
     * @param centerOffset The radial distance from the circle's center. Used for making rings.
     */
    public TexturedCircleRender(Vec3d origin, int maxAge, double size, double centerOffset, int color) {
        this.origin = origin;
        this.centerOffset = centerOffset;
        this.age = 0;
        this.maxAge = maxAge;
        this.size = size;
        this.color = color;
    }
    /**
     * @param maxAge If {@link #maxAge} equals -1, {@link #age} will not tick.<br>
     * @param size The diameter of the circle.
     */
    public TexturedCircleRender(Vec3d origin, int maxAge, double size, int color) {
        this.origin = origin;
        this.centerOffset = 0.0;
        this.age = 0;
        this.maxAge = maxAge;
        this.size = size;
        this.color = color;
    }

    public float getRadius() {
        return (float) this.size / 2;
    }

    public double getCentreX() {
        return this.origin.x;
    }
    public double getCentreY() {
        return this.origin.y;
    }
    public double getCentreZ() {
        return this.origin.z;
    }

    public double getSouthPoint() {
        return MathHelper.clamp(this.origin.z + this.size / 2.0, -MAX_RADIUS, MAX_RADIUS);
    }
    public double getNorthPoint() {
        return MathHelper.clamp(this.origin.z - this.size / 2.0, -MAX_RADIUS, MAX_RADIUS);
    }
    public double getEastPoint() {
        return MathHelper.clamp(this.origin.x + this.size / 2.0, -MAX_RADIUS, MAX_RADIUS);
    }
    public double getWestPoint() {
        return MathHelper.clamp(this.origin.x - this.size / 2.0, -MAX_RADIUS, MAX_RADIUS);
    }
    public double getUpPoint() {
        return MathHelper.clamp(this.origin.y + this.size / 2.0, -MAX_RADIUS, MAX_RADIUS);
    }
    public double getDownPoint() {
        return MathHelper.clamp(this.origin.y - this.size / 2.0, -MAX_RADIUS, MAX_RADIUS);
    }

    /**
     * Transformations (rotations) are handled in the order they are added.
     * <br>
     * Use {@link RotationAxis} class to create the necessary {@link Quaternionf}.
     * <br>
     * <br>
     * Note that {@link RotationAxis} is for global cardinal axes.
     * */
    public void addTransformation(Quaternionf rotation) {
        this.TRANSFORMATIONS.add(rotation);
    }
}
