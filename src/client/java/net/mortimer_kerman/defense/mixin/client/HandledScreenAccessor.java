package net.mortimer_kerman.defense.mixin.client;

import net.minecraft.client.gui.screen.ingame.HandledScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HandledScreen.class)
public interface HandledScreenAccessor
{
    @Accessor int getX();
    @Accessor int getY();
}
