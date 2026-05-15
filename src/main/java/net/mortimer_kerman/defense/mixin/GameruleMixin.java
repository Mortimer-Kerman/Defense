package net.mortimer_kerman.defense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.GameRuleCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.gamerules.GameRule;

import net.mortimer_kerman.defense.Gamerules;
import net.mortimer_kerman.defense.Payloads;

@Mixin(GameRuleCommand.class)
public abstract class GameruleMixin
{
    @Inject(at = @At("HEAD"), method = "setRule")
    private static <T extends GameRule<T>> void sendGamerule(CommandContext<CommandSourceStack> context, GameRule<T> key, CallbackInfoReturnable<Integer> cir)
    {
        MinecraftServer server = context.getSource().getServer();

        Gamerules.Type type;

        Identifier gameruleId = key.getIdentifier();

        if (gameruleId.equals(Gamerules.DEFENSE_DURATION_MINUTES.getIdentifier())) type = Gamerules.Type.INTEGER;
        else if (gameruleId.equals(Gamerules.ALLOW_DEFENSE_KEYBIND.getIdentifier())) type = Gamerules.Type.BOOLEAN;
        else return;

        switch (type)
        {
            case BOOLEAN ->
            {
                boolean value = BoolArgumentType.getBool(context, "value");

                for(ServerPlayer player : server.getPlayerList().getPlayers())
                {
                    server.execute(() -> ServerPlayNetworking.send(player, new Payloads.GamerulePayloads.Boolean(gameruleId, value)));
                }
            }
            case DOUBLE ->
            {
                double value = DoubleArgumentType.getDouble(context, "value");

                for(ServerPlayer player : server.getPlayerList().getPlayers())
                {
                    server.execute(() -> ServerPlayNetworking.send(player, new Payloads.GamerulePayloads.Double(gameruleId, value)));
                }
            }
            case INTEGER ->
            {
                int value = IntegerArgumentType.getInteger(context, "value");

                for(ServerPlayer player : server.getPlayerList().getPlayers())
                {
                    server.execute(() -> ServerPlayNetworking.send(player, new Payloads.GamerulePayloads.Integer(gameruleId, value)));
                }
            }
        }
    }
}

