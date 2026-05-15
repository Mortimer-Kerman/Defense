package net.mortimer_kerman.defense;

import io.netty.buffer.Unpooled;
import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import net.mortimer_kerman.defense.interfaces.PlayerEntityAccess;
import net.mortimer_kerman.defense.render.DefenseRenderLayers;
import net.mortimer_kerman.defense.render.DefenseRenderPipelines;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DefenseClient implements ClientModInitializer
{
	private static final HashSet<UUID> immunePlayers = new HashSet<>();
	private static final HashMap<UUID, DefenseIcon> playerIcons = new HashMap<>();
	private static final HashSet<UUID> afkPlayers = new HashSet<>();

	private static OptionInstance<DefenseIcon> defenseIconOption;

	private static boolean defenseIconChanged = false;

	private static int defenseDurationMinutes = 20;
	private static boolean allowDefenseKeybind = false;

	private static KeyMapping defenseKey;
	private static boolean defensePressed = false;

	@Override
	public void onInitializeClient()
	{
		DefenseRenderPipelines.init();
		DefenseRenderLayers.init();

		defenseKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key." + Defense.MOD_ID + ".toggleDefense",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_UNKNOWN,
				KeyMapping.Category.MULTIPLAYER
		));

		ClientTickEvents.END_CLIENT_TICK.register(client ->
		{
			if (defenseKey.isDown())
			{
				if (!defensePressed)
				{
					if (allowDefenseKeybind) ((PlayerEntityAccess)Minecraft.getInstance().player).defense$switchPvp(!pvpOff);
					defensePressed = true;
				}
			}
			else defensePressed = false;
		});

		defenseIconOption = new OptionInstance<>(
				"options.defense_icon",
				OptionInstance.noTooltip(),
				(optionText, value) -> Component.translatable(value.getTranslationKey()),
				new OptionInstance.Enum<>(Arrays.asList(DefenseIcon.values()), Codec.INT.xmap(DefenseIcon::byId, DefenseIcon::getId)),
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
			defenseStartTick = Long.MIN_VALUE;
			pvpOff = false;

			defenseIconChanged = true;
			tryRecordIconChange();
		});

		ClientPlayConnectionEvents.DISCONNECT.register(((handler, client) ->
		{
			defenseStartTick = Long.MIN_VALUE;
			pvpOff = false;
		}));

		ClientPlayNetworking.registerGlobalReceiver(Payloads.EnableAfkPayload.ID, (payload, context) ->
		{
			if (!(context.client().screen instanceof AFKDefenseScreen)) context.client().setScreen(new AFKDefenseScreen(context.client().screen));
			isAfk = true;
			PlayerEntityAccess plr = (PlayerEntityAccess)context.player();
			plr.defense$switchPvp(true);
		});

		ClientPlayNetworking.registerGlobalReceiver(Payloads.NotifyAfkPayload.ID, ((payload, context) ->
		{
			if(payload.afk()) afkPlayers.add(payload.playerUUID());
			else afkPlayers.remove(payload.playerUUID());
		}));

		ClientLoginNetworking.registerGlobalReceiver(Payloads.handshakeID, (client, handler, buf, callback) ->
		{
			int version = buf.readVarInt();
			if(version > Defense.VERSION_ID)
			{
				handler.onDisconnect(new DisconnectionDetails(Defense.getServerErrorMessage(Defense.ErrorReason.CLIENT_OLDER)));
				return CompletableFuture.completedFuture(null);
			}
			else if(version < Defense.VERSION_ID)
			{
				handler.onDisconnect(new DisconnectionDetails(Defense.getServerErrorMessage(Defense.ErrorReason.CLIENT_NEWER)));
				return CompletableFuture.completedFuture(null);
			}
			else
			{
				return CompletableFuture.completedFuture(new FriendlyByteBuf(Unpooled.EMPTY_BUFFER));
			}
		});

		ClientPlayNetworking.registerGlobalReceiver(Payloads.ForceDefensePayload.ID, ((payload, context) ->
		{
			PlayerEntityAccess plr = (PlayerEntityAccess)context.player();

			switch (payload.action())
			{
				case 0 -> { if (!isPlayerImmune(context.player())) plr.defense$switchPvp(true); }
				case 1 -> { if (isPlayerImmune(context.player())) plr.defense$switchPvp(false); }
				case 2 -> plr.defense$switchPvp(true);
			}
		}));

		ClientPlayNetworking.registerGlobalReceiver(Payloads.GamerulePayloads.Integer.ID, (payload, context) ->
		{
			Identifier gameruleId = payload.gameruleId();

			if (gameruleId.equals(Gamerules.DEFENSE_DURATION_MINUTES.getIdentifier()))
			{
				int val = payload.value();
                durationChange = Integer.compare(val, defenseDurationMinutes);
				defenseDurationMinutes = payload.value();

			}
		});

		ClientPlayNetworking.registerGlobalReceiver(Payloads.GamerulePayloads.Boolean.ID, (payload, context) ->
		{
			Identifier gameruleId = payload.gameruleId();

			if (gameruleId.equals(Gamerules.ALLOW_DEFENSE_KEYBIND.getIdentifier()))
			{
				allowDefenseKeybind = payload.value();
			}
		});
	}

	/**
	 * Checks if a player is immune to PVP.
	 * @param player the player you want to check
	 * @return {@code true} if the player is immune to PVP, {@code false} otherwise.
	 */
	public static boolean isPlayerImmune(Player player)
	{
		return isPlayerImmune(player.getUUID());
	}

	/**
	 * Checks if a player is immune to PVP from its UUID.
	 * @param uuid the player UUID you want to check
	 * @return {@code true} if the player is immune to PVP, {@code false} otherwise.
	 */
	public static boolean isPlayerImmune(UUID uuid)
	{
		if (uuid.equals(Minecraft.getInstance().player.getUUID())) return pvpOff;
		return immunePlayers.contains(uuid);
	}

	public static OptionInstance<DefenseIcon> getDefenseIconOption()
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
		Minecraft.getInstance().execute(() -> ClientPlayNetworking.send(new Payloads.RecordIconPayload(getDefenseIconOption().get().getId())));
		defenseIconChanged = false;
	}

	/**
	 * Gets the defense icon from a player.
	 * @param player the player you want to get the icon from
	 * @return A {@code DefenseIcon}. If the player is not found, {@code DefenseIcon.DEFAULT} is returned.
	 */
	public static DefenseIcon getPlayerIcon(Player player)
	{
		return getPlayerIcon(player.getUUID());
	}

	/**
	 * Gets the defense icon from a player UUID.
	 * @param player the player UUID you want to get the icon from
	 * @return A {@code DefenseIcon}. If the player is not found, {@code DefenseIcon.DEFAULT} is returned.
	 */
	public static DefenseIcon getPlayerIcon(UUID player)
	{
		if (player.equals(Minecraft.getInstance().player.getUUID())) return getDefenseIconOption().get();
		return playerIcons.getOrDefault(player, DefenseIcon.DEFAULT);
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
		Minecraft.getInstance().execute(() -> ClientPlayNetworking.send(new Payloads.RequestAfkUpdatePayload()));
		afkUpdateRequested = false;
	}

	/**
	 * Gets the AFK status from a player UUID, in the "<i>is-the-defense-afk-screen-displayed</i>" way.
	 * @param playerUUID the player UUID you want to know the AFK status
	 * @return {@code true} if the player is AFK, {@code false} otherwise.
	 */
	public static boolean isPlayerAfk(UUID playerUUID)
	{
		return afkPlayers.contains(playerUUID);
	}

	/**
	 * @return the value of the gamerule {@code defenseDurationMinutes} on client side.
	 */
	public static int getDefenseDurationMinutes()
	{
		return defenseDurationMinutes;
	}

	/**
	 * @return the value of the gamerule {@code defenseDurationMinutes} on client side, but in ticks and not minutes.
	 */
	public static long getDefenseDurationTicks()
	{
		return getDefenseDurationMinutes() * 1200L;
	}

	public static Component getDefenseContinueText(PlayerEntityAccess plr)
	{
		int durationMinutes = getDefenseDurationMinutes();
		Component text = Component.translatable("chat.immunity.continue", Defense.getMinutesText(durationMinutes));
		return ComponentUtils.wrapInSquareBrackets(text).withStyle(style -> style.withColor(ChatFormatting.GREEN).withClickEvent(new CRunnableClickEvent(() -> plr.defense$switchPvp(true))).withHoverEvent(new HoverEvent.ShowText(text)));
	}

	private static boolean afkUpdateRequested = false;

	public static boolean isAfk = false;
	public static long defenseStartTick = Long.MIN_VALUE;
	public static boolean pvpOff = false;
	public static int durationChange = 0;
}