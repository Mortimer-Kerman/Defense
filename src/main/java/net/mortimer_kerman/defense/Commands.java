package net.mortimer_kerman.defense;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

import net.mortimer_kerman.defense.argument.DefenseAction;
import net.mortimer_kerman.defense.argument.DefenseArgumentType;

import java.util.Collection;
import java.util.Collections;

public class Commands
{
    public static void DefenseCommand(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(LiteralArgumentBuilder.<CommandSourceStack>literal("defense")
                .then(
                        net.minecraft.commands.Commands.argument("action", DefenseArgumentType.defenseAction())
                                .executes(ctx -> setDefense(ctx.getSource(), Collections.singleton((ctx.getSource()).getPlayerOrException()), DefenseArgumentType.getDefenseAction(ctx, "action")))
                )
                .then(
                        net.minecraft.commands.Commands.argument("targets", EntityArgument.players())
                                .requires(s -> s.permissions().hasPermission(Permissions.COMMANDS_MODERATOR))
                                .then(
                                        net.minecraft.commands.Commands.argument("action", DefenseArgumentType.defenseAction())
                                                .executes(ctx -> setDefense(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), DefenseArgumentType.getDefenseAction(ctx, "action")))
                                )

                )
        );
    }

    private static int setDefense(CommandSourceStack src, Collection<? extends ServerPlayer> targets, DefenseAction action)
    {
        for (ServerPlayer player : targets)
        {
            src.getServer().execute(() -> ServerPlayNetworking.send(player, new Payloads.ForceDefensePayload(action.tag)));
        }

        if (action != DefenseAction.OFF)
        {
            if (targets.size() == 1) src.sendSuccess(() -> Component.translatable("commands.defense.enable.single", targets.iterator().next().getDisplayName()), true);
            else src.sendSuccess(() -> Component.translatable("commands.defense.enable.multiple", targets.size()), true);
        }
        else
        {
            if (targets.size() == 1) src.sendSuccess(() -> Component.translatable("commands.defense.disable.single", targets.iterator().next().getDisplayName()), true);
            else src.sendSuccess(() -> Component.translatable("commands.defense.disable.multiple", targets.size()), true);
        }

        return targets.size();
    }

    public static void AfkCommand(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(LiteralArgumentBuilder.<CommandSourceStack>literal("afk")
                .executes(ctx -> setAfk(ctx.getSource(), ctx.getSource().getPlayerOrException()))
        );
    }

    private static int setAfk(CommandSourceStack src, ServerPlayer player)
    {
        MinecraftServer server = src.getServer();

        server.execute(() ->
        {
            ServerPlayNetworking.send(player, new Payloads.EnableAfkPayload());
            Defense.setPlayerAfk(player);
        });

        return 1;
    }
}
