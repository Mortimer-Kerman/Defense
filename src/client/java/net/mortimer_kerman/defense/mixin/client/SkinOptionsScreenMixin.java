package net.mortimer_kerman.defense.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.options.SkinCustomizationScreen;
import net.minecraft.network.chat.Component;

import net.mortimer_kerman.defense.DefenseIconOptionsScreen;

@Mixin(SkinCustomizationScreen.class)
public abstract class SkinOptionsScreenMixin extends OptionsSubScreen
{
    @Inject(method = "addOptions", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/OptionsList;addSmall(Ljava/util/List;)V", shift = At.Shift.AFTER))
    private void onAddOptions(CallbackInfo ci)
    {
        Button defenseMenuButton = Button.builder(Component.translatable("options.defense_icon"), button -> this.minecraft.setScreen(new DefenseIconOptionsScreen(this, options))).build();
        list.addSmall(defenseMenuButton, null);
    }

    public SkinOptionsScreenMixin(Screen parent, Options gameOptions, Component title) { super(parent, gameOptions, title); }
}
