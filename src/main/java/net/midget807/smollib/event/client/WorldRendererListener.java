package net.midget807.smollib.event.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.midget807.smollib.rendering.SquareRender;
import net.midget807.smollib.rendering.manager.SquareRendererManager;
import net.midget807.smollib.util.ModTextureIds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;

public class WorldRendererListener {
    public static void execute() {
        WorldRenderEvents.END.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            ClientWorld world = client.world;
            Camera camera = context.camera();

            if (world != null) {
                SquareRendererManager.tick();
                SquareRendererManager.get().forEach(squareRender -> renderSquares(context, world, client, camera, squareRender));
            }
        });
    }

    private static void renderSquares(WorldRenderContext context, ClientWorld world, MinecraftClient client, Camera camera, SquareRender squareRender) {
        double clampedViewDistance = client.options.getClampedViewDistance() * 16;
        if (isNotBeyondRenderDistance(camera, squareRender, clampedViewDistance)) {
            double relativeDistanceToEdge = 1.0 - squareRender.getDistanceRelativeToEdge(camera.getPos().x, camera.getPos().y, camera.getPos().z);
            relativeDistanceToEdge = Math.pow(relativeDistanceToEdge, 4.0);
            relativeDistanceToEdge = MathHelper.clamp(relativeDistanceToEdge, 0.0, 1.0);
            double camX = camera.getPos().x;
            double camY = camera.getPos().y;
            double camZ = camera.getPos().z;
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
            RenderSystem.setShaderTexture(0, ModTextureIds.FORCEFIELD);
            RenderSystem.depthMask(MinecraftClient.isFabulousGraphicsOrBetter());
            int color = squareRender.color;
            float red = (color >> 16 & 0xFF) / 255.0f;
            float green = (color >> 8 & 0xFF) / 255.0f;
            float blue = (color & 0xFF) / 255.0f;
            RenderSystem.setShaderColor(red, green, blue, (float) relativeDistanceToEdge);
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            RenderSystem.polygonOffset(-3.0f, -3.0f);
            RenderSystem.enablePolygonOffset();
            RenderSystem.disableCull();

            BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

            double smallestZ = Math.max(MathHelper.floor(camZ - clampedViewDistance), squareRender.getNorthEdge());
            double largestZ = Math.min(MathHelper.ceil(camZ + clampedViewDistance), squareRender.getSouthEdge());
            double smallestX = Math.max(MathHelper.floor(camX - clampedViewDistance), squareRender.getWestEdge());
            double largestX = Math.min(MathHelper.ceil(camX + clampedViewDistance), squareRender.getEastEdge());
            if (camY > squareRender.getUpEdge() - clampedViewDistance) {
                //todo add for loop for texture tiling
                bufferBuilder.vertex((float) (squareRender.getWestEdge() - camX), (float) (squareRender.getUpEdge() - camY), (float) (squareRender.getNorthEdge() - camZ)).texture(0, 0);
                bufferBuilder.vertex((float) (squareRender.getWestEdge() - camX), (float) (squareRender.getUpEdge() - camY), (float) (squareRender.getSouthEdge() - camZ)).texture(1, 0);
                bufferBuilder.vertex((float) (squareRender.getEastEdge() - camX), (float) (squareRender.getUpEdge() - camY), (float) (squareRender.getSouthEdge() - camZ)).texture(1, 1);
                bufferBuilder.vertex((float) (squareRender.getEastEdge() - camX), (float) (squareRender.getUpEdge() - camY), (float) (squareRender.getNorthEdge() - camZ)).texture(0, 1);
            }

            RenderSystem.enableCull();
            RenderSystem.polygonOffset(0.0f, 0.0f);
            RenderSystem.disablePolygonOffset();
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
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
}
