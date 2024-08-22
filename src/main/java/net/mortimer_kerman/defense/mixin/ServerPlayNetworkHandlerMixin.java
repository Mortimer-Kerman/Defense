package net.mortimer_kerman.defense.mixin;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.listener.TickablePacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.*;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.mortimer_kerman.defense.Defense;
import net.mortimer_kerman.defense.Payloads;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin extends ServerCommonNetworkHandler implements ServerPlayPacketListener, PlayerAssociatedNetworkHandler, TickablePacketListener
{
    @Unique private static final long afkTimerSeconds = 60L;

    @Shadow public ServerPlayerEntity player;

    @Inject(method = "tick", at = @At(value = "TAIL"))
    private void onTick(CallbackInfo ci)
    {
        if (server.getPlayerManager().getCurrentPlayerCount() > 1 && player.getLastActionTime() > 0L && Util.getMeasuringTimeMs() - player.getLastActionTime() > (afkTimerSeconds * 1000L) && !Defense.isPlayerAfk(player))
        {
            ServerPlayNetworking.send(player, new Payloads.EnableAfkPayload());
            Defense.setPlayerAfk(player);
        }
    }

    public ServerPlayNetworkHandlerMixin(MinecraftServer server, ClientConnection connection, ConnectedClientData clientData) { super(server, connection, clientData); }
}
