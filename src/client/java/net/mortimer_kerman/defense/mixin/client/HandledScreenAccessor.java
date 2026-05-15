package net.mortimer_kerman.defense.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

@Mixin(AbstractContainerScreen.class)
public interface HandledScreenAccessor
{
    @Accessor int getLeftPos();
    @Accessor int getTopPos();
}
