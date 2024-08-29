package net.mortimer_kerman.defense.mixin;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.GameRuleCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;

import net.mortimer_kerman.defense.Gamerules;
import net.mortimer_kerman.defense.Payloads;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRuleCommand.class)
public abstract class GameruleMixin
{
    @Inject(at = @At("HEAD"), method = "executeSet")
    private static <T extends GameRules.Rule<T>> void sendGamerule(CommandContext<ServerCommandSource> context, GameRules.Key<T> key, CallbackInfoReturnable<Integer> cir)
    {
        MinecraftServer server = context.getSource().getServer();

        Gamerules.Type type;

        String gameruleName = key.getName();

        if (gameruleName.equals(Gamerules.DEFENSE_DURATION_MINUTES.getName())) type = Gamerules.Type.INTEGER;
        else if (gameruleName.equals(Gamerules.ALLOW_DEFENSE_KEYBIND.getName())) type = Gamerules.Type.BOOLEAN;
        else return;

        switch (type)
        {
            case BOOLEAN ->
            {
                boolean value = BoolArgumentType.getBool(context, "value");

                for(ServerPlayerEntity player : server.getPlayerManager().getPlayerList())
                {
                    server.execute(() -> ServerPlayNetworking.send(player, new Payloads.GamerulePayloads.Boolean(gameruleName, value)));
                }
            }
            case DOUBLE ->
            {
                double value = DoubleArgumentType.getDouble(context, "value");

                for(ServerPlayerEntity player : server.getPlayerManager().getPlayerList())
                {
                    server.execute(() -> ServerPlayNetworking.send(player, new Payloads.GamerulePayloads.Double(gameruleName, value)));
                }
            }
            case INTEGER ->
            {
                int value = IntegerArgumentType.getInteger(context, "value");

                for(ServerPlayerEntity player : server.getPlayerManager().getPlayerList())
                {
                    server.execute(() -> ServerPlayNetworking.send(player, new Payloads.GamerulePayloads.Integer(gameruleName, value)));
                }
            }
        }
    }
}

