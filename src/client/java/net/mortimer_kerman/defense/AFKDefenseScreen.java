package net.mortimer_kerman.defense;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.FocusableTextWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import net.mortimer_kerman.defense.interfaces.PlayerEntityAccess;

public class AFKDefenseScreen extends Screen
{
    private final Component desc;

    private FocusableTextWidget textWidget;
    private Button buttonWidget;

    private final Screen parent;

    public AFKDefenseScreen(@Nullable Screen parent)
    {
        super(Component.translatable("defense.afk_title").withStyle(style -> style.withBold(true)));
        this.parent = parent;
        this.desc = Component.translatable("defense.afk_desc");
    }

    @Override
    protected void init()
    {
        this.textWidget = this.addRenderableWidget(FocusableTextWidget.builder(this.desc, this.font, 12).maxWidth(this.width).build());
        this.buttonWidget = this.addRenderableWidget(Button.builder(Component.translatable("menu.returnToGame"), button -> this.onClose()).width(200).build());
        this.repositionElements();
    }

    @Override
    protected void repositionElements()
    {
        this.textWidget.setMaxWidth((int)(this.width * 0.8f));
        this.textWidget.setPosition(this.width / 2 - this.textWidget.getWidth() / 2, this.height / 2 - 20);
        this.buttonWidget.setPosition(this.width / 2 - this.buttonWidget.getWidth() / 2, this.height / 2 + 30);
    }

    @Override
    protected boolean shouldNarrateNavigation()
    {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        this.renderBlurredBackground(context);
        this.renderMenuBackground(context);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.pose().pushMatrix();
        context.pose().scale(2.0F, 2.0F);
        context.drawCenteredString(this.font, this.title, this.width / 4, this.height / 4 - 35, 16777215);
        context.pose().popMatrix();
    }

    @Override
    public void onClose()
    {
        this.minecraft.setScreen(this.parent);
        DefenseClient.isAfk = false;
        PlayerEntityAccess plr = (PlayerEntityAccess)this.minecraft.player;
        plr.defense$switchPvp(true);
        DefenseClient.requestAfkUpdate();
    }
}
