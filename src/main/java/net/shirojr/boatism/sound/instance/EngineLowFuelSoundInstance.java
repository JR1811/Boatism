package net.shirojr.boatism.sound.instance;

import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.sound.BoatismSounds;

public class EngineLowFuelSoundInstance extends BoatismSoundInstance {
    public EngineLowFuelSoundInstance(BoatEngineEntity entity) {
        super(entity, BoatismSounds.BOAT_ENGINE_LOW_FUEL);
    }

    @Override
    public boolean canPlay() {
        return super.canPlay() && boatEngineEntity.hasLowFuel();
    }

    @Override
    public void tick() {
        super.tick();
        if (!boatEngineEntity.hasLowFuel()) {
            this.setDone();
            return;
        }
        BoatismSoundInstance.defaultSoundHandling(this);
    }
}
