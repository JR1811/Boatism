package net.shirojr.boatism.sound.instance.custom;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.MathHelper;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.sound.BoatismSounds;
import net.shirojr.boatism.sound.instance.SoundInstanceState;
import net.shirojr.boatism.util.BoatEngineHandler;
import net.shirojr.boatism.util.LoggerUtil;

@Environment(EnvType.CLIENT)
public class EngineOverheatingSoundInstance extends BoatismSoundInstance implements SoundInstanceState {
    private static final int COOL_BUFFER_MAX = 3;
    private int isCoolBuffer = 0;
    public EngineOverheatingSoundInstance(BoatEngineEntity boatEngineEntity) {
        super(boatEngineEntity, BoatismSounds.BOAT_ENGINE_HEAT, 60, 60);
        transitionState = TransitionState.STARTING;
    }

    @Override
    public boolean canPlay() {
        return super.canPlay() && engineHandler.isHeatingUp();
    }

    @Override
    public void tick() {
        super.tick();

        LoggerUtil.devLogger(String.format("Instance: %s | currentTick: %s | transitionTick: %s | transitionState: %s", this.id, currentTick, transitionTick, transitionState));

        float normalizedOverheatTicks = engineHandler.getOverheat() / BoatEngineHandler.MAX_BASE_OVERHEAT;
        boolean isPotentiallyCooling = !engineHandler.isHeatingUp();
        if (isPotentiallyCooling) {
            if (isCoolBuffer > COOL_BUFFER_MAX) {
                if (!transitionState.equals(TransitionState.FINISHING)) {
                    this.finishSoundInstance();
                    return;
                }
            } else {
                --isCoolBuffer;
            }
        } else {
            isCoolBuffer = 0;
        }

        BoatismSoundInstance.defaultSoundHandling(this);
        this.volume = MathHelper.lerp(normalizedOverheatTicks, 0.0f, 0.7f);
    }

    @Override
    public boolean isMainSound() {
        return false;
    }

}
