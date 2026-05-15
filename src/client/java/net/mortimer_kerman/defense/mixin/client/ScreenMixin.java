package net.mortimer_kerman.defense.mixin.client;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.screens.Screen;

import net.mortimer_kerman.defense.AFKDefenseScreen;
import net.mortimer_kerman.defense.CRunnableClickEvent;
import net.mortimer_kerman.defense.DefenseClient;

@Mixin(Screen.class)
public abstract class ScreenMixin extends AbstractContainerEventHandler implements Renderable
{
    @Shadow @Final protected Minecraft minecraft;

    @WrapWithCondition(method = "defaultHandleClickEvent", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;)V", remap = false))
    private static boolean execute(Logger instance, String s, Object o)
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
        if (minecraft == null) return;

        if ((Object)this instanceof AFKDefenseScreen) return;

        double posX = minecraft.mouseHandler.xpos();
        double posY = minecraft.mouseHandler.ypos();

        if (mouseX != posX || mouseY != posY)
        {
            DefenseClient.requestAfkUpdate();
            mouseX = posX;
            mouseY = posY;
        }
        else if(minecraft.mouseHandler.isLeftPressed() || minecraft.mouseHandler.isMiddlePressed() || minecraft.mouseHandler.isRightPressed()) DefenseClient.requestAfkUpdate();
    }

    @Unique private static double mouseX = 0;
    @Unique private static double mouseY = 0;
}
