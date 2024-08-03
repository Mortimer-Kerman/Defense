package net.mortimer_kerman.defense.mixin.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import net.mortimer_kerman.defense.CRunnableClickEvent;
import net.mortimer_kerman.defense.DefenseClient;
import net.mortimer_kerman.defense.Payloads;
import net.mortimer_kerman.defense.interfaces.PlayerEntityAccess;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements PlayerEntityAccess
{
    @Inject(method = "damage", at = @At(value = "HEAD"), cancellable = true)
    private void onDamaged(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir)
    {
        if (!(source.getAttacker() instanceof PlayerEntity attacker)) return;

        if (attacker.getUuid().equals(this.getUuid())) return;

        if (!getWorld().isClient) return;

        if (DefenseClient.isPlayerImmune((PlayerEntity)(Object)this) || DefenseClient.isPlayerImmune(attacker)) cir.setReturnValue(false);
    }

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void onUpdate(CallbackInfo ci)
    {
        if (!getWorld().isClient || !DefenseClient.pvpOff) return;

        long time = getWorld().getTime();

        if (time > DefenseClient.defenseEndTick) defense$switchPvp(false);
        else if (time == DefenseClient.defenseEndTick - 1200L)
        {
            Text text = Texts.bracketed(Text.translatable("chat.immunity.continue")).styled((style) -> style.withColor(Formatting.GREEN).withClickEvent(new CRunnableClickEvent(() -> defense$switchPvp(true))).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("chat.immunity.continue"))));
            sendMessage(Text.translatable("chat.immunity.warn", text).styled((style) -> style.withColor(Formatting.YELLOW)));
        }

    }

    @Override
    public void defense$switchPvp(boolean pvpOff) {
        if (pvpOff) DefenseClient.defenseEndTick = getWorld().getTime() + 24000L;
        if (DefenseClient.pvpOff != pvpOff)
        {
            MinecraftClient.getInstance().execute(() -> ClientPlayNetworking.send(new Payloads.RecordPVPPayload(pvpOff)));
            DefenseClient.pvpOff = pvpOff;
        }
        if (pvpOff) sendMessage(Text.translatable("chat.immunity.start").styled((style) -> style.withColor(Formatting.AQUA)));
        else sendMessage(Text.translatable("chat.immunity.end").styled((style) -> style.withColor(Formatting.RED)));
    }

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) { super(entityType, world); }
}
