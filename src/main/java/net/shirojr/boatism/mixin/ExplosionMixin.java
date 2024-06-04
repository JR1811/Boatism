package net.shirojr.boatism.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.shirojr.boatism.fluid.BoatismFluids;
import net.shirojr.boatism.util.BoatismProperties;
import net.shirojr.boatism.util.LoggerUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(Explosion.class)
public class ExplosionMixin {
    @Shadow
    @Final
    private World world;

    @Shadow
    @Final
    private double x;
    @Shadow
    @Final
    private double z;
    @Shadow
    @Final
    private double y;
    @Unique
    final List<BlockPos> blocksInExplosion = new ArrayList<>();

    @Inject(method = "collectBlocksAndDamageEntities",
            at = @At(value = "CONSTANT", args = "floatValue=0.3", ordinal = 0, shift = At.Shift.AFTER)
    )
    private void boatism$collectForHeat(CallbackInfo ci, @Local(name = "j") int posX, @Local(name = "k") int posY, @Local(name = "l") int posZ) {
        if (world.isClient()) return;
        BlockPos pos = BlockPos.ofFloored(posX, posY, posZ).add(BlockPos.ofFloored(x, y, z));
        LoggerUtil.devLogger(pos.toString());
        if (this.blocksInExplosion.stream().anyMatch(posInList ->
                posInList.getX() == pos.getX() && posInList.getY() == pos.getY() && posInList.getZ() == pos.getZ())) return;
        int heatRange = 4;
        Vec3d explosionOrigin = new Vec3d(this.x, this.y, this.z);
        if (pos.toCenterPos().subtract(explosionOrigin).length() > heatRange) return;
        this.blocksInExplosion.add(pos);
    }

    @Inject(method = "affectWorld", at = @At("HEAD"))
    private void boatism$heatUpOilFluid(boolean particles, CallbackInfo ci) {
        if (world.isClient()) return;
        for (BlockPos pos : this.blocksInExplosion) {
            FluidState state = world.getFluidState(pos);
            LoggerUtil.devLogger(state.getBlockState().getBlock().getTranslationKey());
            if (!(BoatismFluids.OIL.contains(state))) continue;

            Vec3d entryPos = pos.toCenterPos();
            Vec3d originPos = new Vec3d(x, y, z);
            double distance = entryPos.subtract(originPos).length();
            double maxDistance = this.blocksInExplosion.get(this.blocksInExplosion.size() - 1).toCenterPos().subtract(originPos).length();
            int heat = (int) MathHelper.lerp(distance / maxDistance, 0, 9);
            BlockState newState = state.with(BoatismProperties.FLUID_HEAT, heat).getBlockState();
            world.setBlockState(pos, newState);
        }
    }
}
