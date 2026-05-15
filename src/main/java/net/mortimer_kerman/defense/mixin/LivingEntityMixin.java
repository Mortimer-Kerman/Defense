package net.mortimer_kerman.defense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import net.mortimer_kerman.defense.Defense;
import net.mortimer_kerman.defense.Gamerules;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements Attackable
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

        /*System.out.println("-----DEFENSE RESULT-----");
        System.out.println("pets protected: " + petsProtected);
        System.out.println("mounts protected: " + mountsProtected);
        System.out.println("target immune: " + thisImmune);
        System.out.println("target player related: " + thisPlayerRelated);
        System.out.println("attacker immune: " + attackerImmune);
        System.out.println("attacker player related: " + attackerPlayerRelated);
        System.out.println("------");*/

        if ((thisImmune && attackerPlayerRelated) || (thisPlayerRelated && attackerImmune)) cir.setReturnValue(false);
    }

    @Inject(method = "canAttack(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At(value = "HEAD"), cancellable = true)
    private void onTarget(LivingEntity target, CallbackInfoReturnable<Boolean> cir)
    {
        Level world = level();
        MinecraftServer server = world.getServer();

        if (world.isClientSide() || server == null) return;

        ServerLevel serverWorld = (ServerLevel)world;

        if(!(this instanceof OwnableEntity pet)) return;

        if (target == null || !(pet.getOwner() instanceof Player owner) || !(target instanceof Player targetPlayer)) return;

        if (serverWorld.getGameRules().get(Gamerules.PETS_PROTECTED) && (Defense.isPlayerImmune(owner) || Defense.isPlayerImmune(targetPlayer)))
        {
            cir.setReturnValue(false);
        }
    }

    public LivingEntityMixin(EntityType<?> type, Level world) { super(type, world); }
}
