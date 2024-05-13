package net.shirojr.boatism.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.shirojr.boatism.util.tag.BoatismTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {
    @Shadow private static float red;
    @Shadow private static float green;
    @Shadow private static float blue;
    @Inject(method = "applyFog", at = @At("HEAD"), cancellable = true)
    private static void boatism$applyOilFog(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo ci) {
        if (!isInOil(camera.getBlockPos())) return;
        RenderSystem.setShaderFogStart(-3);
        RenderSystem.setShaderFogEnd(2);
        ci.cancel();
    }

    @ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clearColor(FFFF)V", ordinal = 1))
    private static void boatism$applyFogColorForOil(Args args, Camera camera, float tickDelta, ClientWorld world, int viewDistance, float skyDarkness) {
        if (!isInOil(camera.getBlockPos())) return;
        float oilColor = 0.10f;
        red = oilColor;
        green = oilColor;
        blue = oilColor;
    }

    @Unique
    private static boolean isInOil(BlockPos pos) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) return false;
        return client.world.getFluidState(pos).isIn(BoatismTags.Fluids.OIL);
    }
}
