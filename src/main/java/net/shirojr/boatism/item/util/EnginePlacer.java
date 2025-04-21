package net.shirojr.boatism.item.util;

import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.world.World;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;

public interface EnginePlacer {
    BoatEngineEntity getEngineInstance(World world, BoatEntity boat);
}
