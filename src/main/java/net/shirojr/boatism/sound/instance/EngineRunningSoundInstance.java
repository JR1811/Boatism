package net.shirojr.boatism.sound.instance;

import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.sound.BoatismSounds;

public class EngineRunningSoundInstance extends BoatismSoundInstance {
    public EngineRunningSoundInstance(BoatEngineEntity entity) {
        super(entity, BoatismSounds.BOAT_ENGINE_DEFAULT);
    }

    @Override
    public boolean canPlay() {
        return super.canPlay() && !boatEngineEntity.isSubmerged() && !boatEngineEntity.hasLowFuel();
    }

    @Override
    public void tick() {
        super.tick();
        if (boatEngineEntity.hasLowFuel() || boatEngineEntity.isSubmerged() || boatEngineEntity.hasLowHealth()) {
            this.setDone();
            return;
        }
        BoatismSoundInstance.defaultSoundHandling(this);
    }
}
