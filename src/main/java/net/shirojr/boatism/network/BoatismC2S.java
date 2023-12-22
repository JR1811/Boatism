package net.shirojr.boatism.network;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.shirojr.boatism.Boatism;
import net.shirojr.boatism.util.LoggerUtil;

public class BoatismC2S {
    public static final Identifier SCROLL_PACKET = new Identifier(Boatism.MODID, "scrolled");

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(SCROLL_PACKET, BoatismC2S::handleScrollPackets);
    }

    private static void handleScrollPackets(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
                                            PacketByteBuf buf, PacketSender responseSender) {
        double delta = buf.readDouble(); // -1.0 = down, 1.0 = up
        server.execute(() -> {
            if (!(player.getVehicle() instanceof BoatEntity boatEntity)) return;
            LoggerUtil.devLogger("Scrolled MouseWheel | Mouse delta: " + delta);

            if (delta > 0) {
                int powerLevel = 1;
                player.sendMessage(Text.translatable("mouse.boatism.scrolled_up", powerLevel), true);
            } else {
                int powerLevel = 0;
                player.sendMessage(Text.translatable("mouse.boatism.scrolled_down", powerLevel), true);
            }
        });
    }
}
