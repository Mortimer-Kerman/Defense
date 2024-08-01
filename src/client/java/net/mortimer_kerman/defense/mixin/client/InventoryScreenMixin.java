package net.mortimer_kerman.defense.mixin.client;

import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;

import net.mortimer_kerman.defense.DefenseToggleWidget;
import net.mortimer_kerman.defense.interfaces.PlayerEntityAccess;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractInventoryScreen<PlayerScreenHandler> implements RecipeBookProvider
{
    @Shadow private boolean mouseDown;

    @Shadow @Final private RecipeBookWidget recipeBook;

    @Inject(method = "init", at = @At(value = "TAIL"))
    private void onInit(CallbackInfo ci)
    {
        if (client.interactionManager.hasCreativeInventory()) return;
        addDrawableChild(new DefenseToggleWidget(this.x + 150, this.height / 2 - 22, this.recipeBook, this, (button) -> {
            PlayerEntityAccess plr = (PlayerEntityAccess)this.client.player;
            plr.defense$switchPvp(!plr.defense$isPvpOff());
            mouseDown = true;
        }));
    }

    public InventoryScreenMixin(PlayerScreenHandler screenHandler, PlayerInventory playerInventory, Text text) { super(screenHandler, playerInventory, text); }
}
