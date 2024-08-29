package net.mortimer_kerman.defense.argument;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.server.command.ServerCommandSource;

public class DefenseArgumentType extends EnumArgumentType<DefenseAction>
{
    private DefenseArgumentType() { super(DefenseAction.CODEC, DefenseAction::values); }

    public static EnumArgumentType<DefenseAction> defenseAction() { return new DefenseArgumentType(); }

    public static DefenseAction getDefenseAction(CommandContext<ServerCommandSource> context, String id) {
        return context.getArgument(id, DefenseAction.class);
    }
}