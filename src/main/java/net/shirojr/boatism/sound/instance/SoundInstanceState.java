package net.shirojr.boatism.sound.instance;

import net.shirojr.boatism.util.SoundInstanceHelper;

import java.util.List;

public interface SoundInstanceState {
    /**
     * Main sounds will stop other main sounds, when started.<br>
     * Other sound won't cancel other main sound by default.
     */
    boolean isMainSound();

    default List<SoundInstanceHelper> unsupportedSoundLines() {
        return List.of();
    }
}
