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
import net.mortimer_kerman.defense.Defense;
import net.mortimer_kerman.defense.DefenseClient;
import net.mortimer_kerman.defense.Payloads;
import net.mortimer_kerman.defense.interfaces.PlayerEntityAccess;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
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
        if (!(source.getAttacker() instanceof PlayerEntity)) return;

        if (getWorld().isClient)
        {
            if (DefenseClient.isPlayerImmune((PlayerEntity)(Object)this)) cir.setReturnValue(false);
        }
        else
        {
            if (Defense.isPlayerImmune((PlayerEntity)(Object)this)) cir.setReturnValue(false);
        }
    }

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void onUpdate(CallbackInfo ci)
    {
        if (!getWorld().isClient || !pvpOff) return;

        long time = getWorld().getTime();

        if (time > defenseEndTick) defense$switchPvp(false);
        else if (time == defenseEndTick - 1200L)
        {
            Text text = Texts.bracketed(Text.translatable("chat.immunity.continue")).styled((style) -> style.withColor(Formatting.GREEN).withClickEvent(new CRunnableClickEvent(() -> defense$switchPvp(true))).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("chat.immunity.continue"))));
            sendMessage(Text.translatable("chat.immunity.warn", text).styled((style) -> style.withColor(Formatting.YELLOW)));
        }

    }

    @Override
    public void defense$switchPvp(boolean pvpOff) {
        if (pvpOff) defenseEndTick = getWorld().getTime() + 24000L;
        if (this.pvpOff != pvpOff)
        {
            MinecraftClient.getInstance().execute(() -> ClientPlayNetworking.send(new Payloads.RecordPVPPayload(pvpOff)));
            this.pvpOff = pvpOff;
        }
        if (pvpOff) sendMessage(Text.translatable("chat.immunity.start").styled((style) -> style.withColor(Formatting.AQUA)));
        else sendMessage(Text.translatable("chat.immunity.end").styled((style) -> style.withColor(Formatting.RED)));
    }

    @Override
    public boolean defense$isPvpOff() {
        return pvpOff;
    }

    @Unique private long defenseEndTick = 0L;
    @Unique private boolean pvpOff = false;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) { super(entityType, world); }
}
