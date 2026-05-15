package net.mortimer_kerman.defense.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.world.inventory.RecipeBookMenu;

@Mixin(AbstractRecipeBookScreen.class)
public interface RecipeBookScreenAccessor<T extends RecipeBookMenu>
{
    @Accessor
    RecipeBookComponent<T> getRecipeBookComponent();
}
