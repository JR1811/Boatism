package net.shirojr.boatism.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = "boatism")
@Config.Gui.Background("minecraft:textures/block/stone.png")
public class BoatismConfig implements ConfigData {
    @ConfigEntry.Category("general_engine_data")
    @ConfigEntry.Gui.RequiresRestart
    @Comment("General Health value")
    public float health = 40.0f;
    @ConfigEntry.Category("general_engine_data")
    @Comment("Indication of low health")
    public float lowHealth = 4.0f;
    @ConfigEntry.Category("general_engine_data")
    @Comment("Base fuel capacity")
    public int maxFuel = 500;
    @ConfigEntry.Category("general_engine_data")
    @Comment("Base overheat mitigation")
    public int maxOverheat = 300;
}
