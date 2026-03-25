package net.midget807.smollib.event.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.midget807.smollib.rendering.CubeRender;
import net.midget807.smollib.rendering.TexturedCircleRender;
import net.midget807.smollib.rendering.TexturedSquareRender;
import net.midget807.smollib.rendering.manager.CubeRendererManager;
import net.midget807.smollib.rendering.manager.TexturedCircleRendererManager;
import net.midget807.smollib.rendering.manager.TexturedSquareRendererManager;
import net.midget807.smollib.util.ModTextureIds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class WorldRendererListener {
    @Nullable
    private static VertexBuffer testBuffer;

    public static void execute() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            ClientWorld world = client.world;
            Camera camera = context.camera();

            if (world != null) {
                TexturedSquareRendererManager.tick();
                TexturedSquareRendererManager.get().forEach(texturedSquareRender -> renderSquare(context, world, client, camera, texturedSquareRender));
                CubeRendererManager.tick();
                CubeRendererManager.get().forEach(cubeRender -> renderCube(context, world, client, camera, cubeRender));

                TexturedCircleRendererManager.tick();
                TexturedCircleRendererManager.get().forEach(texturedCircleRender -> renderCircle(context, world, client, camera, texturedCircleRender));

                //renderTest(context, world, client, camera);
            }
        });
    }

    private static void renderCircle(WorldRenderContext context, ClientWorld world, MinecraftClient client, Camera camera, TexturedCircleRender circle) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        double viewDistance = client.options.getClampedViewDistance() * 16;

        double camX = context.camera().getPos().x;
        double camY = context.camera().getPos().y;
        double camZ = context.camera().getPos().z;

        MatrixStack matrices = context.matrixStack();
        matrices.push();

        // Shifts teh matrix to the local pos of the square
        matrices.translate(-camX, -camY, -camZ);
        matrices.translate(circle.getCentreX(), circle.getCentreY(), circle.getCentreZ());

        // Applies transformations
        circle.TRANSFORMATIONS.forEach(matrices::multiply);

        Matrix4f transformation = matrices.peek().getPositionMatrix();

        // Shifts the matrix back to global pos so the vertices aren't fucked
        matrices.translate(-circle.getCentreX(), -circle.getCentreY(), -circle.getCentreZ());
        matrices.translate(camX, camY, camZ);

        if (isNotBeyondRenderDistance(camera, circle, viewDistance)) {
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();

            if (TexturedCircleRendererManager.circleBuffer != null) {
                TexturedCircleRendererManager.circleBuffer.close();
            }
            TexturedCircleRendererManager.circleBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);

            bufferbuilder.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_TEXTURE);

            //todo vertices
            final double forAngleDelta = (float) (Math.PI / 200);
            for (double theta = 0; theta < Math.PI * 2; theta += forAngleDelta) {
                double phi = theta + forAngleDelta;
                double sinT = Math.sin(theta);
                double cosT =  Math.cos(theta);
                double sinP = Math.sin(phi);
                double cosP =  Math.cos(phi);
                double sinTCenter = Math.sin(theta);
                double cosTCenter =  Math.cos(theta);
                double sinPCenter = Math.sin(phi);
                double cosPCenter =  Math.cos(phi);

                double outerSinT = sinT * circle.getRadius();
                double outerSinP = sinP * circle.getRadius();
                double innerSinT = sinT * circle.centerOffset;
                double innerSinP = sinP * circle.centerOffset;
                double outerCosT = cosT * circle.getRadius();
                double outerCosP = cosP * circle.getRadius();
                double innerCosT = cosT * circle.centerOffset;
                double innerCosP = cosP * circle.centerOffset;

                bufferbuilder.vertex(transformation, (float) (circle.getCentreX() + innerSinT - camX), (float) (circle.getCentreY() - camY), (float) (circle.getCentreZ() + innerCosT - camZ)).texture(0, 1).next();
                bufferbuilder.vertex(transformation, (float) (circle.getCentreX() + outerSinT - camX), (float) (circle.getCentreY() - camY), (float) (circle.getCentreZ() + outerCosT - camZ)).texture(1, 1).next();
                bufferbuilder.vertex(transformation, (float) (circle.getCentreX() + outerSinP - camX), (float) (circle.getCentreY() - camY), (float) (circle.getCentreZ() + outerCosP - camZ)).texture(1, 0).next();

                if (circle.centerOffset > 0.0) {
                    bufferbuilder.vertex(transformation, (float) (circle.getCentreX() + innerSinT - camX), (float) (circle.getCentreY() - camY), (float) (circle.getCentreZ() + innerCosT - camZ)).texture(0, 1).next();
                    bufferbuilder.vertex(transformation, (float) (circle.getCentreX() + outerSinP - camX), (float) (circle.getCentreY() - camY), (float) (circle.getCentreZ() + outerCosP - camZ)).texture(1, 1).next();
                    bufferbuilder.vertex(transformation, (float) (circle.getCentreX() + innerSinP - camX), (float) (circle.getCentreY() - camY), (float) (circle.getCentreZ() + innerCosP - camZ)).texture(1, 0).next();
                }
            }

            matrices.pop();

            BufferBuilder.BuiltBuffer builtBuffer = bufferbuilder.end();
            TexturedCircleRendererManager.circleBuffer.bind();
            TexturedCircleRendererManager.circleBuffer.upload(builtBuffer);
            VertexBuffer.unbind();

            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            /* Bit shifting hex colors into that fuckass 256^3 ratio */
            float r = (circle.color >> 16 & 0xFF) / 255.0f;
            float g = (circle.color >> 8 & 0xFF) / 255.0f;
            float b = (circle.color & 0xFF) / 255.0f;
            RenderSystem.setShaderColor(r, g, b, 1.0f);
            RenderSystem.setShaderTexture(0, ModTextureIds.DEBUG_SOLID);
            if (TexturedCircleRendererManager.circleBuffer != null) {
                TexturedCircleRendererManager.circleBuffer.bind();
                ShaderProgram shaderProgram = RenderSystem.getShader();
                Matrix4f positionMatrix = RenderSystem.getModelViewStack().peek().getPositionMatrix();

                TexturedCircleRendererManager.circleBuffer.draw(positionMatrix, RenderSystem.getProjectionMatrix(), shaderProgram);
            }

            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
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

    private static void renderSquare(WorldRenderContext context, ClientWorld world, MinecraftClient client, Camera camera, TexturedSquareRender square) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        double viewDistance = client.options.getClampedViewDistance() * 16;

        double camX = context.camera().getPos().x;
        double camY = context.camera().getPos().y;
        double camZ = context.camera().getPos().z;

        MatrixStack matrices = context.matrixStack();
        matrices.push();

        // Shifts teh matrix to the local pos of the square
        matrices.translate(-camX, -camY, -camZ);
        matrices.translate(square.getCenterX(), square.getCenterY(), square.getCenterZ());

        // Applies transformations
        square.TRANSFORMATIONS.forEach(matrices::multiply);

        Matrix4f transformation = matrices.peek().getPositionMatrix();

        // Shifts the matrix back to global pos so the vertices aren't fucked
        matrices.translate(-square.getCenterX(), -square.getCenterY(), -square.getCenterZ());
        matrices.translate(camX, camY, camZ);


        if (isNotBeyondRenderDistance(camera, square, viewDistance)) {

            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();

            if (TexturedSquareRendererManager.squareBuffer != null) {
                TexturedSquareRendererManager.squareBuffer.close();
            }
            TexturedSquareRendererManager.squareBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);

            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

            double smallestX = Math.max(MathHelper.floor(camX - viewDistance), square.getWestEdge());
            double largestX = Math.min(MathHelper.ceil(camX + viewDistance), square.getEastEdge());
            double smallestY = Math.max(MathHelper.floor(camY - viewDistance), square.getDownEdge());
            double largestY = Math.min(MathHelper.ceil(camY + viewDistance), square.getUpEdge());
            double smallestZ = Math.max(MathHelper.floor(camZ - viewDistance), square.getNorthEdge());
            double largestZ = Math.min(MathHelper.ceil(camZ + viewDistance), square.getSouthEdge());
            float xIterator = (MathHelper.floor(smallestX) & 1) * 0.5f;
            float yIterator = (MathHelper.floor(smallestY) & 1) * 0.5f;
            float zIterator = (MathHelper.floor(smallestZ) & 1) * 0.5f;

            switch (square.direction) {
                case EAST, WEST: {
                    for (double i = square.getNorthEdge(); i < square.getSouthEdge(); i += square.textureSize) {
                        double zRemainder = Math.min(square.textureSize, square.getSouthEdge() - i);
                        float uRemainder = 1.0f;
                        if (square.getSouthEdge() - i < square.textureSize) {
                            uRemainder = (float) ((square.getSouthEdge() - i) / square.textureSize);
                        }
                        for (double j = square.getDownEdge(); j < square.getUpEdge(); j += square.textureSize) {
                            double yRemainder = Math.min(square.textureSize, square.getUpEdge() - j);
                            float vRemainder = 0.0f;
                            if (square.getUpEdge() - j < square.textureSize) {
                                vRemainder = 1.0f - (float) ((square.getUpEdge() - j) / square.textureSize);
                            }
                            bufferBuilder.vertex(transformation, (float) (square.getCenterX() - camX), (float) (j - camY), (float) (i - camZ)).texture(0, 1).next();
                            bufferBuilder.vertex(transformation, (float) (square.getCenterX() - camX), (float) (j + yRemainder - camY), (float) (i - camZ)).texture(0, vRemainder).next();
                            bufferBuilder.vertex(transformation, (float) (square.getCenterX() - camX), (float) (j + yRemainder - camY), (float) (i + zRemainder - camZ)).texture(uRemainder, vRemainder).next();
                            bufferBuilder.vertex(transformation, (float) (square.getCenterX() - camX), (float) (j - camY), (float) (i + zRemainder - camZ)).texture(uRemainder, 1).next();
                        }
                    }
                    break;
                }
                case UP, DOWN: {
                    for (double i = square.getWestEdge(); i < square.getEastEdge(); i += square.textureSize) {
                        double xRemainder = Math.min(square.textureSize, square.getEastEdge() - i);
                        float uRemainder = 1.0f;
                        if (square.getEastEdge() - i < square.textureSize) {
                            uRemainder = (float) ((square.getEastEdge() - i) / square.textureSize);
                        }
                        for (double j = square.getNorthEdge(); j < square.getSouthEdge(); j += square.textureSize) {
                            double zRemainder = Math.min(square.textureSize, square.getSouthEdge() - j);
                            float vRemainder = 0.0f;
                            if (square.getSouthEdge() - j < square.textureSize) {
                                vRemainder = 1.0f - (float) ((square.getSouthEdge() - j) / square.textureSize);
                            }
                            bufferBuilder.vertex(transformation, (float) (i - camX), (float) (square.getCenterY() - camY), (float) (j - camZ)).texture(0, 1).next();
                            bufferBuilder.vertex(transformation, (float) (i - camX), (float) (square.getCenterY() - camY), (float) (j + zRemainder - camZ)).texture(0, vRemainder).next();
                            bufferBuilder.vertex(transformation, (float) (i + xRemainder - camX), (float) (square.getCenterY() - camY), (float) (j + zRemainder - camZ)).texture(uRemainder, vRemainder).next();
                            bufferBuilder.vertex(transformation, (float) (i + xRemainder - camX), (float) (square.getCenterY() - camY), (float) (j - camZ)).texture(uRemainder, 1).next();
                        }
                    }
                    break;
                }
                case NORTH, SOUTH: {
                    for (double i = square.getWestEdge(); i < square.getEastEdge(); i += square.textureSize) {
                        double xRemainder = Math.min(square.textureSize, square.getEastEdge() - i);
                        float uRemainder = 1.0f;
                        if (square.getEastEdge() - i < square.textureSize) {
                            uRemainder = (float) ((square.getEastEdge() - i) / square.textureSize);
                        }
                        for (double j = square.getDownEdge(); j < square.getUpEdge(); j += square.textureSize) {
                            double yRemainder = Math.min(square.textureSize, square.getUpEdge() - j);
                            float vRemainder = 0.0f;
                            if (square.getUpEdge() - j < square.textureSize) {
                                vRemainder = 1.0f - (float) ((square.getUpEdge() - j) / square.textureSize);
                            }
                            bufferBuilder.vertex(transformation, (float) (i - camX), (float) (j - camY), (float) (square.getCenterZ() - camZ)).texture(0, 1).next();
                            bufferBuilder.vertex(transformation, (float) (i - camX), (float) (j + yRemainder - camY), (float) (square.getCenterZ() - camZ)).texture(0, vRemainder).next();
                            bufferBuilder.vertex(transformation, (float) (i + xRemainder - camX), (float) (j + yRemainder - camY), (float) (square.getCenterZ() - camZ)).texture(uRemainder, vRemainder).next();
                            bufferBuilder.vertex(transformation, (float) (i + xRemainder - camX), (float) (j - camY), (float) (square.getCenterZ() - camZ)).texture(uRemainder, 1).next();
                        }
                    }
                    break;
                }
            }

            matrices.pop();

            BufferBuilder.BuiltBuffer builtBuffer = bufferBuilder.end();
            TexturedSquareRendererManager.squareBuffer.bind();
            TexturedSquareRendererManager.squareBuffer.upload(builtBuffer);
            VertexBuffer.unbind();

            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            /* Bit shifting hex colors into that fuckass 256^3 ratio */
            float r = (square.color >> 16 & 0xFF) / 255.0f;
            float g = (square.color >> 8 & 0xFF) / 255.0f;
            float b = (square.color & 0xFF) / 255.0f;
            RenderSystem.setShaderColor(r, g, b, 1.0f);
            RenderSystem.setShaderTexture(0, ModTextureIds.DEBUG);
            if (TexturedSquareRendererManager.squareBuffer != null) {
                TexturedSquareRendererManager.squareBuffer.bind();
                ShaderProgram shaderProgram = RenderSystem.getShader();
                Matrix4f positionMatrix = RenderSystem.getModelViewStack().peek().getPositionMatrix();

                TexturedSquareRendererManager.squareBuffer.draw(positionMatrix, RenderSystem.getProjectionMatrix(), shaderProgram);
            }

            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }



    private static boolean isNotBeyondRenderDistance(Camera camera, TexturedCircleRender shape, double clampedViewDistance) {
        return !(camera.getPos().x < shape.getEastPoint() - clampedViewDistance)
                || !(camera.getPos().x > shape.getWestPoint() + clampedViewDistance)
                || !(camera.getPos().z < shape.getSouthPoint() - clampedViewDistance)
                || !(camera.getPos().z > shape.getNorthPoint() + clampedViewDistance)
                || !(camera.getPos().y < shape.getUpPoint() - clampedViewDistance)
                || !(camera.getPos().y > shape.getDownPoint() + clampedViewDistance);
    }

    private static boolean isNotBeyondRenderDistance(Camera camera, TexturedSquareRender texturedSquareRender, double clampedViewDistance) {
        return !(camera.getPos().x < texturedSquareRender.getEastEdge() - clampedViewDistance)
                || !(camera.getPos().x > texturedSquareRender.getWestEdge() + clampedViewDistance)
                || !(camera.getPos().z < texturedSquareRender.getSouthEdge() - clampedViewDistance)
                || !(camera.getPos().z > texturedSquareRender.getNorthEdge() + clampedViewDistance)
                || !(camera.getPos().y < texturedSquareRender.getUpEdge() - clampedViewDistance)
                || !(camera.getPos().y > texturedSquareRender.getDownEdge() + clampedViewDistance);
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
  