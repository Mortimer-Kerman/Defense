package net.mortimer_kerman.defense.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import com.mojang.authlib.GameProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import net.mortimer_kerman.defense.Defense;
import net.mortimer_kerman.defense.DefenseClient;
import net.mortimer_kerman.defense.Payloads;
import net.mortimer_kerman.defense.interfaces.PlayerEntityAccess;

@Mixin(LocalPlayer.class)
public abstract class ClientPlayerEntityMixin extends Player implements PlayerEntityAccess
{
    @Shadow @Final protected Minecraft minecraft;

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void onUpdate(CallbackInfo ci)
    {
        if (!level().isClientSide()) return;

        if (DefenseClient.afkUpdateRequested()) DefenseClient.requestImmediateAfkUpdate();

        if (!DefenseClient.pvpOff) DefenseClient.durationChange = 0;
        if (!DefenseClient.pvpOff || !Minecraft.getInstance().player.getUUID().equals(this.getUUID())) return;

        if (DefenseClient.isAfk) return;

        int durationMinutes = DefenseClient.getDefenseDurationMinutes();
        long durationTicks = DefenseClient.getDefenseDurationTicks();

        long time = level().getGameTime();

        if (DefenseClient.durationChange != 0)
        {
            Component text = DefenseClient.getDefenseContinueText(this);
            long leftTimeTick = (durationTicks - (time - DefenseClient.defenseStartTick));
            int leftTimeMinutes = Mth.floor(leftTimeTick/1200D);
            Component leftTime = Defense.getMinutesText(leftTimeMinutes);

            if (DefenseClient.durationChange < 0)
            {
                if(leftTimeTick > 0)
                {
                    displayClientMessage(Component.translatable("chat.immunity.change.shorter", leftTime, text).withStyle(style -> style.withColor(ChatFormatting.YELLOW)), false);
                }
                else
                {
                    displayClientMessage(Component.translatable("chat.immunity.change.stop", text).withStyle(style -> style.withColor(ChatFormatting.RED)), false);
                }
            }
            else if (DefenseClient.durationChange > 0)
            {
                displayClientMessage(Component.translatable("chat.immunity.change.longer", leftTime).withStyle(style -> style.withColor(ChatFormatting.AQUA)), false);
            }

            DefenseClient.durationChange = 0;
        }

        if (time > DefenseClient.defenseStartTick + durationTicks) defense$switchPvp(false);
        else if (durationMinutes != 1 && time == DefenseClient.defenseStartTick + durationTicks - 1200L)
        {
            Component text = DefenseClient.getDefenseContinueText(this);
            displayClientMessage(Component.translatable("chat.immunity.warn", text).withStyle(style -> style.withColor(ChatFormatting.YELLOW)), false);
        }
    }

    @Override
    public void defense$switchPvp(boolean pvpOff)
    {
        int durationMinutes = DefenseClient.getDefenseDurationMinutes();

        if (pvpOff) DefenseClient.defenseStartTick = level().getGameTime();
        if (DefenseClient.pvpOff != pvpOff)
        {
            Minecraft.getInstance().execute(() -> ClientPlayNetworking.send(new Payloads.RecordPVPPayload(pvpOff)));
            DefenseClient.pvpOff = pvpOff;
        }

        if (!DefenseClient.isAfk)
        {
            if (pvpOff) displayClientMessage(Component.translatable("chat.immunity.start", Defense.getMinutesText(durationMinutes)).withStyle((style) -> style.withColor(ChatFormatting.AQUA)), false);
            else displayClientMessage(Component.translatable("chat.immunity.end").withStyle((style) -> style.withColor(ChatFormatting.RED)), false);
        }
    }

    public ClientPlayerEntityMixin(Level world, GameProfile profile) { super(world, profile); }
}
