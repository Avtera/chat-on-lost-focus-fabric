package com.atikinbtw.chatonlostfocus;

import com.atikinbtw.chatonlostfocus.config.ConfigScreen;
import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.autoconfig.AutoConfigClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.slf4j.LoggerFactory;

public class ChatOnLostFocus implements ClientModInitializer {

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("chatonlostfocus", "chatonlostfocus")
    );

    private boolean wasFocused = true;

    /**
     * 26.2 uses KEYSYM; 26.3 (SDL) renamed it to KEYBOARD.
     */
    private static InputConstants.Type keyboardType() {
        try {
            return InputConstants.Type.valueOf("KEYBOARD"); // 26.3+
        } catch (IllegalArgumentException e) {
            return InputConstants.Type.valueOf("KEYSYM"); // 26.2
        }
    }

    @Override
    public void onInitializeClient() {
        ConfigScreen.init();
        InputConstants.Type keyType = keyboardType();

        KeyMapping toggleBind = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "keybind.chatonlostfocus.onOffBind",
                keyType,
                InputConstants.KEY_G,
                CATEGORY
        ));
        KeyMapping openConfigScreenBind = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "keybind.chatonlostfocus.openConfigScreenBind",
                keyType,
                InputConstants.KEY_H,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            boolean focused = client.isWindowActive();
            if (wasFocused && !focused && ConfigScreen.config.enabled && client.gui.screen() == null) {
                client.gui.setScreen(new ChatScreen(ConfigScreen.config.textInChat, false));
                client.options.pauseOnLostFocus = false;
            }
            wasFocused = focused;

            if (toggleBind.consumeClick()) {
                ConfigScreen.config.enabled = !ConfigScreen.config.enabled;
                sendActionBarMessage();
            }

            if (openConfigScreenBind.consumeClick()) {
                Minecraft mc = Minecraft.getInstance();
                mc.gui.setScreen(AutoConfigClient.getConfigScreen(ConfigScreen.class, mc.gui.screen()).get());
            }
        });

        LoggerFactory.getLogger("chatonlostfocus").info("[Chat On Lost Focus] Initialized successfully!");
    }

    private void sendActionBarMessage() {
        Component message = Component.translatable(
                ConfigScreen.config.enabled ? "overlay.chatonlostfocus.on" : "overlay.chatonlostfocus.off"
        );
        Minecraft.getInstance().gui.hud.setOverlayMessage(message, true);
    }
}