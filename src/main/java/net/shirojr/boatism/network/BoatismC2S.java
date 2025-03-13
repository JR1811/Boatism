package net.shirojr.boatism.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.shirojr.boatism.api.BoatEngineCoupler;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.init.BoatismSounds;
import net.shirojr.boatism.screen.handler.EngineControlScreenHandler;
import net.shirojr.boatism.util.handler.BoatEngineHandler;

import java.util.Optional;
import java.util.UUID;

public class BoatismC2S {
    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(BoatismNetworkIdentifiers.POWER_LEVEL_CHANGE.getIdentifier(),
                BoatismC2S::handlePowerLevelChangePackets);
        ServerPlayNetworking.registerGlobalReceiver(BoatismNetworkIdentifiers.OPEN_ENGINE_SCREEN.getIdentifier(),
                BoatismC2S::handleOpenEngineInventoryPackets);
    }

    private static void handlePowerLevelChangePackets(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
                                                      PacketByteBuf buf, PacketSender responseSender) {
        double delta = buf.readDouble(); // -1.0 = down, 1.0 = up
        server.execute(() -> {
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
                    player.playSound(BoatismSounds.BOAT_ENGINE_POWER_UP, SoundCategory.NEUTRAL, 0.7f, 1.0f);
                } else if (newPowerLevel < engineHandler.getPowerLevel()) {
                    player.playSound(BoatismSounds.BOAT_ENGINE_POWER_DOWN, SoundCategory.NEUTRAL, 0.7f, 1.0f);
                }

                engineHandler.setPowerLevel(newPowerLevel);
                player.sendMessage(Text.translatable("mouse.boatism.power_level", newPowerLevel), true);
            });
        });
    }

    private static void handleOpenEngineInventoryPackets(MinecraftServer server, ServerPlayerEntity player,
                                                         ServerPlayNetworkHandler handler, PacketByteBuf buf,
                                                         PacketSender responseSender) {
        int entityId = buf.readVarInt();
        server.execute(() -> {
            ServerWorld world = (ServerWorld) player.getWorld();
            if (!(player.getVehicle() instanceof BoatEngineCoupler boatEngineCoupler)) return;
            boatEngineCoupler.boatism$getBoatEngineEntityUuid().ifPresent(uuid -> {
                if (!(world.getEntityById(entityId) instanceof BoatEngineEntity boatEngine)) return;
                player.openHandledScreen(new ExtendedScreenHandlerFactory() {
                    @Override
                    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
                        buf.writeUuid(uuid);
                    }

                    @Override
                    public Text getDisplayName() {
                        return boatEngine.getDisplayName();
                    }

                    @Override
                    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                        Inventory engineInventory = boatEngine.getMountedInventory();
                        PacketByteBuf transferBuf = PacketByteBufs.create();
                        transferBuf.writeVarInt(entityId);
                        return new EngineControlScreenHandler(syncId, playerInventory, engineInventory,
                                boatEngine.getPropertyDelegate(), transferBuf);
                    }
                });
            });
        });
    }
}
