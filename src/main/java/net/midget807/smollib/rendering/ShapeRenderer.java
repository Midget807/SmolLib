package net.midget807.smollib.rendering;

import net.midget807.smollib.rendering.manager.CubeRendererManager;
import net.midget807.smollib.rendering.manager.SquareRendererManager;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class ShapeRenderer {
    /**
     * @param animationAngle The angle which the texture moves in degrees.
     * */
    public static void renderSquare(Vec3d origin, Direction direction, int maxAge, int size, int color, float textureSize, float animationAngle) {
        SquareRendererManager.add(new SquareRender(origin, direction, maxAge, size, color, textureSize, animationAngle));
    }
    public static void renderSquare(Vec3d origin, Direction direction, int maxAge, int size, int color, float textureSize) {
        SquareRendererManager.add(new SquareRender(origin, direction, maxAge, size, color, textureSize));
    }
    public static void renderCube(Vec3d origin, int maxAge, int size, int color) {
        CubeRender cubeRender = new CubeRender(origin, maxAge, size, color);
        CubeRendererManager.add(cubeRender);
    }
}
