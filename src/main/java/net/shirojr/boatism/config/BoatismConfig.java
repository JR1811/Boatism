package net.shirojr.boatism.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.shirojr.boatism.config.custom.EngineHud;

@Config(name = "boatism")
@Config.Gui.Background("minecraft:textures/block/stone.png")
public class BoatismConfig implements ConfigData {
    @ConfigEntry.Category(("general"))
    @Comment("Use Scrolling in a boat to change Power Level")
    public boolean powerLevelScrolling = true;
    @ConfigEntry.Category("engine_data")
    @Comment("General Health value")
    @ConfigEntry.Gui.RequiresRestart
    public float health = 6.0f;
    @ConfigEntry.Category("engine_data")
    @Comment("Indication of low health")
    public float lowHealth = 3.0f;
    @ConfigEntry.Category("engine_data")
    @Comment("Base fuel capacity")
    public int maxBaseFuel = 1000;
    @ConfigEntry.Category("engine_data")
    @Comment("Overheat limit")
    public int maxBaseOverheat = 1000;
    @ConfigEntry.Gui.CollapsibleObject
    @ConfigEntry.Category("screen_and_hud")
    @Comment("Engine status display on Hud")
    public EngineHud engineHudOverlay = new EngineHud(false, 20, 15);
}
