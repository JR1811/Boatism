package net.shirojr.boatism.sound;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.util.Identifier;
import net.shirojr.boatism.sound.instance.SoundInstanceState;
import net.shirojr.boatism.util.SoundInstanceHelper;

import java.util.*;

public class SoundManager {
    private final MinecraftClient client = MinecraftClient.getInstance();
    private Map<SoundInstanceHelper, SoundInstance> activeSoundInstances = new HashMap<>();

    /**
     * Adds new SoundInstance to active sound instances.<br><br>
     * <h3>Excluded SoundInstance handling</h3><br>
     * This will only apply, if the new SoundInstance makes use of the {@linkplain SoundInstanceState} Interface.
     * New entries always have priority over already
     * existing entries. If entries have the same {@linkplain SoundInstanceHelper} the entry, which is already
     * in the list, will be removed.<br>
     * <h4>Criteria:</h4>
     * <ul>
     *     <li>if new SoundInstance is a main sound, it will stop all other currently running main sounds</li>
     *     <li>if new SoundInstance is manually excluding other sounds, those will be stopped as well</li>
     * </ul>
     * @param soundInstanceHelper The identifying enum of the SoundInstance
     * @param soundInstance the actual object, which will be passed to the client SoundManager
     */
    public void start(SoundInstanceHelper soundInstanceHelper, SoundInstance soundInstance) {
        this.activeSoundInstances.put(soundInstanceHelper, soundInstance);
        if (!(soundInstance instanceof SoundInstanceState state)) return;

        for (var activeInstance : activeSoundInstances.entrySet()) {
            if (!(activeInstance.getValue() instanceof SoundInstanceState activeInstanceState)) continue;
            if (state.isMainSound() && activeInstanceState.isMainSound()) this.stop(activeInstance.getKey());
            for (SoundInstanceHelper unsupportedInstance : state.unsupportedInstances()) {
                if (activeInstance.getKey().equals(unsupportedInstance)) this.stop(unsupportedInstance);
            }
        }
    }

    public void stop(SoundInstanceHelper soundInstanceHelper) {
        Identifier soundInstanceIdentifier = this.activeSoundInstances.get(soundInstanceHelper).getId();
        this.client.getSoundManager().stopSounds(soundInstanceIdentifier, soundInstanceHelper.getCategory());
        this.activeSoundInstances.remove(soundInstanceHelper);
    }
}
