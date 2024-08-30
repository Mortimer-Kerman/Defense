package net.mortimer_kerman.defense;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.mortimer_kerman.defense.argument.DefenseArgumentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize()
	{
		Payloads.RegisterPayloads();

		Gamerules.RegisterGamerules();

		ArgumentTypeRegistry.registerArgumentType(Identifier.of(MOD_ID, "template_defense_action"), DefenseArgumentType.class, ConstantArgumentSerializer.of(DefenseArgumentType::defenseAction));

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> Commands.DefenseCommand(dispatcher));
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> Commands.AfkCommand(dispatcher));

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
		{
			ServerPlayerEntity player = handler.getPlayer();

			int defenseDurationMinutes = server.getGameRules().getInt(Gamerules.DEFENSE_DURATION_MINUTES);
			boolean allowDefenseKeybind = server.getGameRules().getBoolean(Gamerules.ALLOW_DEFENSE_KEYBIND);

			server.execute(() ->
			{
				ServerPlayNetworking.send(player, new Payloads.GamerulePayloads.Integer(Gamerules.DEFENSE_DURATION_MINUTES.getName(), defenseDurationMinutes));
				ServerPlayNetworking.send(player, new Payloads.GamerulePayloads.Boolean(Gamerules.ALLOW_DEFENSE_KEYBIND.getName(), allowDefenseKeybind));

				for (UUID playerUUID : immunePlayers) ServerPlayNetworking.send(player, new Payloads.NotifyPVPPayload(playerUUID, true));
				for (UUID playerUUID : afkPlayers) ServerPlayNetworking.send(player, new Payloads.NotifyAfkPayload(playerUUID, true));
				playerIcons.forEach((UUID playerUUID, Integer iconID) -> ServerPlayNetworking.send(player, new Payloads.NotifyIconPayload(playerUUID, iconID)));
			});
		});

		ServerPlayConnectionEvents.DISCONNECT.register(((handler, server) ->
		{
			UUID playerUUID = handler.getPlayer().getUuid();

			if(immunePlayers.contains(playerUUID))
			{
				immunePlayers.remove(playerUUID);
				afkPlayers.remove(playerUUID);
				server.execute(() ->
				{
					for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList())
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
			ServerPlayerEntity sender = context.player();

			UUID playerUUID = sender.getUuid();
			boolean pvpOff = payload.pvpOff();

			if (pvpOff) immunePlayers.add(playerUUID);
			else immunePlayers.remove(playerUUID);

			sender.updateLastActionTime();

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

		ServerPlayNetworking.registerGlobalReceiver(Payloads.RequestAfkUpdatePayload.ID, (payload, context) ->
		{
			ServerPlayerEntity sender = context.player();

			UUID playerUUID = sender.getUuid();

			if(afkPlayers.contains(playerUUID))
			{
				afkPlayers.remove(playerUUID);

				MinecraftServer server = context.server();

				server.execute(() ->
				{
					for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList())
					{
						ServerPlayNetworking.send(player, new Payloads.NotifyAfkPayload(playerUUID, false));
					}
				});
			}

			sender.updateLastActionTime();
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

	public static boolean isEntityImmune(Entity entity, boolean petsProtected, boolean mountsProtected)
	{
		if (entity instanceof ServerPlayerEntity attackedPlayer && isPlayerImmune(attackedPlayer)) return true;

		if (petsProtected && entity instanceof Tameable pet)
		{
			LivingEntity owner = pet.getOwner();
			if (owner != null && isEntityImmune(owner, true, mountsProtected)) return true;
		}

		if (mountsProtected)
		{
			for (Entity passenger : entity.getPassengerList())
			{
				if (passenger != null && isEntityImmune(passenger, petsProtected, true)) return true;
			}
		}

		return false;
	}

	public static boolean isPlayerRelated(Entity entity, boolean petsProtected, boolean mountsProtected)
	{
		if (entity instanceof ServerPlayerEntity) return true;

		if (petsProtected && entity instanceof Tameable pet)
		{
			LivingEntity owner = pet.getOwner();
			if (owner != null && isPlayerRelated(owner, true, mountsProtected)) return true;
		}

		if (mountsProtected)
		{
			for (Entity passenger : entity.getPassengerList())
			{
				if (passenger != null && isPlayerRelated(passenger, petsProtected, true)) return true;
			}
		}
		return false;
	}

	public static boolean isPlayerAfk(PlayerEntity player)
	{
		return afkPlayers.contains(player.getUuid());
	}

	public static void setPlayerAfk(ServerPlayerEntity player)
	{
		UUID playerUUID = player.getUuid();

		if (afkPlayers.contains(playerUUID)) return;

		afkPlayers.add(player.getUuid());

		for (ServerPlayerEntity plr : player.server.getPlayerManager().getPlayerList())
		{
			ServerPlayNetworking.send(plr, new Payloads.NotifyAfkPayload(playerUUID, true));
		}
	}

	public static Text getMinutesText(int minutes)
	{
		if(minutes < 1) return Text.translatable("defense.minutes.lessthanone");
		if(minutes == 1) return Text.translatable("defense.minutes.one");
		return Text.translatable("defense.minutes.morethanone", minutes);
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