package net.shirojr.boatism.sound.instance.custom;

import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.sound.BoatismSounds;
import net.shirojr.boatism.sound.instance.SoundInstanceState;

public class EngineLowFuelSoundInstance extends BoatismSoundInstance implements SoundInstanceState {
    public EngineLowFuelSoundInstance(BoatEngineEntity entity) {
        super(entity, BoatismSounds.BOAT_ENGINE_LOW_FUEL, 100, 80);
    }

    @Override
    public boolean canPlay() {
        return super.canPlay() && boatEngineEntity.getEngineHandler().isLowOnFuel() && boatEngineEntity.isRunning();
    }

    @Override
    public void tick() {
        super.tick();
        if (!boatEngineEntity.getEngineHandler().isLowOnFuel()) {
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
