package net.mortimer_kerman.defense.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

import net.mortimer_kerman.defense.Defense;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity
{
    @Inject(method = "damage", at = @At(value = "HEAD"), cancellable = true)
    private void onDamaged(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir)
    {
        if (!(source.getAttacker() instanceof PlayerEntity attacker)) return;

        if (attacker.getUuid().equals(this.getUuid())) return;

        if (getWorld().isClient) return;

        if (Defense.isPlayerImmune((PlayerEntity)(Object)this) || Defense.isPlayerImmune(attacker)) cir.setReturnValue(false);
    }

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) { super(entityType, world); }
}
