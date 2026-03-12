package net.midget807.smollib.rendering.manager;

import net.midget807.smollib.rendering.CubeRender;
import net.midget807.smollib.rendering.SquareRender;

import java.util.ArrayList;
import java.util.List;

public class CubeRendererManager {
    private static final List<CubeRender> CUBE_RENDERER = new ArrayList<>();
    private static final List<CubeRender> CUBES_TO_RENDER = new ArrayList<>();

    public static void add(CubeRender cube) {
        CUBE_RENDERER.add(cube);
        CUBES_TO_RENDER.add(cube);
    }

    public static void removeFromBuffer(CubeRender cube) {
        CUBES_TO_RENDER.remove(cube);
    }

    public static void tick() {
        CUBE_RENDERER.removeIf(cubeRender -> cubeRender.maxAge != -1 && ++cubeRender.age >= cubeRender.maxAge);
    }

    public static List<CubeRender> get() {
        return CUBE_RENDERER;
    }
    public static List<CubeRender> getBuffer() {
        return CUBES_TO_RENDER;
    }

    public static void clear() {
        CUBE_RENDERER.clear();
    }

    public static void clearBuffer() {
        CUBES_TO_RENDER.clear();
    }
}
