package net.shirojr.boatism.event.custom;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import net.shirojr.boatism.Boatism;
import net.shirojr.boatism.network.BoatismNetworkIdentifiers;

public class KeyBindEvents {
    private static KeyBinding POWER_LEVEL_UP;
    private static KeyBinding POWER_LEVEL_DOWN;

    private static final String BOATISM_KEYBINDINGS_GROUP = "key.%s.group".formatted(Boatism.MODID);
    public static void register() {
        POWER_LEVEL_UP = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.%s.power_level_up".formatted(Boatism.MODID),
                        InputUtil.Type.KEYSYM, InputUtil.UNKNOWN_KEY.getCode(), BOATISM_KEYBINDINGS_GROUP)
        );
        POWER_LEVEL_DOWN = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.%s.power_level_down".formatted(Boatism.MODID),
                        InputUtil.Type.KEYSYM, InputUtil.UNKNOWN_KEY.getCode(), BOATISM_KEYBINDINGS_GROUP)
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            while (POWER_LEVEL_UP.wasPressed()) {
                sendPowerLevelChangePacket(1);
            }
            while (POWER_LEVEL_DOWN.wasPressed()) {
                sendPowerLevelChangePacket(-1);
            }
        });
    }

    private static void sendPowerLevelChangePacket(int delta) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeDouble(delta);
        ClientPlayNetworking.send(BoatismNetworkIdentifiers.POWER_LEVEL_CHANGE.getIdentifier(), buf);
    }
}
