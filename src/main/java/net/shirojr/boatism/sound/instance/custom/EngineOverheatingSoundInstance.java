package net.shirojr.boatism.sound.instance.custom;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.MathHelper;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.sound.BoatismSounds;
import net.shirojr.boatism.sound.instance.SoundInstanceState;
import net.shirojr.boatism.util.handler.BoatEngineHandler;

@Environment(EnvType.CLIENT)
public class EngineOverheatingSoundInstance extends BoatismSoundInstance implements SoundInstanceState {
    private static final int COOL_BUFFER_MAX = 3;
    private int coolingBuffer = 0;

    public EngineOverheatingSoundInstance(BoatEngineEntity boatEngineEntity) {
        super(boatEngineEntity, BoatismSounds.BOAT_ENGINE_HEAT, 60, 60);
        transitionState = TransitionState.STARTING;
    }

    @Override
    public boolean canPlay() {
        return super.canPlay();
    }

    @Override
    public boolean isMainSound() {
        return false;
    }

    @Override
    public boolean canRunIfEngineIsTurnedOff(BoatEngineEntity boatEngine) {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        float normalizedOverheatTicks = engineHandler.getOverheat() / BoatEngineHandler.MAX_BASE_OVERHEAT;
        boolean isPotentiallyCooling = !engineHandler.isHeatingUp();
        if (isPotentiallyCooling) {
            if (coolingBuffer > COOL_BUFFER_MAX) {
                if (!transitionState.equals(TransitionState.FINISHING)) {
                    this.finishSoundInstance();
                    return;
                }
            } else {
                --coolingBuffer;
            }
        } else {
            coolingBuffer = 0;
        }

        BoatismSoundInstance.defaultSoundHandling(this);
        this.volume = MathHelper.lerp(normalizedOverheatTicks, 0.0f, 0.7f);
    }
}
