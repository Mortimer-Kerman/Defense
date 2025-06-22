package net.mortimer_kerman.defense;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.KeyCodes;
import net.minecraft.client.option.GameOptions;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Util;

public class DefenseIconOptionsScreen extends GameOptionsScreen
{
    private DefenseIconSelectionListWidget iconSelectionList;

    public DefenseIconOptionsScreen(Screen parent, GameOptions options)
    {
        super(parent, options, Text.translatable("options.defense_icon.title"));
    }

    @Override
    protected void initBody()
    {
        this.iconSelectionList = this.layout.addBody(new DefenseIconSelectionListWidget(this.client));
    }

    @Override
    protected void addOptions() { }

    @Override
    protected void initFooter() {
        this.layout.addFooter(ButtonWidget.builder(ScreenTexts.DONE, button -> this.onDone()).width(200).build());
    }

    @Override
    protected void refreshWidgetPositions()
    {
        super.refreshWidgetPositions();
        this.iconSelectionList.position(this.width, this.layout);
    }

    void onDone()
    {
        DefenseIconSelectionListWidget.IconEntry iconEntry = this.iconSelectionList.getSelectedOrNull();

        if (iconEntry != null && !iconEntry.icon.equals(DefenseClient.getDefenseIconOption().getValue()))
        {
            DefenseClient.getDefenseIconOption().setValue(iconEntry.icon);
        }

        this.client.setScreen(this.parent);
    }

    @Environment(EnvType.CLIENT)
    class DefenseIconSelectionListWidget extends AlwaysSelectedEntryListWidget<DefenseIconSelectionListWidget.IconEntry>
    {
        public DefenseIconSelectionListWidget(final MinecraftClient client)
        {
            super(client, DefenseIconOptionsScreen.this.width, DefenseIconOptionsScreen.this.height - 33 - 53, 33, 18);

            DefenseIcon current = DefenseClient.getDefenseIconOption().getValue();

            for(DefenseIcon icon : DefenseIcon.values())
            {
                IconEntry iconEntry = new IconEntry(icon);
                this.addEntry(iconEntry);
                if (current.equals(icon)) this.setSelected(iconEntry);
            }

            if (this.getSelectedOrNull() != null) this.centerScrollOn(this.getSelectedOrNull());
        }

        @Override
        public int getRowWidth() { return super.getRowWidth() + 50; }

        @Environment(EnvType.CLIENT)
        public class IconEntry extends AlwaysSelectedEntryListWidget.Entry<IconEntry>
        {
            final DefenseIcon icon;
            private final Text iconText;
            private long clickTime;

            public IconEntry(final DefenseIcon icon)
            {
                this.icon = icon;
                this.iconText = icon.getText();
            }

            @Override
            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                context.drawCenteredTextWithShadow(DefenseIconOptionsScreen.this.textRenderer, this.iconText, DefenseIconSelectionListWidget.this.width / 2, y + entryHeight / 2 - 9 / 2, Colors.WHITE);
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, this.icon.getTexture(true), x + entryWidth - (int)(entryHeight*1.2f), y + (int)(entryHeight/5f), (int)(entryHeight/1.35f), (int)(entryHeight/1.2f));
            }

            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers)
            {
                if (KeyCodes.isToggle(keyCode))
                {
                    this.onPressed();
                    DefenseIconOptionsScreen.this.onDone();
                    return true;
                }

                return super.keyPressed(keyCode, scanCode, modifiers);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button)
            {
                this.onPressed();

                if (Util.getMeasuringTimeMs() - this.clickTime < 250L)
                {
                    DefenseIconOptionsScreen.this.onDone();
                }

                this.clickTime = Util.getMeasuringTimeMs();
                return super.mouseClicked(mouseX, mouseY, button);
            }

            private void onPressed() { DefenseIconSelectionListWidget.this.setSelected(this); }

            @Override
            public Text getNarration() { return Text.translatable("narrator.select", this.iconText); }
        }
    }
}
