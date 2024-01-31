package net.shirojr.boatism.sound;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.shirojr.boatism.BoatismClient;
import net.shirojr.boatism.entity.custom.BoatEngineEntity;
import net.shirojr.boatism.sound.instance.SoundInstanceState;
import net.shirojr.boatism.sound.instance.custom.BoatismSoundInstance;
import net.shirojr.boatism.util.LoggerUtil;
import net.shirojr.boatism.util.sound.SoundInstanceIdentifier;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class BoatismSoundManager {
    private final MinecraftClient client = MinecraftClient.getInstance();
    private final List<SoundInstanceEntry> activeSoundInstances = new ArrayList<>();

    public List<SoundInstanceEntry> getActiveSoundInstances() {
        return this.activeSoundInstances;
    }

    /**
     * Adds new SoundInstance to active sound instances.<br><br>
     * <h2>Excluded SoundInstance handling</h2>
     * This will only apply, if the new SoundInstance makes use of the {@linkplain SoundInstanceState} Interface.
     * New entries always have priority over already
     * existing entries. If entries have the same {@linkplain SoundInstanceIdentifier} the entry, which is already
     * in the list, will be removed.<br><br>
     * <h2>Criteria</h2>
     * <ul>
     *     <li>if new SoundInstance is a main sound, it will stop all other currently running main sounds</li>
     *     <li>if new SoundInstance is manually excluding other sounds, those will be stopped as well</li>
     * </ul>
     *
     * @param soundInstanceIdentifier The identifying enum of the SoundInstance
     * @param soundInstance           the actual object, which will be passed to the client BoatismSoundManager
     */
    public void start(SoundInstanceIdentifier soundInstanceIdentifier, BoatismSoundInstance soundInstance) {
        if (!(soundInstance instanceof SoundInstanceState newInstanceState)) return;
        List<SoundInstanceEntry> unsupportedSoundInstances = new ArrayList<>();
        for (SoundInstanceEntry activeInstance : this.activeSoundInstances) {
            LoggerUtil.devLogger(activeInstance.instance.toString());
            if (!(activeInstance.instance instanceof SoundInstanceState activeInstanceState)) continue;
            if (soundInstance.getBoatEngineEntity().equals(activeInstance.instance.getBoatEngineEntity())) {
                if (newInstanceState.isMainSound() && activeInstanceState.isMainSound()) {
                    unsupportedSoundInstances.add(activeInstance);
                }
                if (soundInstance.equals(activeInstance.instance)) {
                    unsupportedSoundInstances.add(activeInstance);
                }
                for (SoundInstanceIdentifier unsupportedInstance : newInstanceState.unsupportedInstances()) {
                    if (activeInstance.identifier.equals(unsupportedInstance))
                        unsupportedSoundInstances.add(activeInstance);
                }
            }
            if (activeInstance.instance.isFinished()) {
                unsupportedSoundInstances.add(activeInstance);
            }
        }
        unsupportedSoundInstances.forEach(this::stop);

        this.activeSoundInstances.add(new SoundInstanceEntry(soundInstanceIdentifier, soundInstance));
        this.client.getSoundManager().play(soundInstance);
        List<SoundInstanceEntry> soundInstances = BoatismClient.soundManager.getActiveSoundInstances();
        for (var entry : soundInstances) {
            if (!entry.identifier.equals(SoundInstanceIdentifier.ENGINE_OVERHEATING)) continue;
            LoggerUtil.devLogger(entry.instance().getBoatEngineEntity() + " | " + entry.identifier().toString());
        }
    }

    public void stop(SoundInstanceEntry soundInstanceEntry) {
        for (SoundInstanceEntry entry : this.activeSoundInstances) {
            if (soundInstanceEntry.identifier != entry.identifier) continue;
            soundInstanceEntry.instance.finishSoundInstance();
        }
        removeEntriesFromList(List.of(soundInstanceEntry));
    }

    public void stopAllSoundInstancesForBoatEngineEntity(BoatEngineEntity boatEngine, boolean force) {
        for (var entry : this.activeSoundInstances) {
            if (!entry.instance.getBoatEngineEntity().equals(boatEngine)) continue;
            if (!(entry.instance instanceof SoundInstanceState activeInstanceState)) continue;
            if (!force && activeInstanceState.canRunIfEngineIsTurnedOff(boatEngine)) continue;
            client.getSoundManager().stop(entry.instance);
        }
        this.activeSoundInstances.clear();
    }

    public void stopAllSoundInstances(boolean sendInformationText) {
        for (var entry : this.activeSoundInstances) {
            client.getSoundManager().stop(entry.instance);
            if (client.player != null && sendInformationText) {
                client.player.sendMessage(Text.literal("removed %s for: %s"
                        .formatted(entry.identifier, entry.instance.getBoatEngineEntity().toString())));
            }
        }
        this.activeSoundInstances.clear();
    }

    private void removeEntriesFromList(List<SoundInstanceEntry> unsupportedSoundInstances) {
        for (var entry : unsupportedSoundInstances) {
            this.activeSoundInstances.remove(entry);
        }
    }

    /**
     * @param identifier identifier for the sound instance (used e.g. for networking)
     * @param instance   actual sound instance
     */
    public record SoundInstanceEntry(SoundInstanceIdentifier identifier, BoatismSoundInstance instance) {
    }
}
