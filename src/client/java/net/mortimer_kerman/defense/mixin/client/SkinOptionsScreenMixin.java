package net.mortimer_kerman.defense.mixin.client;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.option.SkinOptionsScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;

import net.mortimer_kerman.defense.DefenseClient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;

@Mixin(SkinOptionsScreen.class)
public abstract class SkinOptionsScreenMixin extends GameOptionsScreen
{
    @Unique private static final List<String> lst = Arrays.asList("a","b","c");

    @Inject(method = "addOptions", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/OptionListWidget;addAll(Ljava/util/List;)V", shift = At.Shift.AFTER))
    private void onAddOptions(CallbackInfo ci)
    {
        body.addSingleOptionEntry(DefenseClient.getDefenseIconOption());
    }

    public SkinOptionsScreenMixin(Screen parent, GameOptions gameOptions, Text title) { super(parent, gameOptions, title); }
}
