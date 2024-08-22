package net.mortimer_kerman.defense;

import com.mojang.serialization.Codec;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.login.LoginDisconnectS2CPacket;
import net.mortimer_kerman.defense.interfaces.PlayerEntityAccess;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class DefenseClient implements ClientModInitializer
{
	private static final HashSet<UUID> immunePlayers = new HashSet<>();
	private static final HashMap<UUID, DefenseIcon> playerIcons = new HashMap<>();

	private static SimpleOption<DefenseIcon> defenseIconOption;

	private static boolean defenseIconChanged = false;

	@Override
	public void onInitializeClient()
	{
		defenseIconOption = new SimpleOption<>(
				"options.defense_icon",
				SimpleOption.emptyTooltip(),
				SimpleOption.enumValueText(),
				new SimpleOption.PotentialValuesBasedCallbacks<>(Arrays.asList(DefenseIcon.values()), Codec.INT.xmap(DefenseIcon::byId, DefenseIcon::getId)),
				DefenseIcon.DEFAULT, (value) -> defenseIconChanged = true);

		ClientPlayNetworking.registerGlobalReceiver(Payloads.NotifyPVPPayload.ID, (payload, context) ->
		{
			UUID playerUUID = payload.playerUUID();
			if (payload.pvpOff()) immunePlayers.add(playerUUID);
			else immunePlayers.remove(playerUUID);
		});

		ClientPlayNetworking.registerGlobalReceiver(Payloads.NotifyIconPayload.ID, (payload, context) -> playerIcons.put(payload.playerUUID(), DefenseIcon.byId(payload.iconID())));

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
		{
			defenseEndTick = 0L;
			pvpOff = false;

			defenseIconChanged = true;
			tryRecordIconChange();
		});

		ClientPlayConnectionEvents.DISCONNECT.register(((handler, client) ->
		{
			defenseEndTick = 0L;
			pvpOff = false;
		}));

		ClientPlayNetworking.registerGlobalReceiver(Payloads.EnableAfkPayload.ID, (payload, context) ->
		{
			context.client().setScreen(new AFKDefenseScreen(context.client().currentScreen));
			PlayerEntityAccess plr = (PlayerEntityAccess)context.player();
			if (!isPlayerImmune(context.player())) plr.defense$switchPvp(true);
		});

		ClientLoginNetworking.registerGlobalReceiver(Payloads.handshakeID, (client, handler, buf, callback) ->
		{
			int version = buf.readVarInt();
			if(version > Defense.VERSION_ID)
			{
				handler.onDisconnect(new LoginDisconnectS2CPacket(Defense.getServerErrorMessage(Defense.ErrorReason.CLIENT_OLDER)));
				return CompletableFuture.completedFuture(null);
			}
			else if(version < Defense.VERSION_ID)
			{
				handler.onDisconnect(new LoginDisconnectS2CPacket(Defense.getServerErrorMessage(Defense.ErrorReason.CLIENT_NEWER)));
				return CompletableFuture.completedFuture(null);
			}
			else
			{
				return CompletableFuture.completedFuture(new PacketByteBuf(Unpooled.EMPTY_BUFFER));
			}
		});
	}

	/**
	 * Checks if a player is immune to PVP.
	 * @param player - the player you want to check
	 * @return {@code true} if the player is immune to PVP, {@code false} otherwise.
	 */
	public static boolean isPlayerImmune(PlayerEntity player)
	{
		if (player == MinecraftClient.getInstance().player) return pvpOff;
		return immunePlayers.contains(player.getUuid());
	}

	public static SimpleOption<DefenseIcon> getDefenseIconOption()
	{
		return defenseIconOption;
	}

	/**
	 * If the player has changed its defense icon in the options, this function sends the change to the server. <br>
	 * Otherwise, it does nothing.
	 */
	public static void tryRecordIconChange()
	{
		if (!defenseIconChanged) return;
		MinecraftClient.getInstance().execute(() -> ClientPlayNetworking.send(new Payloads.RecordIconPayload(getDefenseIconOption().getValue().getId())));
		defenseIconChanged = false;
	}

	/**
	 * Gets the defense icon from a player.
	 * @param player - the player you want to get the icon from
	 * @return A {@code DefenseIcon}. If the player is not found, {@code DefenseIcon.DEFAULT} is returned.
	 */
	public static DefenseIcon getPlayerIcon(PlayerEntity player)
	{
		if (player.equals(MinecraftClient.getInstance().player)) return getDefenseIconOption().getValue();
		return playerIcons.getOrDefault(player.getUuid(), DefenseIcon.DEFAULT);
	}

	/**
	 * Sends a packet to trigger {@code ServerPlayerEntity.updateLastActionTime()} for the current player on the server. <br>
	 * Please note that the packet can take up to one tick to be sent.
	 */
	public static void requestAfkUpdate()
	{
		afkUpdateRequested = true;
	}

	/**
	 * @return {@code true} if {@code requestAfkUpdate()} was executed in this tick, {@code false} otherwise.
	 */
	public static boolean afkUpdateRequested()
	{
		return afkUpdateRequested;
	}

	/**
	 * Immediatly sends a packet to trigger {@code ServerPlayerEntity.updateLastActionTime()} for the current player on the server. <br>
	 * It is highly not recommended to use this function, as duplicate packets could be sent. Please use {@code requestAfkUpdate()} instead.
	 */
	public static void requestImmediateAfkUpdate()
	{
		MinecraftClient.getInstance().execute(() -> ClientPlayNetworking.send(new Payloads.RequestAfkUpdatePayload()));
		afkUpdateRequested = false;
	}

	private static boolean afkUpdateRequested = false;

	public static long defenseEndTick = 0L;
	public static boolean pvpOff = false;
}