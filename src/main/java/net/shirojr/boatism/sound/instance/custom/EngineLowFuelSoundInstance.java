package net.shirojr.boatism.sound.instance.custom;

import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.sound.BoatismSounds;
import net.shirojr.boatism.sound.instance.SoundInstanceState;
import net.shirojr.boatism.util.SoundInstanceHelper;

import java.util.List;

public class EngineLowFuelSoundInstance extends BoatismSoundInstance implements SoundInstanceState {
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

    @Override
    public boolean isMainSound() {
        return true;
    }
}
