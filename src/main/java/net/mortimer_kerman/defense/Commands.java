package net.mortimer_kerman.defense;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.WeatherCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.mortimer_kerman.defense.argument.DefenseAction;
import net.mortimer_kerman.defense.argument.DefenseArgumentType;

import java.util.Collection;
import java.util.Collections;

public class Commands
{
    public static void DefenseCommand(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>literal("defense")
                .then(
                        CommandManager.argument("action", DefenseArgumentType.defenseAction())
                                .executes(ctx -> setDefense(ctx.getSource(), Collections.singleton((ctx.getSource()).getPlayerOrThrow()), DefenseArgumentType.getDefenseAction(ctx, "action")))
                )
                .then(
                        CommandManager.argument("targets", EntityArgumentType.players())
                                .requires(s -> s.hasPermissionLevel(2))
                                .then(
                                        CommandManager.argument("action", DefenseArgumentType.defenseAction())
                                                .executes(ctx -> setDefense(ctx.getSource(), EntityArgumentType.getPlayers(ctx, "targets"), DefenseArgumentType.getDefenseAction(ctx, "action")))
                                )

                )
        );
    }

    private static int setDefense(ServerCommandSource src, Collection<? extends ServerPlayerEntity> targets, DefenseAction action)
    {
        for (ServerPlayerEntity player : targets)
        {
            player.getServer().execute(() -> ServerPlayNetworking.send(player, new Payloads.ForceDefensePayload(action.tag)));
        }

        if (action != DefenseAction.OFF)
        {
            if (targets.size() == 1) src.sendFeedback(() -> Text.translatable("commands.defense.enable.single", targets.iterator().next().getDisplayName()), true);
            else src.sendFeedback(() -> Text.translatable("commands.defense.enable.multiple", targets.size()), true);
        }
        else
        {
            if (targets.size() == 1) src.sendFeedback(() -> Text.translatable("commands.defense.disable.single", targets.iterator().next().getDisplayName()), true);
            else src.sendFeedback(() -> Text.translatable("commands.defense.disable.multiple", targets.size()), true);
        }

        return targets.size();
    }

    public static void AfkCommand(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>literal("afk")
                .executes(ctx -> setAfk(ctx.getSource(), ctx.getSource().getPlayerOrThrow()))
        );
    }

    private static int setAfk(ServerCommandSource src, ServerPlayerEntity player)
    {
        MinecraftServer server = player.getServer();

        if (server == null) return 0;

        player.getServer().execute(() ->
        {
            ServerPlayNetworking.send(player, new Payloads.EnableAfkPayload());
            Defense.setPlayerAfk(player);
        });

        return 1;
    }
}
