package net.mortimer_kerman.defense;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;

import net.mortimer_kerman.defense.interfaces.PlayerEntityAccess;
import net.mortimer_kerman.defense.mixin.client.HandledScreenAccessor;

@Environment(EnvType.CLIENT)
public class DefenseToggleWidget extends TexturedButtonWidget
{
    private static final Identifier SWITCH_BACKGROUND_TEXTURE = Identifier.of(Defense.MOD_ID, "defense_toggle/switch_background");
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

        int offset = DefenseClient.pvpOff ? 6 : 0;

        context.drawGuiTexture(this.isSelected() ? BACKGROUND_HIGHLIGHTED_TEXTURE : BACKGROUND_TEXTURE, this.getX() + offset, this.getY(), 14, 18);
        context.drawGuiTexture(texture, this.getX() + 3 + offset, this.getY() + 5, 8, 9);
    }
}