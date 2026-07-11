package com.particlecontrol;

import net.minecraft.client.util.InputUtil;
import com.particlecontrol.config.ParticleConfig;
import com.particlecontrol.gui.ParticleControlScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Identifier;

public class ParticleControlClient implements ClientModInitializer {

    private static KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        ParticleConfig.load();

        KeyBinding.Category category = new KeyBinding.Category(Identifier.of("particlecontrol", "main"));

        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.particlecontrol.open_gui",
                InputUtil.Type.KEYSYM,
                InputUtil.GLFW_KEY_P,
                category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new ParticleControlScreen(null));
                }
            }
        });
    }
}
