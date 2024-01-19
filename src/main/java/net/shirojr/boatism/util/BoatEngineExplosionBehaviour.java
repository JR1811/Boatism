package net.shirojr.boatism.util;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import net.shirojr.boatism.gamerule.BoatismGameRules;

public class BoatEngineExplosionBehaviour extends ExplosionBehavior {
    @Override
    public boolean shouldDamage(Explosion explosion, Entity entity) {
        return super.shouldDamage(explosion, entity);
    }

    @Override
    public boolean canDestroyBlock(Explosion explosion, BlockView world, BlockPos pos, BlockState state, float power) {
        if (!(explosion.getEntity().getWorld() instanceof ServerWorld serverWorld))
            return super.canDestroyBlock(explosion, world, pos, state, power);
        return serverWorld.getGameRules().getBoolean(BoatismGameRules.DESTRUCTIVE_ENGINE_EXPLOSION);
    }
}
