package net.shirojr.boatism.util;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import net.shirojr.boatism.init.BoatismGameRules;

public class BoatEngineExplosionBehaviour extends ExplosionBehavior {
    @Override
    public boolean shouldDamage(Explosion explosion, Entity entity) {
        return super.shouldDamage(explosion, entity);
    }

    @Override
    public boolean canDestroyBlock(Explosion explosion, BlockView world, BlockPos pos, BlockState state, float power) {
        if (explosion.getEntity() == null) return super.canDestroyBlock(explosion, world, pos, state, power);
        World serverWorld = explosion.getEntity().getWorld();
        return serverWorld.getGameRules().getBoolean(BoatismGameRules.DESTRUCTIVE_ENGINE_EXPLOSION);
    }
}
