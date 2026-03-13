package net.midget807.smollib.event.client;

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
import net.minecraft.client.world.ClientWorld;
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
                SquareRendererManager.get().forEach(squareRender -> renderSquare(context, world, client, camera, squareRender));
                CubeRendererManager.tick();
                CubeRendererManager.get().forEach(cubeRender -> renderCube(context, world, client, camera, cubeRender));
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

    private static void renderCube(WorldRenderContext context, ClientWorld world, MinecraftClient client, Camera camera, CubeRender cube) {
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


            //Render West
            bufferBuilder.vertex(transformation, (float) (cube.getWestEdge() - camX), (float) (cube.getUpEdge() - camY), (float) (cube.getNorthEdge() - camZ)).texture(1, 0).next();
            bufferBuilder.vertex(transformation, (float) (cube.getWestEdge() - camX), (float) (cube.getDownEdge() - camY), (float) (cube.getNorthEdge() - camZ)).texture(1, 1).next();
            bufferBuilder.vertex(transformation, (float) (cube.getWestEdge() - camX), (float) (cube.getDownEdge() - camY), (float) (cube.getSouthEdge() - camZ)).texture(0, 1).next();
            bufferBuilder.vertex(transformation, (float) (cube.getWestEdge() - camX), (float) (cube.getUpEdge() - camY), (float) (cube.getSouthEdge() - camZ)).texture(0, 0).next();
            //Render East
            bufferBuilder.vertex(transformation, (float) (cube.getEastEdge() - camX), (float) (cube.getUpEdge() - camY), (float) (cube.getNorthEdge() - camZ)).texture(1, 0).next();
            bufferBuilder.vertex(transformation, (float) (cube.getEastEdge() - camX), (float) (cube.getDownEdge() - camY), (float) (cube.getNorthEdge() - camZ)).texture(1, 1).next();
            bufferBuilder.vertex(transformation, (float) (cube.getEastEdge() - camX), (float) (cube.getDownEdge() - camY), (float) (cube.getSouthEdge() - camZ)).texture(0, 1).next();
            bufferBuilder.vertex(transformation, (float) (cube.getEastEdge() - camX), (float) (cube.getUpEdge() - camY), (float) (cube.getSouthEdge() - camZ)).texture(0, 0).next();


            //Render North
            bufferBuilder.vertex(transformation, (float) (cube.getWestEdge() - camX), (float) (cube.getUpEdge() - camY), (float) (cube.getNorthEdge() - camZ)).texture(0, 0).next();
            bufferBuilder.vertex(transformation, (float) (cube.getEastEdge() - camX), (float) (cube.getUpEdge() - camY), (float) (cube.getNorthEdge() - camZ)).texture(1, 0).next();
            bufferBuilder.vertex(transformation, (float) (cube.getEastEdge() - camX), (float) (cube.getDownEdge() - camY), (float) (cube.getNorthEdge() - camZ)).texture(1, 1).next();
            bufferBuilder.vertex(transformation, (float) (cube.getWestEdge() - camX), (float) (cube.getDownEdge() - camY), (float) (cube.getNorthEdge() - camZ)).texture(0, 1).next();
            //Render South
            bufferBuilder.vertex(transformation, (float) (cube.getWestEdge() - camX), (float) (cube.getUpEdge() - camY), (float) (cube.getSouthEdge() - camZ)).texture(0, 0).next();
            bufferBuilder.vertex(transformation, (float) (cube.getEastEdge() - camX), (float) (cube.getUpEdge() - camY), (float) (cube.getSouthEdge() - camZ)).texture(1, 0).next();
            bufferBuilder.vertex(transformation, (float) (cube.getEastEdge() - camX), (float) (cube.getDownEdge() - camY), (float) (cube.getSouthEdge() - camZ)).texture(1, 1).next();
            bufferBuilder.vertex(transformation, (float) (cube.getWestEdge() - camX), (float) (cube.getDownEdge() - camY), (float) (cube.getSouthEdge() - camZ)).texture(0, 1).next();



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

    private static void renderSquare(WorldRenderContext context, ClientWorld world, MinecraftClient client, Camera camera, SquareRender square) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        double viewDistance = client.options.getClampedViewDistance() * 16;

        double camX = context.camera().getPos().x;
        double camY = context.camera().getPos().y;
        double camZ = context.camera().getPos().z;

        Matrix4f transformation = context.matrixStack().peek().getPositionMatrix();

        if (isNotBeyondRenderDistance(camera, square, viewDistance)) {

            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();

            if (SquareRendererManager.squareBuffer != null) {
                SquareRendererManager.squareBuffer.close();
            }
            SquareRendererManager.squareBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);

            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

            switch (square.getAxis()) {
                case X: {
                    //Render X
                    bufferBuilder.vertex(transformation, (float) (square.getCenterX() - camX), (float) (square.getUpEdge() - camY), (float) (square.getNorthEdge() - camZ)).texture(1, 0).next();
                    bufferBuilder.vertex(transformation, (float) (square.getCenterX() - camX), (float) (square.getDownEdge() - camY), (float) (square.getNorthEdge() - camZ)).texture(1, 1).next();
                    bufferBuilder.vertex(transformation, (float) (square.getCenterX() - camX), (float) (square.getDownEdge() - camY), (float) (square.getSouthEdge() - camZ)).texture(0, 1).next();
                    bufferBuilder.vertex(transformation, (float) (square.getCenterX() - camX), (float) (square.getUpEdge() - camY), (float) (square.getSouthEdge() - camZ)).texture(0, 0).next();
                    break;
                }
                case Y: {
                    //Render Y
                    bufferBuilder.vertex(transformation, (float) (square.getWestEdge() - camX), (float) (square.getCenterY() - camY), (float) (square.getNorthEdge() - camZ)).texture(0, 0).next();
                    bufferBuilder.vertex(transformation, (float) (square.getEastEdge() - camX), (float) (square.getCenterY() - camY), (float) (square.getNorthEdge() - camZ)).texture(1, 0).next();
                    bufferBuilder.vertex(transformation, (float) (square.getEastEdge() - camX), (float) (square.getCenterY() - camY), (float) (square.getSouthEdge() - camZ)).texture(1, 1).next();
                    bufferBuilder.vertex(transformation, (float) (square.getWestEdge() - camX), (float) (square.getCenterY() - camY), (float) (square.getSouthEdge() - camZ)).texture(0, 1).next();
                    break;
                }
                case Z: {
                    //Render Z
                    bufferBuilder.vertex(transformation, (float) (square.getWestEdge() - camX), (float) (square.getUpEdge() - camY), (float) (square.getCenterZ() - camZ)).texture(0, 0).next();
                    bufferBuilder.vertex(transformation, (float) (square.getEastEdge() - camX), (float) (square.getUpEdge() - camY), (float) (square.getCenterZ() - camZ)).texture(1, 0).next();
                    bufferBuilder.vertex(transformation, (float) (square.getEastEdge() - camX), (float) (square.getDownEdge() - camY), (float) (square.getCenterZ() - camZ)).texture(1, 1).next();
                    bufferBuilder.vertex(transformation, (float) (square.getWestEdge() - camX), (float) (square.getDownEdge() - camY), (float) (square.getCenterZ() - camZ)).texture(0, 1).next();
                    break;
                }
            }
            
            BufferBuilder.BuiltBuffer builtBuffer = bufferBuilder.end();
            SquareRendererManager.squareBuffer.bind();
            SquareRendererManager.squareBuffer.upload(builtBuffer);
            VertexBuffer.unbind();

            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            /** Bit shifting hex colors into that fuckass 256^3 ratio */
            float r = (square.color >> 16 & 0xFF) / 255.0f;
            float g = (square.color >> 8 & 0xFF) / 255.0f;
            float b = (square.color & 0xFF) / 255.0f;
            RenderSystem.setShaderColor(r, g, b, 1.0f);
            RenderSystem.setShaderTexture(0, ModTextureIds.DEBUG);
            if (SquareRendererManager.squareBuffer != null) {
                SquareRendererManager.squareBuffer.bind();
                ShaderProgram shaderProgram = RenderSystem.getShader();
                SquareRendererManager.squareBuffer.draw(RenderSystem.getModelViewStack().peek().getPositionMatrix(), RenderSystem.getProjectionMatrix(), shaderProgram);
            }

            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
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
  