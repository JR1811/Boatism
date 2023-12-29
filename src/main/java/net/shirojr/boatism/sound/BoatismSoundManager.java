package net.shirojr.boatism.sound;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.util.Identifier;
import net.shirojr.boatism.sound.instance.SoundInstanceState;
import net.shirojr.boatism.sound.instance.custom.EngineOverheatingSoundInstance;
import net.shirojr.boatism.util.SoundInstanceIdentifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoatismSoundManager {
    private final MinecraftClient client = MinecraftClient.getInstance();
    private final Map<SoundInstanceIdentifier, SoundInstance> activeSoundInstances = new HashMap<>();

    public Map<SoundInstanceIdentifier, SoundInstance> getActiveSoundInstances() {
        return this.activeSoundInstances;
    }

    /**
     * Adds new SoundInstance to active sound instances.<br><br>
     * <h3>Excluded SoundInstance handling</h3><br>
     * This will only apply, if the new SoundInstance makes use of the {@linkplain SoundInstanceState} Interface.
     * New entries always have priority over already
     * existing entries. If entries have the same {@linkplain SoundInstanceIdentifier} the entry, which is already
     * in the list, will be removed.<br>
     * <h4>Criteria:</h4>
     * <ul>
     *     <li>if new SoundInstance is a main sound, it will stop all other currently running main sounds</li>
     *     <li>if new SoundInstance is manually excluding other sounds, those will be stopped as well</li>
     * </ul>
     *
     * @param soundInstanceIdentifier The identifying enum of the SoundInstance
     * @param soundInstance       the actual object, which will be passed to the client BoatismSoundManager
     */
    public void start(SoundInstanceIdentifier soundInstanceIdentifier, SoundInstance soundInstance) {
        if (!(soundInstance instanceof SoundInstanceState state)) return;
        List<SoundInstanceIdentifier> unsupportedSoundInstances = new ArrayList<>();

        for (var activeInstance : this.activeSoundInstances.entrySet()) {
            if (!(activeInstance.getValue() instanceof SoundInstanceState activeInstanceState)) continue;
            if (state.isMainSound() && activeInstanceState.isMainSound())
                unsupportedSoundInstances.add(activeInstance.getKey());
            for (SoundInstanceIdentifier unsupportedInstance : state.unsupportedInstances()) {
                if (activeInstance.getKey().equals(unsupportedInstance))
                    unsupportedSoundInstances.add(activeInstance.getKey());
            }
        }
        unsupportedSoundInstances.forEach(this::stop);

        this.activeSoundInstances.put(soundInstanceIdentifier, soundInstance);
        this.client.getSoundManager().play(soundInstance);
    }

    public void stop(SoundInstanceIdentifier boatismIdentifier) {
        Identifier identifier = this.activeSoundInstances.get(boatismIdentifier).getId();
        this.client.getSoundManager().stopSounds(identifier, boatismIdentifier.getCategory());
        removeEntriesFromList(List.of(boatismIdentifier));
    }

    public void stopAllBoatismSoundInstances() {
        for (var entry : this.activeSoundInstances.entrySet()) {
            client.getSoundManager().stop(entry.getValue());
        }
        this.activeSoundInstances.clear();
    }

    private void removeEntriesFromList(List<SoundInstanceIdentifier> unsupportedSoundInstances) {
        for (var entry : unsupportedSoundInstances) {
            this.activeSoundInstances.remove(entry);
        }
    }
}
