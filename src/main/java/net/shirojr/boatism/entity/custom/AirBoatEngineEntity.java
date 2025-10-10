package net.shirojr.boatism.entity.custom;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.world.World;

public class AirBoatEngineEntity extends BoatEngineEntity {
    public AirBoatEngineEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    public AirBoatEngineEntity(World world, BoatEntity hookedBoatEntity) {
        super(world, hookedBoatEntity);
    }
}