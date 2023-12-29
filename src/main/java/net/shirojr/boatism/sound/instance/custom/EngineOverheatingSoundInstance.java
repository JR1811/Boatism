package net.shirojr.boatism.sound.instance.custom;

import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.sound.BoatismSounds;
import net.shirojr.boatism.sound.instance.SoundInstanceState;

public class EngineOverheatingSoundInstance extends BoatismSoundInstance implements SoundInstanceState {
    public EngineOverheatingSoundInstance(BoatEngineEntity boatEngineEntity) {
        super(boatEngineEntity, BoatismSounds.BOAT_ENGINE_HEAT, 100, 80);
    }

    @Override
    public boolean canPlay() {
        return super.canPlay();
    }

    @Override
    public void tick() {
        super.tick();
        if (boatEngineEntity.getEngineHandler().getOverheatTicks() <= 0 || boatEngineEntity.getEngineHandler().isOverheating()) {
            this.setDone();
            return;
        }
        BoatismSoundInstance.defaultSoundHandling(this);
    }

    @Override
    public boolean isMainSound() {
        return false;
    }

}
