package net.midget807.smollib.event.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.midget807.smollib.rendering.CubeRender;
import net.midget807.smollib.rendering.SquareRender;
import net.midget807.smollib.rendering.manager.CubeRendererManager;
import net.midget807.smollib.rendering.manager.SquareRendererManager;
import net.midget807.smollib.util.ModTextureIds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class WorldRendererListener {
    @Nullable
    private static VertexBuffer testBuffer;

    public static void execute() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            ClientWorld world = client.world;
            Camera camera = context.camera();

            if (world != null) {
                SquareRendererManager.tick();
                SquareRendererManager.get().forEach(squareRender -> renderSquares(context, world, client, camera, squareRender));
                CubeRendererManager.tick();
                CubeRendererManager.get().forEach(cubeRender -> {
                    renderCubes(context, world, client, camera, cubeRender);
                });
                renderTest(context, world, client, camera);
            }
        });
    }

    private static void renderTest(WorldRenderContext context, ClientWorld world, MinecraftClient client, Camera camera) {
        Matrix4f transformation = context.matrixStack().peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();

        if (testBuffer != null) {
            testBuffer.close();
        }
        testBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);


        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

        double camX = context.camera().getPos().x;
        double camY = context.camera().getPos().y;
        double camZ = context.camera().getPos().z;

        bufferBuilder.vertex(transformation, (float) (0 - context.camera().getPos().x), (float) (-55 - context.camera().getPos().y), (float) (0 - context.camera().getPos().z)).texture(0, 0).next();
        bufferBuilder.vertex(transformation, (float) (5 - context.camera().getPos().x), (float) (-55 - context.camera().getPos().y), (float) (0 - context.camera().getPos().z)).texture(1, 0).next();
        bufferBuilder.vertex(transformation, (float) (5 - context.camera().getPos().x), (float) (-55 - context.camera().getPos().y), (float) (5 - context.camera().getPos().z)).texture(1, 1).next();
        bufferBuilder.vertex(transformation, (float) (0 - context.camera().getPos().x), (float) (-55 - context.camera().getPos().y), (float) (5 - context.camera().getPos().z)).texture(0, 1).next();

        BufferBuilder.BuiltBuffer builtBuffer = bufferBuilder.end();
        testBuffer.bind();
        testBuffer.upload(builtBuffer);
        VertexBuffer.unbind();

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0f, 0.8f, 0.0f, 1.0f);
        RenderSystem.setShaderTexture(0, ModTextureIds.DEBUG);
        if (testBuffer != null) {
            testBuffer.bind();
            ShaderProgram shaderProgram = RenderSystem.getShader();
            testBuffer.draw(RenderSystem.getModelViewStack().peek().getPositionMatrix(), RenderSystem.getProjectionMatrix(), shaderProgram);
        }

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private static void renderCubes(WorldRenderContext context, ClientWorld world, MinecraftClient client, Camera camera, CubeRender cube) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        double viewDistance = client.options.getClampedViewDistance() * 16;

        double camX = context.camera().getPos().x;
        double camY = context.camera().getPos().y;
        double camZ = context.camera().getPos().z;

        Matrix4f transformation = context.matrixStack().peek().getPositionMatrix();

        if (isNotBeyondRenderDistance(camera, cube, viewDistance)) {

            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();

            if (CubeRendererManager.cubeBuffer != null) {
                CubeRendererManager.cubeBuffer.close();
            }
            CubeRendererManager.cubeBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);

            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

            //Render Down
            bufferBuilder.vertex(transformation, (float) (cube.getWestEdge() - camX), (float) (cube.getDownEdge() - camY), (float) (cube.getNorthEdge() - camZ)).texture(0, 0).next();
            bufferBuilder.vertex(transformation, (float) (cube.getEastEdge() - camX), (float) (cube.getDownEdge() - camY), (float) (cube.getNorthEdge() - camZ)).texture(1, 0).next();
            bufferBuilder.vertex(transformation, (float) (cube.getEastEdge() - camX), (float) (cube.getDownEdge() - camY), (float) (cube.getSouthEdge() - camZ)).texture(1, 1).next();
            bufferBuilder.vertex(transformation, (float) (cube.getWestEdge() - camX), (float) (cube.getDownEdge() - camY), (float) (cube.getSouthEdge() - camZ)).texture(0, 1).next();
            //Render Up
            bufferBuilder.vertex(transformation, (float) (cube.getWestEdge() - camX), (float) (cube.getUpEdge() - camY), (float) (cube.getNorthEdge() - camZ)).texture(0, 0).next();
            bufferBuilder.vertex(transformation, (float) (cube.getEastEdge() - camX), (float) (cube.getUpEdge() - camY), (float) (cube.getNorthEdge() - camZ)).texture(1, 0).next();
            bufferBuilder.vertex(transformation, (float) (cube.getEastEdge() - camX), (float) (cube.getUpEdge() - camY), (float) (cube.getSouthEdge() - camZ)).texture(1, 1).next();
            bufferBuilder.vertex(transformation, (float) (cube.getWestEdge() - camX), (float) (cube.getUpEdge() - camY), (float) (cube.getSouthEdge() - camZ)).texture(0, 1).next();



            BufferBuilder.BuiltBuffer builtBuffer = bufferBuilder.end();
            CubeRendererManager.cubeBuffer.bind();
            CubeRendererManager.cubeBuffer.upload(builtBuffer);
            VertexBuffer.unbind();

            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            /** Bit shifting hex colors into that fuckass 256^3 ratio */
            float r = (cube.color >> 16 & 0xFF) / 255.0f;
            float g = (cube.color >> 8 & 0xFF) / 255.0f;
            float b = (cube.color & 0xFF) / 255.0f;
            RenderSystem.setShaderColor(r, g, b, 1.0f);
            RenderSystem.setShaderTexture(0, ModTextureIds.DEBUG);
            if (CubeRendererManager.cubeBuffer != null) {
                CubeRendererManager.cubeBuffer.bind();
                ShaderProgram shaderProgram = RenderSystem.getShader();
                CubeRendererManager.cubeBuffer.draw(RenderSystem.getModelViewStack().peek().getPositionMatrix(), RenderSystem.getProjectionMatrix(), shaderProgram);
            }

            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    private static void renderSquares(WorldRenderContext context, ClientWorld world, MinecraftClient client, Camera camera, SquareRender square) {

    }



    private static boolean isNotBeyondRenderDistance(Camera camera, SquareRender squareRender, double clampedViewDistance) {
        return !(camera.getPos().x < squareRender.getEastEdge() - clampedViewDistance)
                || !(camera.getPos().x > squareRender.getWestEdge() + clampedViewDistance)
                || !(camera.getPos().z < squareRender.getSouthEdge() - clampedViewDistance)
                || !(camera.getPos().z > squareRender.getNorthEdge() + clampedViewDistance)
                || !(camera.getPos().y < squareRender.getUpEdge() - clampedViewDistance)
                || !(camera.getPos().y > squareRender.getDownEdge() + clampedViewDistance);
    }

    private static boolean isNotBeyondRenderDistance(Camera camera, CubeRender cubeRender, double clampedViewDistance) {
        return !(camera.getPos().x < cubeRender.getEastEdge() - clampedViewDistance)
                || !(camera.getPos().x > cubeRender.getWestEdge() + clampedViewDistance)
                || !(camera.getPos().z < cubeRender.getSouthEdge() - clampedViewDistance)
                || !(camera.getPos().z > cubeRender.getNorthEdge() + clampedViewDistance)
                || !(camera.getPos().y < cubeRender.getUpEdge() - clampedViewDistance)
                || !(camera.getPos().y > cubeRender.getDownEdge() + clampedViewDistance);
    }
}
  