package net.midget807.smollib.rendering.manager;

import net.midget807.smollib.rendering.TexturedCircleRender;
import net.minecraft.client.gl.VertexBuffer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TexturedCircleRendererManager {
    public static final List<TexturedCircleRender> CIRCLE_RENDERER = new ArrayList<>();
    @Nullable
    public static VertexBuffer circleBuffer;

    public static void add(TexturedCircleRender circle) {
        CIRCLE_RENDERER.add(circle);
    }

    public static void tick() {
        CIRCLE_RENDERER.removeIf(texturedCircleRender -> texturedCircleRender.maxAge != -1 && ++texturedCircleRender.age >= texturedCircleRender.maxAge);
    }

    public static List<TexturedCircleRender> get() {
        return CIRCLE_RENDERER;
    }

    public static void clear() {
        CIRCLE_RENDERER.clear();
    }

}
