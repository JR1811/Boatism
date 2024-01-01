package net.shirojr.boatism.sound.instance.custom;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.sound.BoatismSounds;
import net.shirojr.boatism.sound.instance.SoundInstanceState;
@Environment(EnvType.CLIENT)
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
        if (boatEngineEntity.hasLowHealth() && !transitionState.equals(TransitionState.FINISHING)) {
            this.finishSoundInstance();
            return;
        }
        BoatismSoundInstance.defaultSoundHandling(this);
        BoatismSoundInstance.transformSoundForTransition(this.volume, this.pitch, this);
    }

    @Override
    public boolean isMainSound() {
        return true;
    }
}
