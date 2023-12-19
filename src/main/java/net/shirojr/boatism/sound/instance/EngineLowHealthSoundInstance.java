package net.shirojr.boatism.sound.instance;

import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.sound.BoatismSounds;

public class EngineLowHealthSoundInstance extends BoatismSoundInstance {
    public EngineLowHealthSoundInstance(BoatEngineEntity entity) {
        super(entity, BoatismSounds.BOAT_ENGINE_LOW_HEALTH);
    }

    @Override
    public boolean canPlay() {
        return super.canPlay() && boatEngineEntity.hasLowHealth();
    }

    @Override
    public void tick() {
        super.tick();
        if (boatEngineEntity.hasLowHealth()) {
            this.setDone();
            return;
        }
        BoatismSoundInstance.defaultSoundHandling(this);
    }
}
