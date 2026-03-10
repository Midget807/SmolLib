package net.midget807.smollib;

import net.fabricmc.api.ModInitializer;

import net.midget807.smollib.registry.ModItems;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmolLibMain implements ModInitializer {
	public static final String MOD_ID = "smollib";
    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("wowww prettytyy");

        ModItems.registerModItems();

	}
}