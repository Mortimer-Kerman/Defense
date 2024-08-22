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

import net.mortimer_kerman.defense.GameruleType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRuleCommand.class)
public abstract class GameruleMixin
{
    /*@Inject(at = @At("HEAD"), method = "executeSet")
    private static <T extends GameRules.Rule<T>> void SetGravity(CommandContext<ServerCommandSource> context, GameRules.Key<T> key, CallbackInfoReturnable<Integer> cir)
    {
        String channel;
        MinecraftServer server = context.getSource().getServer();

        GameruleType target = GameruleType.BOOLEAN;

        if (key.getName().equals(Utility.MOVEMENT_GAMERULE.getName())) channel = Utility.MOVEMENT_ON;
        else if (key.getName().equals(Utility.AXIS_X_GAMERULE.getName())) channel = Utility.AXIS_X_ON;
        else if (key.getName().equals(Utility.AXIS_Y_GAMERULE.getName())) channel = Utility.AXIS_Y_ON;
        else if (key.getName().equals(Utility.CANJUMP_GAMERULE.getName())) channel = Utility.CANJUMP_ON;
        else if (key.getName().equals(Utility.PERSPECTIVE_LOCKED_GAMERULE.getName())) channel = Utility.PERSPECTIVE_LOCKED_ON;
        else if (key.getName().equals(Utility.PLAYER_STEP_GAMERULE.getName()))
        {
            channel = Utility.PLAYER_STEP_HEIGHT;
            target = GameruleType.DOUBLE;
        }
        else if (key.getName().equals(Utility.PLAYER_FALL_GAMERULE.getName())) channel = Utility.PLAYER_FALL;
        else if (key.getName().equals(Utility.CAMERA_CLIP_GAMERULE.getName())) channel = Utility.CAMERA_CLIP;
        else if (key.getName().equals(Utility.PLAYER_EYE_HEIGHT_GAMERULE.getName()))
        {
            channel = Utility.PLAYER_EYE_HEIGHT;
            target = GameruleType.DOUBLE;
        }
        else if (key.getName().equals(Utility.PRESSURE_JUMP_GAMERULE.getName())) channel = Utility.PRESSURE_JUMP;
        else if (key.getName().equals(Utility.PSJ_SPEED_GAMERULE.getName()))
        {
            float value = (float) DoubleArgumentType.getDouble(context, "value");
            float inertiaHeight = Utility.calculateInertiaHeight(value);

            for(ServerPlayerEntity player : server.getPlayerManager().getPlayerList())
            {
                server.execute(() -> ServerPlayNetworking.send(player, new Payloads.FloatCouplePayload(Utility.PSJ_SPEED, value, inertiaHeight)));
            }

            return;
        }
        else if (key.getName().equals(Utility.PSJ_HEIGHT_GAMERULE.getName()))
        {
            channel = Utility.PSJ_HEIGHT;
            target = GameruleType.DOUBLE;
        }
        else if (key.getName().equals(Utility.JUMPS_AMOUNT_GAMERULE.getName()))
        {
            channel = Utility.JUMPS_AMOUNT;
            target = GameruleType.INTEGER;
        }
        else return;

        switch (target)
        {
            case BOOLEAN ->
            {
                boolean value = BoolArgumentType.getBool(context, "value");

                for(ServerPlayerEntity player : server.getPlayerManager().getPlayerList())
                {
                    server.execute(() -> ServerPlayNetworking.send(player, new Payloads.BooleanPayload(channel, value)));
                }
            }
            case DOUBLE ->
            {
                float value = (float)DoubleArgumentType.getDouble(context, "value");

                for(ServerPlayerEntity player : server.getPlayerManager().getPlayerList())
                {
                    server.execute(() -> ServerPlayNetworking.send(player, new Payloads.FloatPayload(channel, value)));
                }
            }
            case INTEGER ->
            {
                int value = IntegerArgumentType.getInteger(context, "value");

                for(ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    server.execute(() -> ServerPlayNetworking.send(player, new Payloads.IntPayload(channel, value)));
                }
            }
        }
    }*/
}

