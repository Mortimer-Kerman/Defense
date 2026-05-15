package net.mortimer_kerman.defense;

import org.jetbrains.annotations.NotNull;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;

import net.mortimer_kerman.defense.mixin.client.HandledScreenAccessor;

public class DefenseToggleWidget extends ImageButton
{
    private static final Identifier SWITCH_BACKGROUND_TEXTURE = Identifier.fromNamespaceAndPath(Defense.MOD_ID, "defense_toggle/switch_background");
    private static final Identifier SWITCH_BACKGROUND_LIGHT_TEXTURE = Identifier.fromNamespaceAndPath(Defense.MOD_ID, "defense_toggle/switch_background_light");
    private static final Identifier BACKGROUND_TEXTURE = Identifier.fromNamespaceAndPath(Defense.MOD_ID, "defense_toggle/background");
    private static final Identifier BACKGROUND_HIGHLIGHTED_TEXTURE = Identifier.fromNamespaceAndPath(Defense.MOD_ID, "defense_toggle/background_highlighted");

    public DefenseToggleWidget(int x, int y, RecipeBookComponent<@NotNull InventoryMenu> recipeBook, AbstractRecipeBookScreen<@NotNull InventoryMenu> parent, Button.OnPress action)
    {
        super(x, y, 20, 18, null, action);
        this.recipeBook = recipeBook;
        this.parent = parent;
    }

    private final RecipeBookComponent<@NotNull InventoryMenu> recipeBook;
    private final AbstractRecipeBookScreen<@NotNull InventoryMenu> parent;
    private boolean recipeBookOpen = false;

    @Override
    public void renderContents(@NotNull GuiGraphics context, int mouseX, int mouseY, float deltaTicks)
    {
        if (recipeBook.isVisible() != recipeBookOpen)
        {
            this.setPosition(((HandledScreenAccessor)parent).getLeftPos() + 150, this.getY());
            recipeBookOpen = recipeBook.isVisible();
        }

        Identifier texture = DefenseClient.getDefenseIconOption().get().getTexture(true);

        context.blitSprite(RenderPipelines.GUI_TEXTURED, SWITCH_BACKGROUND_TEXTURE, this.getX(), this.getY(), 20, 18);

        Minecraft client = Minecraft.getInstance();

        int durationMinutes = DefenseClient.getDefenseDurationMinutes();

        if(DefenseClient.isPlayerImmune(client.player))
        {
            long durationTicks = DefenseClient.getDefenseDurationTicks();
            long time = client.level.getGameTime();
            long leftTimeTicks = DefenseClient.defenseStartTick + durationTicks - time;

            int h = Mth.ceil( leftTimeTicks / (double)durationTicks * 17.0F) + 1;
            context.blitSprite(RenderPipelines.GUI_TEXTURED, SWITCH_BACKGROUND_LIGHT_TEXTURE, 20, 18, 0, 18 - h, this.getX(), this.getY() + 18 - h, 20, h);

            int leftTimeMinutes = Mth.floor(leftTimeTicks/1200D);

            this.setTooltip(Tooltip.create(CommonComponents.joinLines(
                    net.minecraft.network.chat.Component.translatable("defense.tooltip.on").withStyle(style -> style.withColor(ChatFormatting.GREEN)),
                    net.minecraft.network.chat.Component.translatable("defense.tooltip.remaining", Defense.getMinutesText(leftTimeMinutes))
            )));
        }
        else if (durationMinutes == 0) this.setTooltip(Tooltip.create(net.minecraft.network.chat.Component.translatable("defense.tooltip.disabled").withStyle(style -> style.withColor(ChatFormatting.RED))));
        else this.setTooltip(Tooltip.create(CommonComponents.joinLines(
                    net.minecraft.network.chat.Component.translatable("defense.tooltip.off").withStyle(style -> style.withColor(ChatFormatting.AQUA)),
                    net.minecraft.network.chat.Component.translatable("defense.tooltip.duration", Defense.getMinutesText(durationMinutes))
        )));

        int offset = DefenseClient.pvpOff ? 6 : 0;

        context.blitSprite(RenderPipelines.GUI_TEXTURED, this.isHoveredOrFocused() ? BACKGROUND_HIGHLIGHTED_TEXTURE : BACKGROUND_TEXTURE, this.getX() + offset, this.getY(), 14, 18);
        context.blitSprite(RenderPipelines.GUI_TEXTURED, texture, this.getX() + 3 + offset, this.getY() + 5, 8, 9);
    }
}