package net.shirojr.boatism.sound.instance;

import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;

public class BoatismSoundInstance extends MovingSoundInstance {
    protected final BoatEngineEntity boatEngineEntity;
    private int currentTick = 0;
    private float distance = 0.0f;

    public BoatismSoundInstance(BoatEngineEntity boatEngineEntity, SoundEvent soundEvent) {
        super(soundEvent, SoundCategory.NEUTRAL, SoundInstance.createRandom());
        this.boatEngineEntity = boatEngineEntity;
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = 0.0f;
        this.x = boatEngineEntity.getX();
        this.y = boatEngineEntity.getY();
        this.z = boatEngineEntity.getZ();
    }

    @Override
    public void tick() {
        if (boatEngineEntity.getWorld().getTickManager().shouldTick()) this.currentTick++;
        else return;

        if (this.boatEngineEntity.isRemoved()) {
            this.setDone();
            return;
        }
        this.x = this.boatEngineEntity.getX();
        this.y = this.boatEngineEntity.getY();
        this.z = this.boatEngineEntity.getZ();
    }

    @Override
    public boolean canPlay() {
        return !this.boatEngineEntity.isSilent();
    }

    protected static void defaultSoundHandling(BoatismSoundInstance soundInstance) {
        double horizontalVelocity = soundInstance.boatEngineEntity.getVelocity().horizontalLength();
        if (soundInstance.boatEngineEntity.getWorld().getTickManager().shouldTick()) {
            soundInstance.distance = MathHelper.clamp(soundInstance.distance + 0.0025f, 0.0f, 1.0f);

            float velocityClamp = (float)MathHelper.clamp(horizontalVelocity, 0.0f, 0.5f);
            soundInstance.volume = MathHelper.lerp(velocityClamp, 0.0f, 0.7f);
            soundInstance.pitch = MathHelper.lerp(velocityClamp, 0.9f, 1.2f);
        } else {
            soundInstance.distance = 0.0f;
            soundInstance.volume = 0.0f;
            soundInstance.pitch = 1.0f;
        }
    }
}
