package net.mortimer_kerman.defense.mixin.client;

import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.RecipeBookScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;

import net.mortimer_kerman.defense.DefenseClient;
import net.mortimer_kerman.defense.DefenseToggleWidget;
import net.mortimer_kerman.defense.interfaces.PlayerEntityAccess;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends RecipeBookScreen<PlayerScreenHandler>
{
    @Shadow private boolean mouseDown;

    @Inject(method = "init", at = @At(value = "TAIL"))
    private void onInit(CallbackInfo ci)
    {
        if (client == null || client.interactionManager == null) return;
        if (client.interactionManager.hasCreativeInventory()) return;
        addDrawableChild(new DefenseToggleWidget(this.x + 150, this.height / 2 - 22, ((RecipeBookScreenAccessor)this).getRecipeBook(), this, (button) -> {
            PlayerEntityAccess plr = (PlayerEntityAccess)this.client.player;
            mouseDown = true;
            if (plr == null) return;
            if (DefenseClient.getDefenseDurationMinutes() != 0) plr.defense$switchPvp(!DefenseClient.pvpOff);
            else if (DefenseClient.pvpOff) plr.defense$switchPvp(false);
        }));
    }

    public InventoryScreenMixin(PlayerScreenHandler handler, RecipeBookWidget<?> recipeBook, PlayerInventory inventory, Text title) { super(handler, recipeBook, inventory, title); }
}
