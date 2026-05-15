package net.mortimer_kerman.defense;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

public class DefenseIconOptionsScreen extends OptionsSubScreen
{
    private DefenseIconSelectionListWidget iconSelectionList;

    public DefenseIconOptionsScreen(Screen parent, Options options)
    {
        super(parent, options, Component.translatable("options.defense_icon.title"));
    }

    @Override
    protected void addContents()
    {
        this.iconSelectionList = this.layout.addToContents(new DefenseIconSelectionListWidget(this.minecraft));
    }

    @Override
    protected void addOptions() { }

    @Override
    protected void addFooter() {
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, button -> this.onDone()).width(200).build());
    }

    @Override
    protected void repositionElements()
    {
        super.repositionElements();
        this.iconSelectionList.updateSize(this.width, this.layout);
    }

    void onDone()
    {
        DefenseIconSelectionListWidget.IconEntry iconEntry = this.iconSelectionList.getSelected();

        if (iconEntry != null && !iconEntry.icon.equals(DefenseClient.getDefenseIconOption().get()))
        {
            DefenseClient.getDefenseIconOption().set(iconEntry.icon);
        }

        this.minecraft.setScreen(this.lastScreen);
    }

    @Environment(EnvType.CLIENT)
    class DefenseIconSelectionListWidget extends ObjectSelectionList<DefenseIconSelectionListWidget.IconEntry>
    {
        public DefenseIconSelectionListWidget(final Minecraft client)
        {
            super(client, DefenseIconOptionsScreen.this.width, DefenseIconOptionsScreen.this.height - 33 - 53, 33, 18);

            DefenseIcon current = DefenseClient.getDefenseIconOption().get();

            for(DefenseIcon icon : DefenseIcon.values())
            {
                IconEntry iconEntry = new IconEntry(icon);
                this.addEntry(iconEntry);
                if (current.equals(icon)) this.setSelected(iconEntry);
            }

            if (this.getSelected() != null) this.centerScrollOn(this.getSelected());
        }

        @Override
        public int getRowWidth() { return super.getRowWidth() + 50; }

        @Environment(EnvType.CLIENT)
        public class IconEntry extends ObjectSelectionList.Entry<IconEntry>
        {
            final DefenseIcon icon;
            private final Component iconText;
            private long clickTime;

            public IconEntry(final DefenseIcon icon)
            {
                this.icon = icon;
                this.iconText = Component.translatable(icon.getTranslationKey());
            }

            @Override
            public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
                int entryWidth = getContentWidth();
                int entryHeight = getContentHeight();
                int x = getContentX();
                int y = getContentY();
                context.drawCenteredString(DefenseIconOptionsScreen.this.font, this.iconText, DefenseIconSelectionListWidget.this.width / 2, y + entryHeight / 2 - 9 / 2, CommonColors.WHITE);
                context.blitSprite(RenderPipelines.GUI_TEXTURED, this.icon.getTexture(true), x + entryWidth - (int)(entryHeight*1.2f), y + (int)(entryHeight/5f), (int)(entryHeight/1.35f), (int)(entryHeight/1.2f));
            }

            @Override
            public boolean keyPressed(KeyEvent input)
            {
                if (input.isSelection())
                {
                    this.onPressed();
                    DefenseIconOptionsScreen.this.onDone();
                    return true;
                }

                return super.keyPressed(input);
            }

            @Override
            public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {

                this.onPressed();

                if (doubled)
                {
                    DefenseIconOptionsScreen.this.onDone();
                }
                return super.mouseClicked(click, doubled);
            }

            private void onPressed() { DefenseIconSelectionListWidget.this.setSelected(this); }

            @Override
            public Component getNarration() { return Component.translatable("narrator.select", this.iconText); }
        }
    }
}
