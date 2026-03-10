package net.midget807.smollib.rendering;

import net.midget807.smollib.rendering.manager.SquareRendererManager;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class ShapeRenderer {
    public static void renderSquare(Vec3d origin, Direction direction, int maxAge, int size, int color) {
        SquareRendererManager.add(new SquareRender(origin, direction, maxAge, size, color));
    }
}
