package net.shirojr.boatism.mixin.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.PacketByteBuf;
import net.shirojr.boatism.Boatism;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.network.BoatismNetworkIdentifiers;
import net.shirojr.boatism.api.BoatEngineCoupler;
import net.shirojr.boatism.util.handler.EntityHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.UUID;

@Mixin(Mouse.class)
public class MouseMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;scrollInHotbar(D)V"), cancellable = true)
    private void boatism$onScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (!Boatism.CONFIG.powerLevelScrolling) return;
        GameOptions options = client.options;
        boolean mouseScrolled = options.getDiscreteMouseScroll().getValue();
        double delta = (mouseScrolled ? Math.signum(horizontal) : vertical) * options.getMouseWheelSensitivity().getValue();
        ClientPlayerEntity player = client.player;

        if (player == null || player.getVehicle() == null) return;
        if (!(player.getVehicle() instanceof BoatEntity boatEntity)) return;
        if (boatEntity.getFirstPassenger() == null || !boatEntity.getFirstPassenger().equals(player)) return;

        Optional<UUID> boatEngineUuid = ((BoatEngineCoupler) boatEntity).boatism$getBoatEngineEntityUuid();
        boatEngineUuid.ifPresent(uuid -> {
            Optional<BoatEngineEntity> boatEngineEntity = EntityHandler.getBoatEngineEntityFromUuid(uuid, player.getWorld(), player.getPos(), 10);
            if (boatEngineEntity.isEmpty() || !boatEngineEntity.get().isRunning()) return;

            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeDouble(delta);
            ClientPlayNetworking.send(BoatismNetworkIdentifiers.SCROLLED.getIdentifier(), buf);
            ci.cancel();
        });
    }
}
