package net.mortimer_kerman.defense.mixin.client;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.platform.WindowEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;

import net.mortimer_kerman.defense.DefenseClient;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin extends ReentrantBlockableEventLoop<@NotNull Runnable> implements WindowEventHandler
{
    @Shadow @Nullable public Screen screen;

    @Shadow private boolean clientLevelTeardownInProgress;

    @Inject(method = "setScreen", at = @At(value = "HEAD"))
    private void onSetScreen(@Nullable Screen screen, CallbackInfo ci)
    {
        if (this.screen instanceof PauseScreen && screen == null && !clientLevelTeardownInProgress) DefenseClient.tryRecordIconChange();
    }

    public MinecraftClientMixin(String string) { super(string); }
}
