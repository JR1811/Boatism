package net.shirojr.boatism.sound.instance.custom;

import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.sound.BoatismSounds;
import net.shirojr.boatism.sound.instance.SoundInstanceState;
import net.shirojr.boatism.util.BoatEngineHandler;

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
        if (boatEngineEntity.isSubmerged() || boatEngineEntity.hasLowHealth()) {
            this.setDone();
            return;
        }
        BoatismSoundInstance.defaultSoundHandling(this);
        BoatismSoundInstance.transformSoundForTransition(this.volume, this.pitch, this);
        transformSoundForEngineLoad(this.volume, this.pitch, this);
    }

    protected static void transformSoundForEngineLoad(float originalVolume, float originalPitch, BoatismSoundInstance soundInstance) {
        BoatEngineHandler boatEngineHandler = soundInstance.engineHandler;
        int powerLevel = boatEngineHandler.getPowerLevel();

    }

    @Override
    public boolean isMainSound() {
        return true;
    }
}
