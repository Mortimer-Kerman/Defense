package net.mortimer_kerman.defense;

import com.mojang.serialization.Codec;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import net.mortimer_kerman.defense.interfaces.PlayerEntityAccess;
import net.mortimer_kerman.defense.mixin.client.SimpleOptionAccessor;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

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
                new DefenseIconCallbacks(Arrays.asList(DefenseIcon.values()), Codec.INT.xmap(DefenseIcon::byId, DefenseIcon::getId)),
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
	}

	/**
	 * Checks if a player is immune to PVP
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
	 * If the player has changed its defense icon in the options, this function sends the change to the server.
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

	public static long defenseEndTick = 0L;
	public static boolean pvpOff = false;

	public record DefenseIconCallbacks(List<DefenseIcon> values, Codec<DefenseIcon> codec) implements SimpleOption.CyclingCallbacks<DefenseIcon>
	{
        @Override public Optional<DefenseIcon> validate(DefenseIcon value) { return this.values.contains(value) ? Optional.of(value) : Optional.empty(); }
		@Override public CyclingButtonWidget.Values<DefenseIcon> getValues() { return CyclingButtonWidget.Values.of(this.values); }
		@Override public Codec<DefenseIcon> codec() { return this.codec; }

		@Override
		public Function<SimpleOption<DefenseIcon>, ClickableWidget> getWidgetCreator(SimpleOption.TooltipFactory<DefenseIcon> tooltipFactory, GameOptions gameOptions, int x, int y, int width, Consumer<DefenseIcon> changeCallback) {
			return (option) -> {
				List<DefenseIcon> list = this.getValues().getDefaults();
				DefenseIcon value = option.getValue();
				int initialIndex = 0;
				int i = list.indexOf(value);
				if (i != -1) { initialIndex = i; }
				DefenseIcon object = value != null ? value : list.get(initialIndex);

				Text text = ((SimpleOptionAccessor<DefenseIcon>)(Object)option).getText();
				Function<DefenseIcon, Text> textGetter = ((SimpleOptionAccessor<DefenseIcon>)(Object)option).getTextGetter();

				Text text2 = ScreenTexts.composeGenericOptionText(text, textGetter.apply(object));
				return new CyclingButtonWidget<>(x, y, width, 20, (Text) text2, text, initialIndex, object, this.getValues(), textGetter, CyclingButtonWidget::getGenericNarrationMessage, (button, val) -> {
                    this.valueSetter().set(option, val);
                    gameOptions.write();
                    changeCallback.accept(val);
                }, tooltipFactory, false){
					@Override
					protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
						super.renderWidget(context, mouseX, mouseY, delta);

						int posX = getX();
						int posY = getY();
						int sizeX = getWidth();
						int sizeY = getHeight();

						context.drawGuiTexture(getDefenseIconOption().getValue().getTexture(true),posX + sizeX - sizeY + (sizeY/4), posY + (sizeY/4), (int)(sizeY/2.25f), sizeY/2);
					}
				};
			};
		}
	}
}