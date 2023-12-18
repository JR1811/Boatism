package net.shirojr.boatism.util;

import net.fabricmc.loader.api.FabricLoader;
import net.shirojr.boatism.Boatism;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerUtil {
    public static final Logger LOGGER = LoggerFactory.getLogger(Boatism.MODID);

    /**
     * Utility class for clean LOGGER handling
     */
    private LoggerUtil() {
    }

    /**
     * Method to display useful information in the console at runtime.<br>
     * The information will only be printed, if the instance is running in a developer environment.<br><br>
     * <i>Override of the {@link #devLogger(String, boolean, Exception) devLogger} method.</i>
     *
     * @param input Informative text of the current state of the mod, to display in the game console
     */
    public static void devLogger(String input) {
        devLogger(input, false, null);
    }

    /**
     * Uses LOGGER only when the instance has been started in a development environment.<br>
     * In addition, this method can print information or error values
     *
     * @param input     Informative text of the current state of the mod, to display in the game console
     * @param isError   Will display texts differently in the instance's console
     * @param exception If not available, pass over a <b><i>null</i></b> value
     */
    public static void devLogger(String input, boolean isError, Exception exception) {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment()) return;
        String printText = "DEV - [ " + input + " ]";
        if (!isError) {
            LOGGER.info(printText);
            return;
        }
        if (exception == null) {
            LOGGER.error(printText);
            return;
        }
        LOGGER.error(printText, exception);
    }
}
