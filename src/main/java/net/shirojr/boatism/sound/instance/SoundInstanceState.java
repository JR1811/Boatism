package net.shirojr.boatism.sound.instance;

import net.shirojr.boatism.util.SoundInstanceHelper;

import java.util.List;

/**
 * Check out {@linkplain net.shirojr.boatism.sound.SoundManager SoundManager} for more information.
 */
public interface SoundInstanceState {
    /**
     * Main sounds will stop other main sounds, when started.<br>
     * Other sound won't cancel other main sound by default.
     */
    boolean isMainSound();

    /**
     * Manually specify, which other SoundInstances should be stopped, when activated
     * @return List of unsupported SoundInstanceHelper objects
     */
    default List<SoundInstanceHelper> unsupportedInstances() {
        return List.of();
    }
}
