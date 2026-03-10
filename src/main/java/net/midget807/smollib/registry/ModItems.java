package net.midget807.smollib.registry;

import net.midget807.smollib.SmolLibMain;
import net.midget807.smollib.item.DebuggerItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Rarity;

public class ModItems {
    public static final Item Debugger = registerItem("debugger", new DebuggerItem(new Item.Settings().fireproof().rarity(Rarity.EPIC).maxCount(1)));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, SmolLibMain.id(name), item);
    }

    public static void registerModItems() {
        SmolLibMain.LOGGER.info("Registering Mod Items");
    }
}
