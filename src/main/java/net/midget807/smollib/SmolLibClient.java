package net.midget807.smollib;

import net.fabricmc.api.ClientModInitializer;
import net.midget807.smollib.event.client.WorldRendererListener;

public class SmolLibClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WorldRendererListener.execute();
    }
}
