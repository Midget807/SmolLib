package net.midget807.smollib.rendering.manager;

import net.midget807.smollib.rendering.CubeRender;
import net.minecraft.client.gl.VertexBuffer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CubeRendererManager {
    private static final List<CubeRender> CUBE_RENDERER = new ArrayList<>();
    private static final List<CubeRender> CUBES_TO_RENDER = new ArrayList<>();
    @Nullable
    public static VertexBuffer cubeBuffer;

    public static void add(CubeRender cube) {
        CUBE_RENDERER.add(cube);
        CUBES_TO_RENDER.add(cube);
    }

    public static void removeFromToRender(CubeRender cube) {
        CUBES_TO_RENDER.remove(cube);
    }

    public static void tick() {
        CUBE_RENDERER.removeIf(cubeRender -> cubeRender.maxAge != -1 && ++cubeRender.age >= cubeRender.maxAge);
    }

    public static List<CubeRender> get() {
        return CUBE_RENDERER;
    }
    public static List<CubeRender> getToRender() {
        return CUBES_TO_RENDER;
    }

    public static void clear() {
        CUBE_RENDERER.clear();
    }

    public static void clearFromToRender() {
        CUBES_TO_RENDER.clear();
    }

    public static @Nullable VertexBuffer getCubeBuffer() {
        return cubeBuffer;
    }

    public static void setCubeBuffer(@Nullable VertexBuffer cubeBuffer) {
        CubeRendererManager.cubeBuffer = cubeBuffer;
    }
}
