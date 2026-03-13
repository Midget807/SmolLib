package net.midget807.smollib.rendering.manager;

import net.midget807.smollib.rendering.SquareRender;
import net.minecraft.client.gl.VertexBuffer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SquareRendererManager {
    private static final List<SquareRender> SQUARE_RENDERER = new ArrayList<>();
    @Nullable
    public static VertexBuffer squareBuffer;

    public static void add(SquareRender square) {
        SQUARE_RENDERER.add(square);
    }

    public static void tick() {
        SQUARE_RENDERER.removeIf(squareRender -> squareRender.maxAge != -1 && ++squareRender.age >= squareRender.maxAge);
    }

    public static List<SquareRender> get() {
        return SQUARE_RENDERER;
    }

    public static void clear() {
        SQUARE_RENDERER.clear();
    }

    public static @Nullable VertexBuffer getCubeBuffer() {
        return squareBuffer;
    }

    public static void setCubeBuffer(@Nullable VertexBuffer cubeBuffer) {
        CubeRendererManager.cubeBuffer = cubeBuffer;
    }
}
