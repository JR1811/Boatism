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
    public EngineOverheatingSoundInstance(BoatEngineEntity boatEngineEntity) {
        super(boatEngineEntity, BoatismSounds.BOAT_ENGINE_HEAT, 60, 60);
        transitionState = TransitionState.STARTING;
    }

    @Override
    public boolean canPlay() {
        return super.canPlay() && engineHandler.getOverheat() > 0;
    }

    @Override
    public void tick() {
        super.tick();
        float normalizedOverheatTicks = (float) engineHandler.getOverheat() / BoatEngineHandler.MAX_OVERHEAT;
        boolean isCool = !engineHandler.isHeatingUp();
        LoggerUtil.devLogger("overheat: " + engineHandler.getOverheat());
        if (isCool && !transitionState.equals(TransitionState.FINISHING)) {
            this.finishSoundInstance();
            return;
        }
        BoatismSoundInstance.defaultSoundHandling(this);
        this.volume = MathHelper.lerp(normalizedOverheatTicks, 0.0f, 0.7f);
    }

    @Override
    public boolean isMainSound() {
        return false;
    }

}
