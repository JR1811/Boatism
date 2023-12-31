package net.shirojr.boatism.sound.instance.custom;

import net.minecraft.util.math.MathHelper;
import net.shirojr.boatism.BoatismClient;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.sound.BoatismSoundManager;
import net.shirojr.boatism.sound.BoatismSounds;
import net.shirojr.boatism.sound.instance.SoundInstanceState;
import net.shirojr.boatism.util.BoatEngineHandler;
import net.shirojr.boatism.util.SoundInstanceIdentifier;

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
