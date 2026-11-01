package dev.eventtimer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class EventTimerMod implements ClientModInitializer {
    static final String COMMAND = "event";
    static final int INTERVAL_TICKS = 20 * 60;

    static boolean enabled = false;
    static int timer = 0;
    static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        KeyBinding.Category cat = KeyBinding.Category.create(Identifier.of("eventtimer", "main"));
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Таймер /event delay", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_BRACKET, cat));
        ClientTickEvents.END_CLIENT_TICK.register(EventTimerMod::tick);
    }

    static void tick(MinecraftClient mc) {
        while (toggleKey.wasPressed()) {
            enabled = !enabled;
            timer = INTERVAL_TICKS;
            if (mc.player != null) {
                mc.player.sendMessage(Text.literal("[EventTimer] " + (enabled ? "ВКЛ" : "ВЫКЛ")), true);
            }
        }
        if (!enabled || mc.player == null) return;
        if (++timer >= INTERVAL_TICKS) {
            timer = 0;
            mc.player.networkHandler.sendChatCommand(COMMAND);
        }
    }
}
