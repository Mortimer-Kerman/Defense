package net.mortimer_kerman.defense;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.NarratedMultilineTextWidget;
import net.minecraft.text.*;
import net.mortimer_kerman.defense.interfaces.PlayerEntityAccess;
import org.jetbrains.annotations.Nullable;

public class AFKDefenseScreen extends Screen
{
    private final Text desc;

    private NarratedMultilineTextWidget textWidget;
    private ButtonWidget buttonWidget;

    private final Screen parent;

    public AFKDefenseScreen(@Nullable Screen parent)
    {
        super(Text.translatable("defense.afk_title").styled(style -> style.withBold(true)));
        this.parent = parent;
        this.desc = Text.translatable("defense.afk_desc");
    }

    @Override
    protected void init()
    {
        this.textWidget = this.addDrawableChild(new NarratedMultilineTextWidget(this.width, this.desc, this.textRenderer, 12));
        this.buttonWidget = this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.returnToGame"), button -> this.close()).width(200).build());
        this.initTabNavigation();
    }

    @Override
    protected void initTabNavigation()
    {
        this.textWidget.initMaxWidth((int)(this.width * 0.8f));
        this.textWidget.setPosition(this.width / 2 - this.textWidget.getWidth() / 2, this.height / 2 - 20);
        this.buttonWidget.setPosition(this.width / 2 - this.buttonWidget.getWidth() / 2, this.height / 2 + 30);
    }

    @Override
    protected boolean hasUsageText()
    {
        return false;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta)
    {
        this.applyBlur(delta);
        this.renderDarkening(context);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.getMatrices().push();
        context.getMatrices().scale(2.0F, 2.0F, 2.0F);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 4, this.height / 4 - 35, 16777215);
        context.getMatrices().pop();
    }

    @Override
    public void close()
    {
        this.client.setScreen(this.parent);
        PlayerEntityAccess plr = (PlayerEntityAccess)this.client.player;
        plr.defense$switchPvp(false);
    }
}
