package net.midget807.smollib.item;

import net.midget807.smollib.rendering.ShapeRenderer;
import net.midget807.smollib.rendering.TexturedSquareRender;
import net.midget807.smollib.rendering.manager.CubeRendererManager;
import net.midget807.smollib.rendering.manager.TexturedSquareRendererManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class DebuggerItem extends Item {
    public DebuggerItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (player.isSneaking()) {
            if (world.isClient) {

                return TypedActionResult.success(itemStack);
            } else {

                return TypedActionResult.success(itemStack);
            }

        } else {
            if (world.isClient) {
                final Vec3d origin = player.getEyePos().add(player.getRotationVector().normalize().multiply(8));
                Vec3d square1Origin = origin.add(0, 4, 0);
                Vec3d square2Origin = origin.add(0, 6, 0);
                Vec3d square3Origin = origin.add(0, 8, 0);
                TexturedSquareRender texturedSquareRender = new TexturedSquareRender(origin, Direction.EAST, 200, 4, 0x0000ff, 3.0f);
                texturedSquareRender.addTransformation(RotationAxis.NEGATIVE_X.rotationDegrees(45));
                TexturedSquareRendererManager.add(texturedSquareRender);
                return TypedActionResult.success(itemStack);
            } else {

                return TypedActionResult.success(itemStack);
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        if (entity instanceof PlayerEntity player) {
            //player.sendMessage(Text.literal("No. shape: " + SquareRendererManager.get().size()), true);
            player.sendMessage(Text.literal("No. shape: " + CubeRendererManager.get().size()), true);
        }
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }
}
