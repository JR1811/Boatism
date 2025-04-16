package net.shirojr.boatism.sound.instance.custom;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.init.BoatismSounds;
import net.shirojr.boatism.sound.instance.SoundInstanceState;

@Environment(EnvType.CLIENT)
public class EngineRunningFastFuelInjectedSoundInstance extends BoatismSoundInstance implements SoundInstanceState {
    public EngineRunningFastFuelInjectedSoundInstance(BoatEngineEntity entity) {
        super(entity, BoatismSounds.BOAT_ENGINE_DEFAULT_FUEL_INJECTOR, 100, 80);
    }

    @Override
    public boolean canPlay() {
        return super.canPlay();
    }

    @Override
    public void tick() {
        super.tick();
        if (!boatEngineEntity.isRunning() && !transitionState.equals(TransitionState.FINISHING)) {
            this.finishSoundInstance();
            return;
        }
        BoatismSoundInstance.defaultSoundHandling(this);
        BoatismSoundInstance.transformSoundForTransition(this.volume, this.pitch, this);
        BoatismSoundInstance.transformSoundForEngineLoad(this.volume, this.pitch, this);
    }

    @Override
    public boolean isMainSound() {
        return true;
    }
}
