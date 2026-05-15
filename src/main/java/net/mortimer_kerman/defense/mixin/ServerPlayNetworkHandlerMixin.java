package net.mortimer_kerman.defense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.network.Connection;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.util.Util;

import net.mortimer_kerman.defense.Defense;
import net.mortimer_kerman.defense.Gamerules;
import net.mortimer_kerman.defense.Payloads;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerPlayNetworkHandlerMixin extends ServerCommonPacketListenerImpl implements ServerGamePacketListener, ServerPlayerConnection, TickablePacketListener
{
    @Shadow public ServerPlayer player;

    @Inject(method = "tick", at = @At(value = "TAIL"))
    private void onTick(CallbackInfo ci)
    {
        int afkTimer = server.overworld().getGameRules().get(Gamerules.AFK_TIMER_SECONDS);
        if (afkTimer == 0) return;

        if(player.getLastActionTime() > 0L && Util.getMillis() - player.getLastActionTime() > (afkTimer * 1000L))
        {
            if (server.getPlayerList().getPlayerCount() > 1 && !Defense.isPlayerAfk(player))
            {
                ServerPlayNetworking.send(player, new Payloads.EnableAfkPayload());
                Defense.setPlayerAfk(player);
            }
        }
    }

    public ServerPlayNetworkHandlerMixin(MinecraftServer server, Connection connection, CommonListenerCookie clientData) { super(server, connection, clientData); }
}
