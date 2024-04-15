package net.shirojr.boatism;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.shirojr.boatism.config.BoatismConfig;
import net.shirojr.boatism.entity.BoatismEntities;
import net.shirojr.boatism.entity.BoatismEntityAttributes;
import net.shirojr.boatism.event.BoatismEvents;
import net.shirojr.boatism.gamerule.BoatismGameRules;
import net.shirojr.boatism.item.BoatismItems;
import net.shirojr.boatism.network.BoatismC2S;
import net.shirojr.boatism.screen.handler.BoatismScreenHandlers;
import net.shirojr.boatism.sound.BoatismSounds;
import net.shirojr.boatism.util.LoggerUtil;
import net.shirojr.boatism.util.tag.BoatismTags;

public class Boatism implements ModInitializer {
    public static String MODID = "boatism";
    public static BoatismConfig CONFIG = new BoatismConfig();

    @Override
    public void onInitialize() {
        initConfig();
        BoatismItems.initialize();
        BoatismEntities.initialize();
        BoatismEntityAttributes.initialize();
        BoatismEvents.registerCommonEvents();
        BoatismSounds.initializeSounds();
        BoatismC2S.registerServerReceivers();
        BoatismTags.initialize();
        BoatismGameRules.initialize();
        BoatismScreenHandlers.initialize();

        LoggerUtil.LOGGER.info("Spread the Boatism!");
    }

    private static void initConfig() {
        AutoConfig.register(BoatismConfig.class, JanksonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(BoatismConfig.class).getConfig();
    }
}