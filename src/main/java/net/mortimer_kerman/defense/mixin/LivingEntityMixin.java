package net.mortimer_kerman.defense.mixin;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import net.mortimer_kerman.defense.Defense;
import net.mortimer_kerman.defense.Gamerules;
import net.mortimer_kerman.defense.Payloads;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements Attackable
{
    @Inject(method = "damage", at = @At(value = "HEAD"), cancellable = true)
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir)
    {
        Entity attacker = source.getAttacker();
        World world = getWorld();

        if (attacker == null || world.isClient) return;

        if (attacker.getUuid().equals(this.getUuid())) return;

        boolean petsProtected = world.getGameRules().getBoolean(Gamerules.PETS_PROTECTED);
        boolean mountsProtected = world.getGameRules().getBoolean(Gamerules.MOUNTS_PROTECTED);

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

    @Inject(method = "canTarget(Lnet/minecraft/entity/LivingEntity;)Z", at = @At(value = "HEAD"), cancellable = true)
    private void onTarget(LivingEntity target, CallbackInfoReturnable<Boolean> cir)
    {
        World world = getWorld();
        MinecraftServer server = world.getServer();

        if (getWorld().isClient || server == null) return;

        if(!(this instanceof Tameable pet)) return;

        if (target == null || !(pet.getOwner() instanceof PlayerEntity owner) || !(target instanceof PlayerEntity targetPlayer)) return;

        if (world.getGameRules().getBoolean(Gamerules.PETS_PROTECTED) && (Defense.isPlayerImmune(owner) || Defense.isPlayerImmune(targetPlayer)))
        {
            cir.setReturnValue(false);
        }
    }

    public LivingEntityMixin(EntityType<?> type, World world) { super(type, world); }
}
