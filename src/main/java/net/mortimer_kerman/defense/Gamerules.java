package net.mortimer_kerman.defense;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.world.rule.GameRule;
import net.minecraft.world.rule.GameRuleCategory;

public class Gamerules
{
    public static final GameRule<Boolean> PETS_PROTECTED = GameRuleBuilder
            .forBoolean(true)
            .category(GameRuleCategory.MOBS)
            .buildAndRegister(Defense.idOf("pets_protected"));

    public static final GameRule<Boolean> MOUNTS_PROTECTED = GameRuleBuilder
            .forBoolean(true)
            .category(GameRuleCategory.MOBS)
            .buildAndRegister(Defense.idOf("mounts_protected"));

    public static final GameRule<Integer> AFK_TIMER_SECONDS = GameRuleBuilder
            .forInteger(60)
            .minValue(0)
            .category(GameRuleCategory.PLAYER)
            .buildAndRegister(Defense.idOf("afk_timer_seconds"));

    public static final GameRule<Integer> DEFENSE_DURATION_MINUTES = GameRuleBuilder
            .forInteger(20)
            .minValue(0)
            .category(GameRuleCategory.PLAYER)
            .buildAndRegister(Defense.idOf("defense_duration_minutes"));

    public static final GameRule<Boolean> ALLOW_DEFENSE_KEYBIND = GameRuleBuilder
            .forBoolean(false)
            .category(GameRuleCategory.PLAYER)
            .buildAndRegister(Defense.idOf("allow_defense_keybind"));

    public enum Type
    {
        BOOLEAN,
        DOUBLE,
        INTEGER;
    }

    public static void RegisterGamerules() { /*yes it is empty, yes it is normal*/ }
}
