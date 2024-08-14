package net.mortimer_kerman.defense.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.mortimer_kerman.defense.Defense;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity
{
    @Inject(method = "damage", at = @At(value = "HEAD"), cancellable = true)
    private void onDamaged(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir)
    {
        if (!(source.getAttacker() instanceof PlayerEntity attacker)) return;

        if (attacker.getUuid().equals(this.getUuid())) return;

        if (getWorld().isClient) return;

        if (Defense.isPlayerImmune(this) || Defense.isPlayerImmune(attacker)) cir.setReturnValue(false);
    }

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) { super(world, pos, yaw, gameProfile); }
}
