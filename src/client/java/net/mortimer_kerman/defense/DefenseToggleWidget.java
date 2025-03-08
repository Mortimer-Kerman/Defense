package net.mortimer_kerman.defense;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import net.mortimer_kerman.defense.mixin.client.HandledScreenAccessor;

public class DefenseToggleWidget extends TexturedButtonWidget
{
    private static final Identifier SWITCH_BACKGROUND_TEXTURE = Identifier.of(Defense.MOD_ID, "defense_toggle/switch_background");
    private static final Identifier SWITCH_BACKGROUND_LIGHT_TEXTURE = Identifier.of(Defense.MOD_ID, "defense_toggle/switch_background_light");
    private static final Identifier BACKGROUND_TEXTURE = Identifier.of(Defense.MOD_ID, "defense_toggle/background");
    private static final Identifier BACKGROUND_HIGHLIGHTED_TEXTURE = Identifier.of(Defense.MOD_ID, "defense_toggle/background_highlighted");

    public DefenseToggleWidget(int x, int y, RecipeBookWidget recipeBook, AbstractInventoryScreen<PlayerScreenHandler> parent, ButtonWidget.PressAction action)
    {
        super(x, y, 20, 18, null, action);
        this.recipeBook = recipeBook;
        this.parent = parent;
    }

    private final RecipeBookWidget recipeBook;
    private final AbstractInventoryScreen<PlayerScreenHandler> parent;
    private boolean recipeBookOpen = false;

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta)
    {
        if (recipeBook.isOpen() != recipeBookOpen)
        {
            this.setPosition(((HandledScreenAccessor)parent).getX() + 150, this.getY());
            recipeBookOpen = recipeBook.isOpen();
        }

        Identifier texture = DefenseClient.getDefenseIconOption().getValue().getTexture(true);

        context.drawGuiTexture(SWITCH_BACKGROUND_TEXTURE, this.getX(), this.getY(), 20, 18);

        MinecraftClient client = MinecraftClient.getInstance();

        int durationMinutes = DefenseClient.getDefenseDurationMinutes();

        if(DefenseClient.isPlayerImmune(client.player))
        {
            long durationTicks = DefenseClient.getDefenseDurationTicks();
            long time = client.world.getTime();
            long leftTimeTicks = DefenseClient.defenseStartTick + durationTicks - time;

            int h = MathHelper.ceil( leftTimeTicks / (double)durationTicks * 17.0F) + 1;
            context.drawGuiTexture(SWITCH_BACKGROUND_LIGHT_TEXTURE, 20, 18, 0, 18 - h, this.getX(), this.getY() + 18 - h, 20, h);

            int leftTimeMinutes = MathHelper.floor(leftTimeTicks/1200D);

            this.setTooltip(Tooltip.of(ScreenTexts.joinLines(
                    Text.translatable("defense.tooltip.on").styled(style -> style.withColor(Formatting.GREEN)),
                    Text.translatable("defense.tooltip.remaining", Defense.getMinutesText(leftTimeMinutes))
            )));
        }
        else if (durationMinutes == 0) this.setTooltip(Tooltip.of(Text.translatable("defense.tooltip.disabled").styled(style -> style.withColor(Formatting.RED))));
        else this.setTooltip(Tooltip.of(ScreenTexts.joinLines(
                Text.translatable("defense.tooltip.off").styled(style -> style.withColor(Formatting.AQUA)),
                Text.translatable("defense.tooltip.duration", Defense.getMinutesText(durationMinutes))
        )));

        int offset = DefenseClient.pvpOff ? 6 : 0;

        context.drawGuiTexture(this.isSelected() ? BACKGROUND_HIGHLIGHTED_TEXTURE : BACKGROUND_TEXTURE, this.getX() + offset, this.getY(), 14, 18);
        context.drawGuiTexture(texture, this.getX() + 3 + offset, this.getY() + 5, 8, 9);
    }
}