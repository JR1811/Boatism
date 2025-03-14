package net.shirojr.boatism.network.packet;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.shirojr.boatism.api.BoatEngineCoupler;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.network.BoatismNetworkIdentifiers;
import net.shirojr.boatism.screen.handler.EngineControlScreenHandler;

public record OpenEngineInventoryPacket(int entityNetworkId) implements CustomPayload {
    public static final Id<OpenEngineInventoryPacket> IDENTIFIER = new Id<>(BoatismNetworkIdentifiers.OPEN_ENGINE_SCREEN.getId());

    public static final PacketCodec<RegistryByteBuf, OpenEngineInventoryPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, OpenEngineInventoryPacket::entityNetworkId,
            OpenEngineInventoryPacket::new
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
        if (!(player.getVehicle() instanceof BoatEngineCoupler boatEngineCoupler)) return;

        boatEngineCoupler.boatism$getBoatEngineEntityUuid().map(world::getEntity).ifPresent(entity -> {
            if (!(world.getEntityById(entityNetworkId) instanceof BoatEngineEntity boatEngine)) return;
            player.openHandledScreen(new ExtendedScreenHandlerFactory<OpenEngineInventoryPacket>() {
                @Override
                public OpenEngineInventoryPacket getScreenOpeningData(ServerPlayerEntity player) {
                    return new OpenEngineInventoryPacket(entity.getId());
                }

                @Override
                public Text getDisplayName() {
                    return boatEngine.getDisplayName();
                }

                @Override
                public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                    Inventory engineInventory = boatEngine.getMountedInventory();
                    return new EngineControlScreenHandler(syncId, playerInventory, engineInventory,
                            boatEngine.getPropertyDelegate(), new OpenEngineInventoryPacket(entityNetworkId));
                }
            });
        });
    }
}
