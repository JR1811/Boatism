package net.shirojr.boatism.mixin.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.PacketByteBuf;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.network.BoatismC2S;
import net.shirojr.boatism.util.BoatEngineCoupler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(Mouse.class)
public class MouseMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;scrollInHotbar(D)V"), cancellable = true)
    private void boatism$onScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        GameOptions options = client.options;
        boolean mouseScrolled = options.getDiscreteMouseScroll().getValue();
        double delta = (mouseScrolled ? Math.signum(horizontal) : vertical) * options.getMouseWheelSensitivity().getValue();
        ClientPlayerEntity player = client.player;

        if (player == null || player.getVehicle() == null) return;
        if (!(player.getVehicle() instanceof BoatEntity boatEntity)) return;
        if (boatEntity.getFirstPassenger() == null || !boatEntity.getFirstPassenger().equals(player)) return;
        Optional<BoatEngineEntity> boatEngineEntity = ((BoatEngineCoupler)boatEntity).boatism$getBoatEngineEntity();
        if (boatEngineEntity.isEmpty()) return;

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeDouble(delta);
        ClientPlayNetworking.send(BoatismC2S.SCROLL_PACKET, buf);
        ci.cancel();
    }
}
