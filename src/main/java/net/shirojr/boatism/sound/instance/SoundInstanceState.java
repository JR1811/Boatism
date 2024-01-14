package net.shirojr.boatism.sound.instance;

import net.shirojr.boatism.sound.BoatismSoundManager;
import net.shirojr.boatism.util.sound.SoundInstanceIdentifier;

import java.util.List;

/**
 * Check out {@linkplain BoatismSoundManager BoatismSoundManager} for more information.
 */
public interface SoundInstanceState {
    /**
     * Main sounds will stop other main sounds, when started.<br>
     * Other sound won't cancel other main sound by default.
     */
    boolean isMainSound();

    /**
     * Manually specify, which other SoundInstances should be stopped, when activated
     * @return List of unsupported SoundInstanceIdentifier objects
     */
    default List<SoundInstanceIdentifier> unsupportedInstances() {
        return List.of();
    }
}
