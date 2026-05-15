package net.mortimer_kerman.defense.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Options;

import net.mortimer_kerman.defense.DefenseClient;

@Mixin(Options.class)
public abstract class GameOptionsMixin
{
    @Inject(method = "processOptions", at = @At(value = "TAIL"))
    private void onAccept(Options.FieldAccess visitor, CallbackInfo ci)
    {
        visitor.process("defenseIcon", DefenseClient.getDefenseIconOption());
    }
}
