package net.shirojr.boatism.network.packet;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.shirojr.boatism.api.BoatEngineCoupler;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.init.BoatismSounds;
import net.shirojr.boatism.network.BoatismNetworkIdentifiers;
import net.shirojr.boatism.util.handler.BoatEngineHandler;

import java.util.Optional;
import java.util.UUID;

public class PowerLevelChangePacket {
    public static final Identifier IDENTIFIER = BoatismNetworkIdentifiers.POWER_LEVEL_CHANGE.getId();

    public static void sendPacket(double delta) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeDouble(delta);
        ClientPlayNetworking.send(IDENTIFIER, buf);
    }

    public static void handlePacket(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
                              PacketByteBuf buf, PacketSender responseSender) {
        double delta = buf.readDouble(); // -1.0 = down, 1.0 = up
        server.execute(() -> {
        if (!(player.getVehicle() instanceof BoatEntity boatEntity)) return;
        Optional<UUID> boatEngineEntityUuid = Optional.ofNullable(((BoatEngineCoupler) boatEntity).boatism$getBoatEngineEntityUuid());
        boatEngineEntityUuid.ifPresent(uuid -> {
            BoatEngineEntity boatEngineEntity = (BoatEngineEntity) ((ServerWorld) player.getWorld()).getEntity(uuid);
            if (boatEngineEntity == null) return;
            BoatEngineHandler engineHandler = boatEngineEntity.getEngineHandler();
            int newPowerLevel = engineHandler.getPowerLevel() + (int) delta;
            newPowerLevel = Math.min(newPowerLevel, engineHandler.getMaxPowerLevel());
            newPowerLevel = Math.max(newPowerLevel, 0);

            if (newPowerLevel > engineHandler.getPowerLevel()) {
                player.playSound(BoatismSounds.BOAT_ENGINE_POWER_UP, SoundCategory.NEUTRAL, 0.7f, 1.0f);
            } else if (newPowerLevel < engineHandler.getPowerLevel()) {
                player.playSound(BoatismSounds.BOAT_ENGINE_POWER_DOWN, SoundCategory.NEUTRAL, 0.7f, 1.0f);
            }

            engineHandler.setPowerLevel(newPowerLevel);
            player.sendMessage(Text.translatable("mouse.boatism.power_level", newPowerLevel), true);
            });
        });
    }
}
