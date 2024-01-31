package net.shirojr.boatism.sound.instance.custom;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.sound.BoatismSounds;
import net.shirojr.boatism.sound.instance.SoundInstanceState;

@Environment(EnvType.CLIENT)
public class EngineLowFuelSoundInstance extends BoatismSoundInstance implements SoundInstanceState {
    public EngineLowFuelSoundInstance(BoatEngineEntity entity) {
        super(entity, BoatismSounds.BOAT_ENGINE_LOW_FUEL, 100, 80);
    }

    @Override
    public boolean canPlay() {
        return super.canPlay() && boatEngineEntity.getEngineHandler().isLowOnFuel();
    }

    @Override
    public void tick() {
        super.tick();
        boolean shouldStop = !boatEngineEntity.getEngineHandler().isLowOnFuel() || !boatEngineEntity.isRunning();
        if (shouldStop && !transitionState.equals(TransitionState.FINISHING)) {
            this.finishSoundInstance();
            return;
        }
        BoatismSoundInstance.defaultSoundHandling(this);
        transformSoundForTransition(this.volume, this.pitch, this, false, true);
    }

    @Override
    public boolean isMainSound() {
        return true;
    }
}
