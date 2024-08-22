package net.mortimer_kerman.defense.mixin.client;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

import net.mortimer_kerman.defense.CRunnableClickEvent;

import net.mortimer_kerman.defense.DefenseClient;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin
{
    @Shadow @Nullable protected MinecraftClient client;

    @WrapWithCondition(method = "handleTextClick", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;)V", remap = false))
    private boolean execute(Logger instance, String s, Object o)
    {
        if (!s.equals("Don't know how to handle {}")) return true;

        if (o instanceof CRunnableClickEvent event)
        {
            event.execute();
            return false;
        }

        return true;
    }

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void onTick(CallbackInfo ci)
    {
        if (client == null) return;

        double posX = client.mouse.getX();
        double posY = client.mouse.getY();

        if (mouseX != posX || mouseY != posY)
        {
            DefenseClient.requestAfkUpdate();
            mouseX = posX;
            mouseY = posY;
        }
        else if(client.mouse.wasLeftButtonClicked() || client.mouse.wasMiddleButtonClicked() || client.mouse.wasRightButtonClicked()) DefenseClient.requestAfkUpdate();
    }

    @Unique private static double mouseX = 0;
    @Unique private static double mouseY = 0;
}
