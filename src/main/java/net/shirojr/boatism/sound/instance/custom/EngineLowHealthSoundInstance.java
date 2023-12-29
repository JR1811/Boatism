package net.shirojr.boatism.sound.instance.custom;

import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.sound.BoatismSounds;
import net.shirojr.boatism.sound.instance.SoundInstanceState;

public class EngineLowHealthSoundInstance extends BoatismSoundInstance implements SoundInstanceState {
    public EngineLowHealthSoundInstance(BoatEngineEntity entity) {
        super(entity, BoatismSounds.BOAT_ENGINE_LOW_HEALTH, 100, 80);
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

    @Override
    public boolean isMainSound() {
        return true;
    }
}
