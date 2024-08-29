package net.mortimer_kerman.defense;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;

public class Gamerules
{
    public static final GameRules.Key<GameRules.BooleanRule> PETS_PROTECTED = GameRuleRegistry.register("petsProtected", GameRules.Category.MOBS, GameRuleFactory.createBooleanRule(true));
    public static final GameRules.Key<GameRules.BooleanRule> MOUNTS_PROTECTED = GameRuleRegistry.register("mountsProtected", GameRules.Category.MOBS, GameRuleFactory.createBooleanRule(true));
    public static final GameRules.Key<GameRules.IntRule> AFK_TIMER_SECONDS = GameRuleRegistry.register("afkTimerSeconds", GameRules.Category.PLAYER, GameRuleFactory.createIntRule(60, 0));
    public static final GameRules.Key<GameRules.IntRule> DEFENSE_DURATION_MINUTES = GameRuleRegistry.register("defenseDurationMinutes", GameRules.Category.PLAYER, GameRuleFactory.createIntRule(20, 0));
    public static final GameRules.Key<GameRules.BooleanRule> ALLOW_DEFENSE_KEYBIND = GameRuleRegistry.register("allowDefenseKeybind", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(false));


    public enum Type
    {
        BOOLEAN,
        DOUBLE,
        INTEGER;
    }

    public static void RegisterGamerules() { /*yes it is empty, yes it is normal*/ }
}
