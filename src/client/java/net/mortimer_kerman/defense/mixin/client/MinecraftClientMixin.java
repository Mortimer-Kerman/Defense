package net.mortimer_kerman.defense.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.WindowEventHandler;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import net.mortimer_kerman.defense.DefenseClient;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin extends ReentrantThreadExecutor<Runnable> implements WindowEventHandler
{
    @Shadow @Nullable public Screen currentScreen;

    @Shadow private boolean disconnecting;

    @Inject(method = "setScreen", at = @At(value = "HEAD"))
    private void onSetScreen(@Nullable Screen screen, CallbackInfo ci)
    {
        if (currentScreen instanceof GameMenuScreen && screen == null && !disconnecting) DefenseClient.tryRecordIconChange();
    }

    public MinecraftClientMixin(String string) { super(string); }
}
