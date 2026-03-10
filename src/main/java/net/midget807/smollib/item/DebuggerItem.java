package net.midget807.smollib.item;

import net.midget807.smollib.rendering.ShapeRenderer;
import net.midget807.smollib.rendering.manager.SquareRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

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
                Vec3d origin = player.getPos();
                ShapeRenderer.renderSquare(origin, Direction.UP, 100, 2, 0xff0000);
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
            player.sendMessage(Text.literal("No squares: " + SquareRendererManager.get().size()), true);
        }
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }
}
