package net.mortimer_kerman.defense.mixin.client;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import net.minecraft.client.gui.screen.Screen;

import net.mortimer_kerman.defense.CRunnableClickEvent;

import org.slf4j.Logger;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Screen.class)
public abstract class ScreenMixin
{
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
}
