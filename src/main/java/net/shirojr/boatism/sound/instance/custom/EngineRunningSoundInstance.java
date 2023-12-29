package net.shirojr.boatism.sound.instance.custom;

import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.sound.BoatismSounds;
import net.shirojr.boatism.sound.instance.SoundInstanceState;

public class EngineRunningSoundInstance extends BoatismSoundInstance implements SoundInstanceState {
    public EngineRunningSoundInstance(BoatEngineEntity entity) {
        super(entity, BoatismSounds.BOAT_ENGINE_DEFAULT, 100, 80);
    }

    @Override
    public boolean canPlay() {
        return super.canPlay() && !boatEngineEntity.isSubmerged();
    }

    @Override
    public void tick() {
        super.tick();
        if (/*boatEngineEntity.getEngineHandler().isLowOnFuel() ||*/ boatEngineEntity.isSubmerged() || boatEngineEntity.hasLowHealth()) {
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
