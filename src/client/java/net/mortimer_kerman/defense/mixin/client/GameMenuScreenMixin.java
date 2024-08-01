package net.mortimer_kerman.defense.mixin.client;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.Positioner;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.text.Text;

import net.mortimer_kerman.defense.DefenseClient;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen
{
    @Shadow @Final private static Text RETURN_TO_GAME_TEXT;

    @WrapWithCondition(method = "initWidgets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/GridWidget$Adder;add(Lnet/minecraft/client/gui/widget/Widget;ILnet/minecraft/client/gui/widget/Positioner;)Lnet/minecraft/client/gui/widget/Widget;"))
    private <T extends Widget> boolean onInitWidget(GridWidget.Adder adder, T widget, int occupiedColumns, Positioner positioner)
    {
        adder.add(ButtonWidget.builder(RETURN_TO_GAME_TEXT, (button) -> {
            client.setScreen(null);
            client.mouse.lockCursor();
            DefenseClient.tryRecordIconChange();
        }).width(204).build(), occupiedColumns, positioner);
        return false;
    }

    protected GameMenuScreenMixin(Text title) { super(title); }
}
