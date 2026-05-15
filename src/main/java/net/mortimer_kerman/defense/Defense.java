package net.mortimer_kerman.defense;

import io.netty.buffer.Unpooled;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gamerules.GameRules;

import net.mortimer_kerman.defense.argument.DefenseArgumentType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class Defense implements ModInitializer
{
	public static final String MOD_ID = "defense";
	public static final String MOD_VERSION = "1.21-0.4.1";
	public static final int VERSION_ID = 4;

	private static final HashSet<UUID> immunePlayers = new HashSet<>();
	private static final HashMap<UUID, Integer> playerIcons = new HashMap<>();
	private static final HashSet<UUID> afkPlayers = new HashSet<>();

	@Override
	public void onInitialize()
	{
		Payloads.RegisterPayloads();

		Gamerules.RegisterGamerules();

		ArgumentTypeRegistry.registerArgumentType(Identifier.fromNamespaceAndPath(MOD_ID, "template_defense_action"), DefenseArgumentType.class, SingletonArgumentInfo.contextFree(DefenseArgumentType::defenseAction));

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> Commands.DefenseCommand(dispatcher));
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> Commands.AfkCommand(dispatcher));

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
		{
			ServerPlayer player = handler.getPlayer();

			GameRules gameRules = server.overworld().getGameRules();
			int defenseDurationMinutes = gameRules.get(Gamerules.DEFENSE_DURATION_MINUTES);
			boolean allowDefenseKeybind = gameRules.get(Gamerules.ALLOW_DEFENSE_KEYBIND);

			server.execute(() ->
			{
				ServerPlayNetworking.send(player, new Payloads.GamerulePayloads.Integer(Gamerules.DEFENSE_DURATION_MINUTES.getIdentifier(), defenseDurationMinutes));
				ServerPlayNetworking.send(player, new Payloads.GamerulePayloads.Boolean(Gamerules.ALLOW_DEFENSE_KEYBIND.getIdentifier(), allowDefenseKeybind));

				for (UUID playerUUID : immunePlayers) ServerPlayNetworking.send(player, new Payloads.NotifyPVPPayload(playerUUID, true));
				for (UUID playerUUID : afkPlayers) ServerPlayNetworking.send(player, new Payloads.NotifyAfkPayload(playerUUID, true));
				playerIcons.forEach((UUID playerUUID, Integer iconID) -> ServerPlayNetworking.send(player, new Payloads.NotifyIconPayload(playerUUID, iconID)));
			});
		});

		ServerPlayConnectionEvents.DISCONNECT.register(((handler, server) ->
		{
			UUID playerUUID = handler.getPlayer().getUUID();

			if(immunePlayers.contains(playerUUID))
			{
				immunePlayers.remove(playerUUID);
				afkPlayers.remove(playerUUID);
				server.execute(() ->
				{
					for (ServerPlayer player : server.getPlayerList().getPlayers())
					{
						ServerPlayNetworking.send(player, new Payloads.NotifyPVPPayload(playerUUID, false));
						ServerPlayNetworking.send(player, new Payloads.NotifyAfkPayload(playerUUID, false));
					}
				});
			}
		}));

		ServerPlayNetworking.registerGlobalReceiver(Payloads.RecordPVPPayload.ID, (payload, context) ->
		{
			MinecraftServer server = context.server();
			ServerPlayer sender = context.player();

			UUID playerUUID = sender.getUUID();
			boolean pvpOff = payload.pvpOff();

			if (pvpOff) immunePlayers.add(playerUUID);
			else immunePlayers.remove(playerUUID);

			sender.resetLastActionTime();

			server.execute(() ->
			{
				for (ServerPlayer player : server.getPlayerList().getPlayers())
				{
					if (!sender.equals(player)) ServerPlayNetworking.send(player, new Payloads.NotifyPVPPayload(playerUUID, pvpOff));
				}
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(Payloads.RecordIconPayload.ID, (payload, context) ->
		{
			MinecraftServer server = context.server();
			ServerPlayer sender = context.player();

			UUID playerUUID = sender.getUUID();
			int iconID = payload.iconID();

			playerIcons.put(playerUUID, iconID);

			server.execute(() ->
			{
				for (ServerPlayer player : server.getPlayerList().getPlayers())
				{
					if (!sender.equals(player)) ServerPlayNetworking.send(player, new Payloads.NotifyIconPayload(playerUUID, iconID));
				}
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(Payloads.RequestAfkUpdatePayload.ID, (payload, context) ->
		{
			ServerPlayer sender = context.player();

			UUID playerUUID = sender.getUUID();

			if(afkPlayers.contains(playerUUID))
			{
				afkPlayers.remove(playerUUID);

				MinecraftServer server = context.server();

				server.execute(() ->
				{
					for (ServerPlayer player : server.getPlayerList().getPlayers())
					{
						ServerPlayNetworking.send(player, new Payloads.NotifyAfkPayload(playerUUID, false));
					}
				});
			}

			sender.resetLastActionTime();
		});

		ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) ->
		{
			FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer(5));
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

	public static boolean isPlayerImmune(Player player)
	{
		return immunePlayers.contains(player.getUUID());
	}

	public static boolean isEntityImmune(Entity entity, boolean petsProtected, boolean mountsProtected)
	{
		if (entity instanceof ServerPlayer attackedPlayer && isPlayerImmune(attackedPlayer)) return true;

		if (petsProtected && entity instanceof OwnableEntity pet)
		{
			LivingEntity owner = pet.getOwner();
			if (owner != null && isEntityImmune(owner, true, mountsProtected)) return true;
		}

		if (mountsProtected)
		{
			for (Entity passenger : entity.getPassengers())
			{
				if (passenger != null && isEntityImmune(passenger, petsProtected, true)) return true;
			}
		}

		return false;
	}

	public static boolean isPlayerRelated(Entity entity, boolean petsProtected, boolean mountsProtected)
	{
		if (entity instanceof ServerPlayer) return true;

		if (petsProtected && entity instanceof OwnableEntity pet)
		{
			LivingEntity owner = pet.getOwner();
			if (owner != null && isPlayerRelated(owner, true, mountsProtected)) return true;
		}

		if (mountsProtected)
		{
			for (Entity passenger : entity.getPassengers())
			{
				if (passenger != null && isPlayerRelated(passenger, petsProtected, true)) return true;
			}
		}
		return false;
	}

	public static boolean isPlayerAfk(Player player)
	{
		return afkPlayers.contains(player.getUUID());
	}

	public static void setPlayerAfk(ServerPlayer player)
	{
		UUID playerUUID = player.getUUID();

		if (afkPlayers.contains(playerUUID)) return;

		afkPlayers.add(player.getUUID());

		for (ServerPlayer plr : player.level().getServer().getPlayerList().getPlayers())
		{
			ServerPlayNetworking.send(plr, new Payloads.NotifyAfkPayload(playerUUID, true));
		}
	}

	public static Component getMinutesText(int minutes)
	{
		if(minutes < 1) return Component.translatable("defense.minutes.lessthanone");
		if(minutes == 1) return Component.translatable("defense.minutes.one");
		return Component.translatable("defense.minutes.morethanone", minutes);
	}

	public static Component getServerErrorMessage(ErrorReason reason)
	{
		String desc = switch (reason)
		{
            case MOD_NOT_INSTALLED -> "The Defense mod is not installed!";
            case CLIENT_OLDER -> "You have an outdated version of the Defense mod!";
            case CLIENT_NEWER -> "You have a too recent version of the Defense mod!";
        };
		Component version = Component.literal(MOD_VERSION).withStyle(style -> style.withUnderlined(true));
		Component url = Component.literal("https://modrinth.com/mod/defense").withStyle((style -> style.withColor(ChatFormatting.GREEN)));
		return Component.translatable(desc + " Please download the version %s from %s.", version, url);
	}

	public enum ErrorReason
	{
		MOD_NOT_INSTALLED,
		CLIENT_OLDER,
		CLIENT_NEWER
	}

	public static Identifier idOf(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}