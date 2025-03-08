package net.mortimer_kerman.defense.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.vehicle.VehicleEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.mortimer_kerman.defense.Defense;
import net.mortimer_kerman.defense.Gamerules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VehicleEntity.class)
public abstract class VehicleEntityMixin extends Entity
{
    @Inject(method = "damage", at = @At(value = "HEAD"), cancellable = true)
    private void onDamage(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir)
    {
        Entity attacker = source.getAttacker();

        if (attacker == null || world.isClient) return;

        if (attacker.getUuid().equals(this.getUuid())) return;

        boolean petsProtected = world.getGameRules().getBoolean(Gamerules.PETS_PROTECTED);
        boolean mountsProtected = world.getGameRules().getBoolean(Gamerules.MOUNTS_PROTECTED);

        boolean thisImmune = Defense.isEntityImmune(this, petsProtected, mountsProtected);
        boolean thisPlayerRelated = Defense.isPlayerRelated(this, petsProtected, mountsProtected);

        boolean attackerImmune = Defense.isEntityImmune(attacker, petsProtected, mountsProtected);
        boolean attackerPlayerRelated = Defense.isPlayerRelated(attacker, petsProtected, mountsProtected);

        if ((thisImmune && attackerPlayerRelated) || (thisPlayerRelated && attackerImmune)) cir.setReturnValue(false);
    }

    public VehicleEntityMixin(EntityType<?> type, World world) { super(type, world); }
}
