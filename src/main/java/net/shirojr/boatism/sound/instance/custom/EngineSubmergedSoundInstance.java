package net.shirojr.boatism.sound.instance.custom;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.init.BoatismSounds;
import net.shirojr.boatism.sound.instance.SoundInstanceState;

@Environment(EnvType.CLIENT)
public class EngineSubmergedSoundInstance extends BoatismSoundInstance implements SoundInstanceState {
    public EngineSubmergedSoundInstance(BoatEngineEntity boatEngineEntity) {
        super(boatEngineEntity, BoatismSounds.BOAT_ENGINE_UNDERWATER, 100, 80);
    }

    @Override
    public boolean canPlay() {
        return super.canPlay() && boatEngineEntity.isSubmerged();
    }

    @Override
    public void tick() {
        super.tick();
        if (!boatEngineEntity.isSubmerged() && !transitionState.equals(TransitionState.FINISHING)) {
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
