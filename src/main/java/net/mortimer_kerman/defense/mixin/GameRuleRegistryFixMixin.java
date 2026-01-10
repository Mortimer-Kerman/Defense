package net.mortimer_kerman.defense.mixin;

import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.fix.GameRuleRegistryFix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRuleRegistryFix.class)
public class GameRuleRegistryFixMixin {

    @Shadow private static Dynamic<?> isTrue(Dynamic<?> dynamic) { return null; }
    @Shadow private static Dynamic<?> clamp(Dynamic<?> dynamic, int min) { return null; }

    @Inject(method = "method_76071", at = @At("RETURN"), cancellable = true)
    private static void onFixingGamerules(Dynamic<?> dynamic, CallbackInfoReturnable<Dynamic<?>> cir) {
        cir.setReturnValue(
                cir.getReturnValue()
                        .renameAndFixField("petsProtected", "defense:pets_protected", GameRuleRegistryFixMixin::isTrue)
                        .renameAndFixField("mountsProtected", "defense:mounts_protected", GameRuleRegistryFixMixin::isTrue)
                        .renameAndFixField("afkTimerSeconds", "defense:afk_timer_seconds", dynamicxx -> clamp(dynamicxx, 0))
                        .renameAndFixField("defenseDurationMinutes", "defense:defense_duration_minutes", dynamicxx -> clamp(dynamicxx, 0))
                        .renameAndFixField("allowDefenseKeybind", "defense:allow_defense_keybind", GameRuleRegistryFixMixin::isTrue)
        );
    }

}
