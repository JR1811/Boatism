package net.shirojr.boatism.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.network.PacketByteBuf;
import net.shirojr.boatism.api.BoatEngineCoupler;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.network.BoatismNetworkIdentifiers;
import net.shirojr.boatism.util.handler.EntityHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.Optional;
import java.util.UUID;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow
    @Nullable
    public ClientPlayerEntity player;

    @WrapOperation(method = "handleInputEvents",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;wasPressed()Z", ordinal = 0),
            slice = @Slice(
                    from = @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/client/option/GameOptions;inventoryKey:Lnet/minecraft/client/option/KeyBinding;",
                            ordinal = 0)
            )
    )
    private boolean openEngineInventoryScreen(KeyBinding instance, Operation<Boolean> original) {
        Optional<BoatEngineEntity> boatEngine = getBoatEngineEntity(player);
        while (original.call(instance)) {
            if (boatEngine.isEmpty()) return true;
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeVarInt(boatEngine.get().getId());
            ClientPlayNetworking.send(BoatismNetworkIdentifiers.OPEN_ENGINE_SCREEN.getIdentifier(), buf);
        }
        return false;
    }

    @Unique
    private Optional<BoatEngineEntity> getBoatEngineEntity(ClientPlayerEntity clientPlayer) {
        if (clientPlayer == null || clientPlayer.getWorld() == null) return Optional.empty();
        if (!(clientPlayer.getVehicle() instanceof BoatEngineCoupler coupler)) return Optional.empty();
        if (coupler.boatism$getBoatEngineEntityUuid().isEmpty()) return Optional.empty();
        UUID uuid = coupler.boatism$getBoatEngineEntityUuid().get();
        return EntityHandler.getBoatEngineEntityFromUuid(uuid, clientPlayer.getWorld(), clientPlayer.getPos(), 3);
    }
}
