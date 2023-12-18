package net.shirojr.boatism;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;

import net.shirojr.boatism.config.BoatismConfig;
import net.shirojr.boatism.util.LoggerUtil;

public class Boatism implements ModInitializer {
	public static String MODID = "boatism";
	public static BoatismConfig CONFIG = new BoatismConfig();

	@Override
	public void onInitialize() {
		initConfig();

		LoggerUtil.devLogger("initialized common entrypoint");
		LoggerUtil.LOGGER.info("Spread the Boatism!");
	}

	private static void initConfig() {
		AutoConfig.register(BoatismConfig.class, JanksonConfigSerializer::new);
		CONFIG = AutoConfig.getConfigHolder(BoatismConfig.class).getConfig();
	}
}