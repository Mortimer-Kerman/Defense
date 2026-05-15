package net.mortimer_kerman.defense.mixin.client;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;

import net.mortimer_kerman.defense.DefenseClient;
import net.mortimer_kerman.defense.DefenseToggleWidget;
import net.mortimer_kerman.defense.interfaces.PlayerEntityAccess;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractRecipeBookScreen<@NotNull InventoryMenu>
{
    @Inject(method = "init", at = @At(value = "TAIL"))
    private void onInit(CallbackInfo ci)
    {
        if (minecraft.gameMode == null) return;
        if (minecraft.gameMode.getPlayerMode().isCreative()) return;
        addRenderableWidget(new DefenseToggleWidget(this.leftPos + 150, this.height / 2 - 22, ((RecipeBookScreenAccessor<@NotNull InventoryMenu>)this).getRecipeBookComponent(), this, (button) -> {
            PlayerEntityAccess plr = (PlayerEntityAccess)this.minecraft.player;
            if (plr == null) return;
            if (DefenseClient.getDefenseDurationMinutes() != 0) plr.defense$switchPvp(!DefenseClient.pvpOff);
            else if (DefenseClient.pvpOff) plr.defense$switchPvp(false);
        }));
    }

    public InventoryScreenMixin(InventoryMenu handler, RecipeBookComponent<?> recipeBook, Inventory inventory, Component title) { super(handler, recipeBook, inventory, title); }
}
