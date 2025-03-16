package net.shirojr.boatism.init;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;

public interface BoatismGameRules {

    GameRules.Key<GameRules.BooleanRule> DESTRUCTIVE_ENGINE_EXPLOSION =
            GameRuleRegistry.register("engineExplosionDestruction", GameRules.Category.MISC,
                    GameRuleFactory.createBooleanRule(true));

    static void initialize() {
        // static initialisation
    }
}
