package net.shirojr.boatism.sound.instance;

import net.minecraft.sound.SoundEvent;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.sound.BoatismSounds;

public class EngineSubmergedSoundInstance extends BoatismSoundInstance{
    public EngineSubmergedSoundInstance(BoatEngineEntity boatEngineEntity) {
        super(boatEngineEntity, BoatismSounds.BOAT_ENGINE_UNDERWATER);
    }

    @Override
    public boolean canPlay() {
        return super.canPlay() && boatEngineEntity.isSubmerged();
    }

    @Override
    public void tick() {
        super.tick();

    }
}
