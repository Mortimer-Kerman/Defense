package net.mortimer_kerman.defense;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class Defense implements ModInitializer
{
	public static final String MOD_ID = "defense";
	public static final String MOD_VERSION = "1.21-0.2.0";
	public static final int VERSION_ID = 0;

	private static final HashSet<UUID> immunePlayers = new HashSet<>();
	private static final HashMap<UUID, Integer> playerIcons = new HashMap<>();

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize()
	{
		Payloads.RegisterPayloads();

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
		{
			ServerPlayerEntity player = handler.getPlayer();
			server.execute(() ->
			{
				for (UUID playerUUID : immunePlayers) ServerPlayNetworking.send(player, new Payloads.NotifyPVPPayload(playerUUID, true));
				playerIcons.forEach((UUID playerUUID, Integer iconID) -> ServerPlayNetworking.send(player, new Payloads.NotifyIconPayload(playerUUID, iconID)));
			});
		});

		ServerPlayConnectionEvents.DISCONNECT.register(((handler, server) ->
		{
			UUID playerUUID = handler.getPlayer().getUuid();

			if(immunePlayers.contains(playerUUID))
			{
				immunePlayers.remove(playerUUID);
				server.execute(() ->
				{
					for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList())
					{
						ServerPlayNetworking.send(player, new Payloads.NotifyPVPPayload(playerUUID, false));
					}
				});
			}
		}));

		ServerPlayNetworking.registerGlobalReceiver(Payloads.RecordPVPPayload.ID, (payload, context) ->
		{
			MinecraftServer server = context.server();
			ServerPlayerEntity sender = context.player();

			UUID playerUUID = sender.getUuid();
			boolean pvpOff = payload.pvpOff();

			if (pvpOff) immunePlayers.add(playerUUID);
			else immunePlayers.remove(playerUUID);

			server.execute(() ->
			{
				for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList())
				{
					if (!sender.equals(player)) ServerPlayNetworking.send(player, new Payloads.NotifyPVPPayload(playerUUID, pvpOff));
				}
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(Payloads.RecordIconPayload.ID, (payload, context) ->
		{
			MinecraftServer server = context.server();
			ServerPlayerEntity sender = context.player();

			UUID playerUUID = sender.getUuid();
			int iconID = payload.iconID();

			playerIcons.put(playerUUID, iconID);

			server.execute(() ->
			{
				for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList())
				{
					if (!sender.equals(player)) ServerPlayNetworking.send(player, new Payloads.NotifyIconPayload(playerUUID, iconID));
				}
			});
		});

		ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) ->
		{
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer(5));
			buf.writeVarInt(VERSION_ID);
			sender.sendPacket(Payloads.handshakeID, buf);
		});

		ServerLoginNetworking.registerGlobalReceiver(Payloads.handshakeID, (server, handler, understood, buf, synchronizer, sender) ->
		{
			if(!understood)
			{
				handler.disconnect(getServerErrorMessage(ErrorReason.MOD_NOT_INSTALLED));
			}
		});
	}

	public static boolean isPlayerImmune(PlayerEntity player)
	{
		return immunePlayers.contains(player.getUuid());
	}

	public static Text getServerErrorMessage(ErrorReason reason)
	{
		String desc = switch (reason)
		{
            case MOD_NOT_INSTALLED -> "The Defense mod is not installed!";
            case CLIENT_OLDER -> "You have an outdated version of the Defense mod!";
            case CLIENT_NEWER -> "You have a too recent version of the Defense mod!";
        };
		Text version = Text.literal(MOD_VERSION).styled(style -> style.withUnderline(true));
		Text url = Text.literal("https://modrinth.com/mod/defense").styled((style -> style.withColor(Formatting.GREEN)));
		return Text.translatable(desc + " Please download the version %s from %s.", version, url);
	}

	public enum ErrorReason
	{
		MOD_NOT_INSTALLED,
		CLIENT_OLDER,
		CLIENT_NEWER
	}
}