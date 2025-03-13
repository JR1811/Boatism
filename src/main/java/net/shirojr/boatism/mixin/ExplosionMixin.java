package net.shirojr.boatism.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import net.shirojr.boatism.init.BoatismFluids;
import net.shirojr.boatism.init.BoatismProperties;
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
import java.util.Optional;

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
    private final List<BlockPos> blocksInExplosion = new ArrayList<>();


    @WrapOperation(method = "collectBlocksAndDamageEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/explosion/ExplosionBehavior;getBlastResistance(Lnet/minecraft/world/explosion/Explosion;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/fluid/FluidState;)Ljava/util/Optional;"
            )
    )
    private Optional<Float> boatism$collectForHeat2(ExplosionBehavior instance, Explosion explosion, BlockView world,
                                                    BlockPos pos, BlockState blockState, FluidState fluidState,
                                                    Operation<Optional<Float>> original) {
        Vec3d explosionOrigin = new Vec3d(this.x, this.y, this.z);
        BlockPos currentPos = BlockPos.ofFloored(explosionOrigin);

        if (!world.getBlockState(currentPos).isAir()) {
            LoggerUtil.devLogger("Block: %s | at : %s".formatted(world.getBlockState(currentPos).getBlock(), currentPos.toString()));
            //if (this.blocksInExplosion.stream().noneMatch(posInList -> posInList.equals(currentPos))) {
                this.blocksInExplosion.add(currentPos);
            //}
        }
        return original.call(instance, explosion, world, pos, blockState, fluidState);
    }

    @Inject(method = "affectWorld", at = @At("HEAD"))
    private void boatism$heatUpOilFluid(boolean particles, CallbackInfo ci) {
        if (world.isClient()) return;
        for (BlockPos pos : this.blocksInExplosion) {
            FluidState state = world.getFluidState(pos);
            if (!(BoatismFluids.OIL.contains(state))) continue;

            Vec3d entryPos = pos.toCenterPos();
            Vec3d originPos = new Vec3d(x, y, z);
            double distance = entryPos.subtract(originPos).length();
            double maxDistance = this.blocksInExplosion.get(this.blocksInExplosion.size() - 1).toCenterPos().subtract(originPos).length();
            double heat = MathHelper.lerp((maxDistance - distance) / maxDistance, 0, 9);
            FluidState newState = state.with(BoatismProperties.FLUID_HEAT, (int) heat);
            world.setBlockState(pos, newState.getBlockState(), Block.NOTIFY_ALL);
        }
    }
}
