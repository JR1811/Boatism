package net.shirojr.boatism.network.packet;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.shirojr.boatism.api.BoatEngineCoupler;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.network.BoatismNetworkIdentifiers;
import net.shirojr.boatism.screen.handler.EngineControlScreenHandler;

import java.util.Optional;

public class OpenEngineInventoryPacket {
    public static final Identifier IDENTIFIER = BoatismNetworkIdentifiers.OPEN_ENGINE_SCREEN.getId();

    public static void sendPacket(int entityNetworkId) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(entityNetworkId);
        ClientPlayNetworking.send(IDENTIFIER, buf);
    }

    public static void handlePacket(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        int entityNetworkId = buf.readInt();
        ServerWorld world = player.getServerWorld();

        server.execute(() -> {
            if (!(player.getVehicle() instanceof BoatEngineCoupler boatEngineCoupler)) return;

            Optional.ofNullable(boatEngineCoupler.boatism$getBoatEngineEntityUuid())
                    .map(world::getEntity)
                    .ifPresent(entity -> {
                        if (!(world.getEntityById(entityNetworkId) instanceof BoatEngineEntity boatEngine)) return;

                        player.openHandledScreen(new ExtendedScreenHandlerFactory() {
                            @Override
                            public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
                                buf.writeInt(entity.getId());
                            }

                            @Override
                            public Text getDisplayName() {
                                return boatEngine.getDisplayName();
                            }

                            @Override
                            public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                                Inventory engineInventory = boatEngine.getMountedInventory();
                                return new EngineControlScreenHandler(
                                        syncId,
                                        playerInventory,
                                        engineInventory,
                                        boatEngine.getPropertyDelegate(),
                                        entityNetworkId
                                );
                            }
                        });
                    });
        });
    }
}