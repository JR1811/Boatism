package net.shirojr.boatism.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = "boatism")
@Config.Gui.Background("minecraft:textures/block/stone.png")
public class BoatismConfig implements ConfigData {
    @ConfigEntry.Category("general_engine_data")
    @Comment("General Health value")
    @ConfigEntry.Gui.RequiresRestart
    public float health = 6.0f;
    @ConfigEntry.Category("general_engine_data")
    @Comment("Indication of low health")
    @ConfigEntry.Gui.RequiresRestart
    public float lowHealth = 3.0f;
    @ConfigEntry.Category("general_engine_data")
    @Comment("Base fuel capacity")
    @ConfigEntry.Gui.RequiresRestart
    public int maxBaseFuel = 1000;
    @ConfigEntry.Category("general_engine_data")
    @Comment("Overheat limit")
    @ConfigEntry.Gui.RequiresRestart
    public int maxBaseOverheat = 1000;
}
