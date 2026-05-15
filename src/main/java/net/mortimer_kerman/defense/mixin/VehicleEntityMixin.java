package net.mortimer_kerman.defense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.level.Level;

import net.mortimer_kerman.defense.Defense;
import net.mortimer_kerman.defense.Gamerules;

@Mixin(VehicleEntity.class)
public abstract class VehicleEntityMixin extends Entity
{
    @Inject(method = "hurtServer", at = @At(value = "HEAD"), cancellable = true)
    private void onDamage(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir)
    {
        Entity attacker = source.getEntity();

        if (attacker == null || world.isClientSide()) return;

        if (attacker.getUUID().equals(this.getUUID())) return;

        boolean petsProtected = world.getGameRules().get(Gamerules.PETS_PROTECTED);
        boolean mountsProtected = world.getGameRules().get(Gamerules.MOUNTS_PROTECTED);

        boolean thisImmune = Defense.isEntityImmune(this, petsProtected, mountsProtected);
        boolean thisPlayerRelated = Defense.isPlayerRelated(this, petsProtected, mountsProtected);

        boolean attackerImmune = Defense.isEntityImmune(attacker, petsProtected, mountsProtected);
        boolean attackerPlayerRelated = Defense.isPlayerRelated(attacker, petsProtected, mountsProtected);

        if ((thisImmune && attackerPlayerRelated) || (thisPlayerRelated && attackerImmune)) cir.setReturnValue(false);
    }

    public VehicleEntityMixin(EntityType<?> type, Level world) { super(type, world); }
}
