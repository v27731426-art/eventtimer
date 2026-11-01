package dev.eventtimer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
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
                "Таймер /event", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_BRACKET, cat));
        ClientTickEvents.END_CLIENT_TICK.register(EventTimerMod::tick);
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
                Identifier.of("eventtimer", "hud"), (ctx, tc) -> render(ctx));
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

    static int lerp(int a, int b, float t) {
        int r = (int) (((a >> 16) & 255) + (((b >> 16) & 255) - ((a >> 16) & 255)) * t);
        int g = (int) (((a >> 8) & 255) + (((b >> 8) & 255) - ((a >> 8) & 255)) * t);
        int bl = (int) ((a & 255) + ((b & 255) - (a & 255)) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | bl;
    }

    static void render(DrawContext ctx) {
        if (!enabled) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        int left = INTERVAL_TICKS - Math.min(timer, INTERVAL_TICKS);
        int sec = (left + 19) / 20;
        String text = "/" + COMMAND + "  " + (sec / 60) + ":" + String.format("%02d", sec % 60);

        int x = 6, y = 6, h = 22;
        int w = mc.textRenderer.getWidth(text) + 12;

        ctx.fill(x, y, x + w, y + h, 0xAA101018);
        ctx.fill(x, y, x + 2, y + h, 0xFF55AAFF);
        ctx.drawTextWithShadow(mc.textRenderer, text, x + 6, y + 3, 0xFFFFFFFF);

        float p = 1f - left / (float) INTERVAL_TICKS;
        int trackW = w - 12;
        ctx.fill(x + 6, y + h - 5, x + 6 + trackW, y + h - 3, 0x55FFFFFF);
        ctx.fill(x + 6, y + h - 5, x + 6 + (int) (trackW * p), y + h - 3, lerp(0xFF55FF55, 0xFFFF5555, p));
    }
}
