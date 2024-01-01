package net.shirojr.boatism.sound.instance.custom;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.util.BoatEngineHandler;
import net.shirojr.boatism.util.LoggerUtil;

@Environment(EnvType.CLIENT)
public class BoatismSoundInstance extends MovingSoundInstance {
    protected BoatEngineEntity boatEngineEntity;
    protected BoatEngineHandler engineHandler;
    protected final int startTransitionTicks;
    protected final int endTransitionTicks;
    protected TransitionState transitionState;
    protected int currentTick = 0, transitionTick = 0;
    protected float distance = 0.0f;
    protected boolean isFinished = false;

    public BoatismSoundInstance(BoatEngineEntity boatEngineEntity, SoundEvent soundEvent, int startTransitionTicks, int endTransitionTicks) {
        super(soundEvent, SoundCategory.NEUTRAL, SoundInstance.createRandom());
        this.boatEngineEntity = boatEngineEntity;
        this.engineHandler = boatEngineEntity.getEngineHandler();
        this.repeat = true;
        this.repeatDelay = 0;
        this.startTransitionTicks = startTransitionTicks;
        this.endTransitionTicks = endTransitionTicks;
        this.transitionState = TransitionState.STARTING;
        this.volume = 0.0f;
        this.x = boatEngineEntity.getX();
        this.y = boatEngineEntity.getY();
        this.z = boatEngineEntity.getZ();
    }

    @Override
    public void tick() {
        if (boatEngineEntity.getWorld().getTickManager().shouldTick()) this.currentTick++;
        else return;
        if (this.boatEngineEntity.isRemoved() || boatEngineEntity.isDead()) {
            this.finishSoundInstance();
        }

        if (this.transitionState.equals(TransitionState.STARTING)) {
            transitionTick++;
            if (this.transitionTick >= startTransitionTicks) {
                this.transitionState = TransitionState.IDLE;
                this.transitionTick = 0;
            }
        }
        if (transitionState.equals(TransitionState.FINISHING)) {
            if (this.transitionTick < endTransitionTicks) this.transitionTick++;
            else {
                this.isFinished = true;
            }
        }
        if (this instanceof EngineOverheatingSoundInstance) {
            LoggerUtil.devLogger(String.format("Instance: %s | currentTick: %s | transitionTick: %s | transitionState: %s", this.id, currentTick, transitionTick, transitionState));
        }

        this.x = this.boatEngineEntity.getX();
        this.y = this.boatEngineEntity.getY();
        this.z = this.boatEngineEntity.getZ();

        if (isFinished()) {
            this.transitionState = TransitionState.STARTING;
            this.currentTick = 0;
            this.transitionTick = 0;
            this.setDone();
        }

    }

    @Override
    public boolean canPlay() {
        return !this.boatEngineEntity.isSilent();
    }

    @Override
    public boolean shouldAlwaysPlay() {
        return true;
    }

    protected static void defaultSoundHandling(BoatismSoundInstance soundInstance) {
        boolean shouldTick = soundInstance.boatEngineEntity.getWorld().getTickManager().shouldTick();
        if (!shouldTick) {
            soundInstance.distance = 0.0f;
            soundInstance.volume = 0.0f;
            soundInstance.pitch = 1.0f;
        } else {
            transformSoundForDistance(soundInstance);
        }
    }

    protected static void transformSoundForDistance(BoatismSoundInstance soundInstance) {
        soundInstance.boatEngineEntity.getHookedBoatEntity().ifPresentOrElse(boatEntity -> {
            double horizontalVelocity = soundInstance.boatEngineEntity.getVelocity().horizontalLength();
            soundInstance.distance = MathHelper.clamp(soundInstance.distance + 0.0025f, 0.0f, 1.0f);
            float velocityClamp = (float) MathHelper.clamp(horizontalVelocity, 0.0f, 0.5f);
            soundInstance.volume = MathHelper.lerp(velocityClamp, 0.0f, 0.7f);
            soundInstance.pitch = MathHelper.lerp(velocityClamp, 0.9f, 1.2f);
        }, () -> {
            soundInstance.volume = 0.7f;
            soundInstance.pitch = 1.0f;
        });
    }

    protected static void transformSoundForTransition(float originalVolume, float originalPitch, BoatismSoundInstance soundInstance) {
        float normalizedStartTransitionTick = (float) soundInstance.transitionTick / soundInstance.startTransitionTicks;
        float normalizedEndTransitionTick = (float) soundInstance.transitionTick / soundInstance.endTransitionTicks;
        switch (soundInstance.transitionState) {
            case STARTING -> {
                soundInstance.volume = MathHelper.lerp(normalizedStartTransitionTick, 0.0f, originalVolume);
                soundInstance.pitch = MathHelper.lerp(normalizedStartTransitionTick, originalPitch - 0.2f, originalPitch);
            }
            case FINISHING -> {
                soundInstance.volume = MathHelper.lerp(normalizedEndTransitionTick, originalVolume, 0.0f);
                soundInstance.pitch = MathHelper.lerp(normalizedEndTransitionTick, originalPitch, originalPitch - 0.2f);
            }
        }
    }

    protected static void transformSoundForEngineLoad(float originalVolume, float originalPitch, BoatismSoundInstance soundInstance) {
        BoatEngineHandler boatEngineHandler = soundInstance.engineHandler;
        float normalizedPowerLevel = boatEngineHandler.getPowerLevel() * 0.1f;

        soundInstance.volume = MathHelper.lerp(normalizedPowerLevel, originalVolume - 0.1f, originalVolume);
        soundInstance.pitch = MathHelper.lerp(normalizedPowerLevel, originalPitch - 0.2f, originalPitch + 0.1f);
    }

    public void finishSoundInstance() {
        this.transitionState = TransitionState.FINISHING;
    }

    public boolean isFinished() {
        return this.isFinished;
    }

    public BoatEngineEntity getBoatEngineEntity() {
        return this.boatEngineEntity;
    }

    protected enum TransitionState {
        STARTING, FINISHING, IDLE;
    }
}
