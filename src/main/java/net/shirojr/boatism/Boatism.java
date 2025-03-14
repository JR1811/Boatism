package net.shirojr.boatism;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import net.shirojr.boatism.config.BoatismConfig;
import net.shirojr.boatism.init.*;
import net.shirojr.boatism.network.BoatismC2S;
import net.shirojr.boatism.network.BoatismNetworkPayloads;
import net.shirojr.boatism.util.LoggerUtil;
import net.shirojr.boatism.util.tag.BoatismTags;

public class Boatism implements ModInitializer {
    public static String MODID = "boatism";
    public static BoatismConfig CONFIG = new BoatismConfig();

    @Override
    public void onInitialize() {
        initConfig();
        BoatismItems.initialize();
        BoatismDataComponents.initialize();
        BoatismBlocks.initialize();
        BoatismEntities.initialize();
        BoatismFluids.initialize();
        BoatismStorage.initialize();
        BoatismItemGroups.initialize();
        BoatismEntityAttributes.initialize();
        BoatismEvents.registerCommonEvents();
        BoatismSounds.initialize();
        BoatismNetworkPayloads.initialize();
        BoatismC2S.registerServerReceivers();
        BoatismTags.initialize();
        BoatismGameRules.initialize();
        BoatismScreenHandlers.initialize();

        LoggerUtil.LOGGER.info("Spread the Boatism!");
    }

    public static Identifier getId(String path) {
        return Identifier.of(MODID, path);
    }

    private static void initConfig() {
        AutoConfig.register(BoatismConfig.class, JanksonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(BoatismConfig.class).getConfig();
    }
}