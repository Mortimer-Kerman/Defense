package net.mortimer_kerman.defense.mixin.client;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import net.mortimer_kerman.defense.*;
import net.mortimer_kerman.defense.interfaces.PlayerEntityAccess;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends PlayerEntity implements PlayerEntityAccess
{
    @Shadow @Final protected MinecraftClient client;

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void onUpdate(CallbackInfo ci)
    {
        if (!getWorld().isClient) return;

        if (DefenseClient.afkUpdateRequested()) DefenseClient.requestImmediateAfkUpdate();

        if (!DefenseClient.pvpOff) DefenseClient.durationChange = 0;
        if (!DefenseClient.pvpOff || !MinecraftClient.getInstance().player.getUuid().equals(this.getUuid())) return;

        if (DefenseClient.isAfk) return;

        int durationMinutes = DefenseClient.getDefenseDurationMinutes();
        long durationTicks = DefenseClient.getDefenseDurationTicks();

        long time = getWorld().getTime();

        if (DefenseClient.durationChange != 0)
        {
            Text text = DefenseClient.getDefenseContinueText(this);
            long leftTimeTick = (durationTicks - (time - DefenseClient.defenseStartTick));
            int leftTimeMinutes = MathHelper.floor(leftTimeTick/1200D);
            Text leftTime = Defense.getMinutesText(leftTimeMinutes);

            if (DefenseClient.durationChange < 0)
            {
                if(leftTimeTick > 0)
                {
                    sendMessage(Text.translatable("chat.immunity.change.shorter", leftTime, text).styled(style -> style.withColor(Formatting.YELLOW)), false);
                }
                else
                {
                    sendMessage(Text.translatable("chat.immunity.change.stop", text).styled(style -> style.withColor(Formatting.RED)), false);
                }
            }
            else if (DefenseClient.durationChange > 0)
            {
                sendMessage(Text.translatable("chat.immunity.change.longer", leftTime).styled(style -> style.withColor(Formatting.AQUA)), false);
            }

            DefenseClient.durationChange = 0;
        }

        if (time > DefenseClient.defenseStartTick + durationTicks) defense$switchPvp(false);
        else if (durationMinutes != 1 && time == DefenseClient.defenseStartTick + durationTicks - 1200L)
        {
            Text text = DefenseClient.getDefenseContinueText(this);
            sendMessage(Text.translatable("chat.immunity.warn", text).styled(style -> style.withColor(Formatting.YELLOW)), false);
        }
    }

    @Override
    public void defense$switchPvp(boolean pvpOff)
    {
        int durationMinutes = DefenseClient.getDefenseDurationMinutes();

        if (pvpOff) DefenseClient.defenseStartTick = getWorld().getTime();
        if (DefenseClient.pvpOff != pvpOff)
        {
            MinecraftClient.getInstance().execute(() -> ClientPlayNetworking.send(new Payloads.RecordPVPPayload(pvpOff)));
            DefenseClient.pvpOff = pvpOff;
        }

        if (!DefenseClient.isAfk)
        {
            if (pvpOff) sendMessage(Text.translatable("chat.immunity.start", Defense.getMinutesText(durationMinutes)).styled((style) -> style.withColor(Formatting.AQUA)), false);
            else sendMessage(Text.translatable("chat.immunity.end").styled((style) -> style.withColor(Formatting.RED)), false);
        }
    }

    public ClientPlayerEntityMixin(World world, GameProfile profile) { super(world, profile); }
}
