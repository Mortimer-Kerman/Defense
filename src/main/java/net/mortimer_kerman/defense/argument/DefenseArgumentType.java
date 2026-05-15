package net.mortimer_kerman.defense.argument;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.StringRepresentableArgument;

public class DefenseArgumentType extends StringRepresentableArgument<DefenseAction>
{
    private DefenseArgumentType() { super(DefenseAction.CODEC, DefenseAction::values); }

    public static StringRepresentableArgument<DefenseAction> defenseAction() { return new DefenseArgumentType(); }

    public static DefenseAction getDefenseAction(CommandContext<CommandSourceStack> context, String id) {
        return context.getArgument(id, DefenseAction.class);
    }
}