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
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

public class WorldRendererListener {
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

        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

        bufferBuilder.vertex(transformation, 0, -55, 0).texture(0, 0).next();
        bufferBuilder.vertex(transformation, 5, -55, 0).texture(1, 0).next();
        bufferBuilder.vertex(transformation, 5, -55, 5).texture(1, 1).next();
        bufferBuilder.vertex(transformation, 0, -55, 5).texture(0, 1).next();

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        tessellator.draw();
    }

    private static void renderCubes(WorldRenderContext context, ClientWorld world, MinecraftClient client, Camera camera, CubeRender cube) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        double viewDistance = client.options.getClampedViewDistance() * 16;
        if (isNotBeyondRenderDistance(camera, cube, viewDistance)) {
            //Variable e in world border controls the transparency depending on the player position to the world border
            final double camX = camera.getPos().x;
            final double camY = camera.getPos().y;
            final double camZ = camera.getPos().z;
            //Variable h = getFarPlaneDistance is for the world border to render infinitely vertically
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
            RenderSystem.setShaderTexture(0, ModTextureIds.DEBUG);
            RenderSystem.depthMask(MinecraftClient.isFabulousGraphicsOrBetter());
            MatrixStack matrices = context.matrixStack(); /** The transformation matrix */
            matrices.push();
            matrices.loadIdentity();
            RenderSystem.applyModelViewMatrix();
            Matrix4f transformation = context.matrixStack().peek().getPositionMatrix();
            /** Bit shifting hex colors into that fuckass 256^3 ratio */
            float r = (cube.color >> 16 & 0xFF) / 255.0f;
            float g = (cube.color >> 8 & 0xFF) / 255.0f;
            float b = (cube.color & 0xFF) / 255.0f;
            RenderSystem.setShaderColor(r, g, b, 1);
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            RenderSystem.polygonOffset(-3.0f, -3.0f);
            RenderSystem.enablePolygonOffset();
            RenderSystem.disableCull();

            bufferbuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

            double smallestX = Math.max(MathHelper.floor(camX - viewDistance), cube.getWestEdge());
            double largestX = Math.min(MathHelper.ceil(camX + viewDistance), cube.getEastEdge());
            double smallestY = Math.max(MathHelper.floor(camY - viewDistance), cube.getDownEdge());
            double largestY = Math.min(MathHelper.ceil(camY + viewDistance), cube.getUpEdge());
            double smallestZ = Math.max(MathHelper.floor(camZ - viewDistance), cube.getNorthEdge());
            double largestZ = Math.min(MathHelper.ceil(camZ + viewDistance), cube.getSouthEdge());
            float u1 = (MathHelper.floor(smallestX) & 1) * 0.5f;
            float v0 = (float)(-MathHelper.fractionalPart(camera.getPos().y * 0.5));
            float v1 = v0 + cube.size;

            //Renders South Face
            if (camZ > cube.getSouthEdge() - viewDistance) {
                double deltaX = Math.min(cube.size, largestX - smallestX);
                u1 += cube.size;
                float u0 = (float) deltaX * 0.5f;
                bufferbuilder.vertex(transformation, (float) (smallestX - camX), (float) (cube.getDownEdge() - camY), (float) (cube.getSouthEdge() - camZ)).texture(u0, v1).next();
                bufferbuilder.vertex(transformation, (float) (smallestX + deltaX - camX), (float) (cube.getDownEdge() - camY), (float) (cube.getSouthEdge() - camZ)).texture(u0 + u1, v1).next();
                bufferbuilder.vertex(transformation, (float) (smallestX + deltaX - camX), (float) (cube.getUpEdge() - camY), (float) (cube.getSouthEdge() - camZ)).texture(u0 + u1, v0).next();
                bufferbuilder.vertex(transformation, (float) (smallestX - camX), (float) (cube.getUpEdge() - camY), (float) (cube.getSouthEdge() - camZ)).texture(u0, v0).next();
            }

            if (camZ < cube.getNorthEdge() + viewDistance) {
                bufferbuilder.vertex(cube.getWestEdge() - camX, cube.getDownEdge() - camY, cube.getNorthEdge() - camZ).texture(0, 1).next();
                bufferbuilder.vertex(cube.getEastEdge() - camX, cube.getDownEdge() - camY, cube.getNorthEdge() - camZ).texture(1, 1).next();
                bufferbuilder.vertex(cube.getEastEdge() - camX, cube.getUpEdge() - camY, cube.getNorthEdge() - camZ).texture(1, 0).next();
                bufferbuilder.vertex(cube.getWestEdge() - camX, cube.getUpEdge() - camY, cube.getNorthEdge() - camZ).texture(0, 0).next();
            }
            if (camX > cube.getEastEdge() - viewDistance) {
                bufferbuilder.vertex(cube.getEastEdge(), cube.getDownEdge(), cube.getNorthEdge()).texture(0, 1).next();
                bufferbuilder.vertex(cube.getEastEdge(), cube.getDownEdge(), cube.getSouthEdge()).texture(1, 1).next();
                bufferbuilder.vertex(cube.getEastEdge(), cube.getUpEdge(), cube.getSouthEdge()).texture(1, 0).next();
                bufferbuilder.vertex(cube.getEastEdge(), cube.getUpEdge(), cube.getNorthEdge()).texture(0, 0).next();
            }

            bufferbuilder.vertex(transformation, (float) (0), (float) (-55), (float) (5)).texture(0, 1).next();
            bufferbuilder.vertex(transformation, (float) (5), (float) (-55), (float) (5)).texture(1, 1).next();
            bufferbuilder.vertex(transformation, (float) (5), (float) (-55), (float) (0)).texture(1, 0).next();
            bufferbuilder.vertex(transformation, (float) (0), (float) (-55), (float) (0)).texture(0, 0).next();

            BufferBuilder.BuiltBuffer builtBuffer = bufferbuilder.endNullable();
            if (builtBuffer != null) {
                BufferRenderer.drawWithGlobalProgram(builtBuffer);
            }
            //BufferRenderer.drawWithGlobalProgram(bufferbuilder.end());
            RenderSystem.enableCull();
            RenderSystem.polygonOffset(0.0F, 0.0F);
            RenderSystem.disablePolygonOffset();
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
            matrices.pop();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.depthMask(true);
        }
    }

    private static void renderSquares(WorldRenderContext context, ClientWorld world, MinecraftClient client, Camera camera, SquareRender square) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        double viewDistance = client.options.getClampedViewDistance() * 16;
        if (isNotBeyondRenderDistance(camera, square, viewDistance)) {
            //Variable e in world border controls the transparency depending on the player position to the world border
            double camX = camera.getPos().x;
            double camY = camera.getPos().y;
            double camZ = camera.getPos().z;
            //Variable h = getFarPlaneDistance is for the world border to render infinitely vertically
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
            RenderSystem.setShaderTexture(0, ModTextureIds.DEBUG);
            RenderSystem.depthMask(MinecraftClient.isFabulousGraphicsOrBetter());
            MatrixStack matrices = RenderSystem.getModelViewStack(); /** The transformation matrix */
            matrices.push();
            RenderSystem.applyModelViewMatrix();
            /** Bit shifting hex colors into that fuckass 256^3 ratio */
            float r = (square.color >> 16 & 0xFF) / 255.0f;
            float g = (square.color >> 8 & 0xFF) / 255.0f;
            float b = (square.color & 0xFF) / 255.0f;
            RenderSystem.setShaderColor(r, g, b, 1);
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            RenderSystem.polygonOffset(-3.0f, -3.0f);
            RenderSystem.enablePolygonOffset();
            RenderSystem.disableCull();

            bufferbuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

            double smallestX = Math.max(MathHelper.floor(camX - viewDistance), square.getWestEdge());
            double largestX = Math.min(MathHelper.ceil(camX + viewDistance), square.getEastEdge());
            double smallestY = Math.max(MathHelper.floor(camY - viewDistance), square.getDownEdge());
            double largestY = Math.min(MathHelper.ceil(camY + viewDistance), square.getUpEdge());
            double smallestZ = Math.max(MathHelper.floor(camZ - viewDistance), square.getNorthEdge());
            double largestZ = Math.min(MathHelper.ceil(camZ + viewDistance), square.getSouthEdge());
            float u0 = (MathHelper.floor(smallestX) & 1) * 0.5f;
            float v0 = (MathHelper.floor(smallestY) & 1) * 0.5f;
            float u1 = u0 + (float) square.size / 2;
            float v1 = v0 + (float) square.size / 2;

            if (square.direction == Direction.UP) {

            } else if (square.direction == Direction.SOUTH) {

                if (camZ > square.getSouthEdge() - viewDistance) {

                    bufferbuilder.vertex(smallestX - camX, largestY - camY, square.getSouthEdge() - camZ).texture(0, 1).next();
                    bufferbuilder.vertex(largestX - camX, largestY - camY, square.getSouthEdge() - camZ).texture(1, 1).next();
                    bufferbuilder.vertex(largestX - camX, smallestY - camY, square.getSouthEdge() - camZ).texture(1, 0).next();
                    bufferbuilder.vertex(smallestX - camX, smallestY - camY, square.getSouthEdge() - camZ).texture(0, 0).next();
                }
            } else if (square.direction == Direction.EAST) {}

            BufferRenderer.drawWithGlobalProgram(bufferbuilder.end());
            RenderSystem.enableCull();
            RenderSystem.polygonOffset(0.0F, 0.0F);
            RenderSystem.disablePolygonOffset();
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
            matrices.pop();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.depthMask(true);
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
  