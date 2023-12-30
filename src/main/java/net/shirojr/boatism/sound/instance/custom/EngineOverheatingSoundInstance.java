package net.shirojr.boatism.sound.instance.custom;

import net.minecraft.util.math.MathHelper;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.sound.BoatismSounds;
import net.shirojr.boatism.sound.instance.SoundInstanceState;
import net.shirojr.boatism.util.BoatEngineHandler;

public class EngineOverheatingSoundInstance extends BoatismSoundInstance implements SoundInstanceState {
    public EngineOverheatingSoundInstance(BoatEngineEntity boatEngineEntity) {
        super(boatEngineEntity, BoatismSounds.BOAT_ENGINE_HEAT, 60, 60);
    }

    @Override
    public boolean canPlay() {
        return super.canPlay();
    }

    @Override
    public void tick() {
        super.tick();
        BoatismSoundInstance.defaultSoundHandling(this);
        BoatEngineHandler boatEngineHandler = this.engineHandler;
        float normalizedOverheatTicks = (float) boatEngineHandler.getOverheat() / BoatEngineHandler.MAX_OVERHEAT_TICKS;

        if (boatEngineHandler.getOverheat() <= 0 || boatEngineHandler.isOverheating()) {
            this.setDone();
            return;
        }
        this.volume = MathHelper.lerp(normalizedOverheatTicks, 0.0f, 0.7f);


    }

    @Override
    public boolean isMainSound() {
        return false;
    }

}
