package net.shirojr.boatism.gamerule;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;

public class BoatismGameRules {

    public static final GameRules.Key<GameRules.BooleanRule> DESTRUCTIVE_ENGINE_EXPLOSION =
            GameRuleRegistry.register("engineExplosionDestruction", GameRules.Category.MISC,
                    GameRuleFactory.createBooleanRule(true));

    public static void initialize() {
    }
}
