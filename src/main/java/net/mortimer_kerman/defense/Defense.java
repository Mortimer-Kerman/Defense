package net.mortimer_kerman.defense;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class Defense implements ModInitializer
{
	public static final String MOD_ID = "defense";

	private static final HashSet<UUID> immunePlayers = new HashSet<>();
	private static final HashMap<UUID, Integer> playerIcons = new HashMap<>();

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
	}

	public static boolean isPlayerImmune(PlayerEntity player)
	{
		return immunePlayers.contains(player.getUuid());
	}
}