package net.midget807.smollib.rendering;

import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;

public class TexturedSphereRender {
    public static final int MAX_RADIUS = 29999984;
    public final Vec3d origin;
    public int age;
    public final int maxAge;
    public final int size;
    public final int color;
    public List<Quaternionf> TRANSFORMATIONS = new ArrayList<>();

    /** @param maxAge If {@link #maxAge} equals -1, {@link #age} will not tick.<br>
     *  @param size The diameter of the circle.
     */
    public TexturedSphereRender(Vec3d origin, int maxAge, int size, int color) {
        this.origin = origin;
        this.age = 0;
        this.maxAge = maxAge;
        this.size = size;
        this.color = color;
    }

    public float getRadius() {
        return (float) this.size / 2;
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
