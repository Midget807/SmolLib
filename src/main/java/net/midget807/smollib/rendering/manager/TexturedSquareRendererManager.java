package net.midget807.smollib.rendering.manager;

import net.midget807.smollib.rendering.TexturedSquareRender;
import net.minecraft.client.gl.VertexBuffer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TexturedSquareRendererManager {
    private static final List<TexturedSquareRender> SQUARE_RENDERER = new ArrayList<>();
    @Nullable
    public static VertexBuffer squareBuffer;

    public static void add(TexturedSquareRender square) {
        SQUARE_RENDERER.add(square);
    }

    public static void tick() {
        SQUARE_RENDERER.removeIf(texturedSquareRender -> texturedSquareRender.maxAge != -1 && ++texturedSquareRender.age >= texturedSquareRender.maxAge);
    }

    public static List<TexturedSquareRender> get() {
        return SQUARE_RENDERER;
    }

    public static void clear() {
        SQUARE_RENDERER.clear();
    }

}
