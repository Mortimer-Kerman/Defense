package net.mortimer_kerman.defense;

import com.mojang.serialization.Codec;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.login.LoginDisconnectS2CPacket;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.mortimer_kerman.defense.interfaces.PlayerEntityAccess;
import net.mortimer_kerman.defense.render.DefenseRenderLayers;
import net.mortimer_kerman.defense.render.DefenseRenderPipelines;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class DefenseClient implements ClientModInitializer
{
	private static final HashSet<UUID> immunePlayers = new HashSet<>();
	private static final HashMap<UUID, DefenseIcon> playerIcons = new HashMap<>();
	private static final HashSet<UUID> afkPlayers = new HashSet<>();

	private static SimpleOption<DefenseIcon> defenseIconOption;

	private static boolean defenseIconChanged = false;

	private static int defenseDurationMinutes = 20;
	private static boolean allowDefenseKeybind = false;

	private static KeyBinding defenseKey;
	private static boolean defensePressed = false;

	@Override
	public void onInitializeClient()
	{
		DefenseRenderPipelines.init();
		DefenseRenderLayers.init();

		defenseKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key." + Defense.MOD_ID + ".toggleDefense",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_UNKNOWN,
				KeyBinding.MULTIPLAYER_CATEGORY
		));

		ClientTickEvents.END_CLIENT_TICK.register(client ->
		{
			if (defenseKey.isPressed())
			{
				if (!defensePressed)
				{
					if (allowDefenseKeybind) ((PlayerEntityAccess)MinecraftClient.getInstance().player).defense$switchPvp(!pvpOff);
					defensePressed = true;
				}
			}
			else defensePressed = false;
		});

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
			if (!(context.client().currentScreen instanceof  AFKDefenseScreen)) context.client().setScreen(new AFKDefenseScreen(context.client().currentScreen));
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
			String gameruleName = payload.gameruleName();

			if (gameruleName.equals(Gamerules.DEFENSE_DURATION_MINUTES.getName()))
			{
				int val = payload.value();
                durationChange = Integer.compare(val, defenseDurationMinutes);
				defenseDurationMinutes = payload.value();

			}
		});

		ClientPlayNetworking.registerGlobalReceiver(Payloads.GamerulePayloads.Boolean.ID, (payload, context) ->
		{
			String gameruleName = payload.gameruleName();

			if (gameruleName.equals(Gamerules.ALLOW_DEFENSE_KEYBIND.getName()))
			{
				allowDefenseKeybind = payload.value();
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
		return isPlayerImmune(player.getUuid());
	}

	/**
	 * Checks if a player is immune to PVP from its UUID.
	 * @param uuid - the player UUID you want to check
	 * @return {@code true} if the player is immune to PVP, {@code false} otherwise.
	 */
	public static boolean isPlayerImmune(UUID uuid)
	{
		if (uuid.equals(MinecraftClient.getInstance().player.getUuid())) return pvpOff;
		return immunePlayers.contains(uuid);
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
		return getPlayerIcon(player.getUuid());
	}

	/**
	 * Gets the defense icon from a player UUID.
	 * @param player - the player UUID you want to get the icon from
	 * @return A {@code DefenseIcon}. If the player is not found, {@code DefenseIcon.DEFAULT} is returned.
	 */
	public static DefenseIcon getPlayerIcon(UUID player)
	{
		if (player.equals(MinecraftClient.getInstance().player.getUuid())) return getDefenseIconOption().getValue();
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
		MinecraftClient.getInstance().execute(() -> ClientPlayNetworking.send(new Payloads.RequestAfkUpdatePayload()));
		afkUpdateRequested = false;
	}

	/**
	 * Gets the AFK status from a player UUID, in the way <i>is-the-defense-afk-screen-displayed</i>.
	 * @param playerUUID - the player UUID you want to know the AFK status
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

	public static Text getDefenseContinueText(PlayerEntityAccess plr)
	{
		int durationMinutes = getDefenseDurationMinutes();
		Text text = Text.translatable("chat.immunity.continue", Defense.getMinutesText(durationMinutes));
		return Texts.bracketed(text).styled(style -> style.withColor(Formatting.GREEN).withClickEvent(new CRunnableClickEvent(() -> plr.defense$switchPvp(true))).withHoverEvent(new HoverEvent.ShowText(text)));
	}

	private static boolean afkUpdateRequested = false;

	public static boolean isAfk = false;
	public static long defenseStartTick = Long.MIN_VALUE;
	public static boolean pvpOff = false;
	public static int durationChange = 0;
}