package net.mortimer_kerman.defense.mixin.client;

import net.minecraft.client.option.GameOptions;

import net.mortimer_kerman.defense.DefenseClient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameOptions.class)
public abstract class GameOptionsMixin
{
    @Inject(method = "accept", at = @At(value = "TAIL"))
    private void onAccept(GameOptions.Visitor visitor, CallbackInfo ci)
    {
        visitor.accept("defenseIcon", DefenseClient.getDefenseIconOption());
    }
}
