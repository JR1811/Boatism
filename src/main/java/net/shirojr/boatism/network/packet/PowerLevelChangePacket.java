package net.shirojr.boatism.network.packet;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.shirojr.boatism.api.BoatEngineCoupler;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.init.BoatismSounds;
import net.shirojr.boatism.network.BoatismNetworkIdentifiers;
import net.shirojr.boatism.util.handler.BoatEngineHandler;

import java.util.Optional;
import java.util.UUID;

// Delta -1.0 = down, 1.0 = up
public record PowerLevelChangePacket(double delta) implements CustomPayload {
    public static final Id<PowerLevelChangePacket> IDENTIFIER = new Id<>(BoatismNetworkIdentifiers.POWER_LEVEL_CHANGE.getId());

    public static final PacketCodec<RegistryByteBuf, PowerLevelChangePacket> CODEC = PacketCodec.tuple(
            PacketCodecs.DOUBLE, PowerLevelChangePacket::delta,
            PowerLevelChangePacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return IDENTIFIER;
    }

    public void sendPacket() {
        ClientPlayNetworking.send(this);
    }

    public void handlePacket(ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();
        ServerWorld world = player.getServerWorld();
        if (!(player.getVehicle() instanceof BoatEntity boatEntity)) return;
        Optional<UUID> boatEngineEntityUuid = ((BoatEngineCoupler) boatEntity).boatism$getBoatEngineEntityUuid();

        boatEngineEntityUuid.ifPresent(uuid -> {
            BoatEngineEntity boatEngineEntity = (BoatEngineEntity) ((ServerWorld) player.getWorld()).getEntity(uuid);
            if (boatEngineEntity == null) return;
            if (!boatEngineEntity.isRunning()) return;
            BoatEngineHandler engineHandler = boatEngineEntity.getEngineHandler();
            int newPowerLevel = engineHandler.getPowerLevel() + (int) delta;
            newPowerLevel = Math.min(newPowerLevel, engineHandler.getMaxPowerLevel());
            newPowerLevel = Math.max(newPowerLevel, 0);

            if (newPowerLevel > engineHandler.getPowerLevel()) {
                world.playSound(null, player.getBlockPos(), BoatismSounds.BOAT_ENGINE_POWER_UP, SoundCategory.NEUTRAL, 0.7f, 1.0f);
            } else if (newPowerLevel < engineHandler.getPowerLevel()) {
                world.playSound(null, player.getBlockPos(), BoatismSounds.BOAT_ENGINE_POWER_DOWN, SoundCategory.NEUTRAL, 0.7f, 1.0f);
            }

            engineHandler.setPowerLevel(newPowerLevel);
            player.sendMessage(Text.translatable("mouse.boatism.power_level", newPowerLevel), true);
        });
    }
}
