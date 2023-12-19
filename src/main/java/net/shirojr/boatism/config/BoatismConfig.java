package net.shirojr.boatism.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import net.shirojr.boatism.config.data.BoatData;

@Config(name = "boatism")
public class BoatismConfig implements ConfigData {
    public BoatData boatValues = new BoatData(4.0f);
}
