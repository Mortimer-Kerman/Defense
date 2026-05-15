package net.mortimer_kerman.defense.mixin.client;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.world.level.GameType;

import net.mortimer_kerman.defense.DefenseClient;

@Mixin(PlayerTabOverlay.class)
public class PlayerListHudMixin
{
    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/PlayerFaceRenderer;draw(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/resources/Identifier;IIIZZI)V"))
    private boolean onRenderPlayerHead(GuiGraphics context, Identifier texture, int x, int y, int size, boolean hatVisible, boolean upsideDown, int color)
    {
        if (currentEntry == null || !DefenseClient.isPlayerAfk(currentEntry.getProfile().id())) return true;
        PlayerFaceRenderer.draw(context, texture, x, y, size, hatVisible, upsideDown, color);
        context.fill(x, y, x + size, y + size, 0x80_00_00_00); //ARGB: a=128, r=0, g=0, b=0
        return false;
    }

    @Unique @org.jspecify.annotations.Nullable
    private static PlayerInfo currentEntry = null;

    @Unique
    private static boolean renderingScoreboard = true;

    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;"))
    private <E> E retrieveEntry(E obj)
    {
        if (obj instanceof PlayerInfo entry) currentEntry = entry;
        return obj;
    }

    @Definition(id = "SPECTATOR", field = "Lnet/minecraft/world/level/GameType;SPECTATOR:Lnet/minecraft/world/level/GameType;")
    @Expression("? == SPECTATOR")
    @ModifyExpressionValue(method = "render", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean changeNameColorWhite(boolean spectatorMode)
    {
        renderingScoreboard = !renderingScoreboard;

        if (renderingScoreboard) return spectatorMode;

        if (currentEntry != null) return true;
        return spectatorMode;
    }

    @ModifyConstant(method = "render", constant = @Constant(intValue = -1862270977))
    private int changeNameColorGrey(int value)
    {
        if (currentEntry == null) return value;
        boolean isAfk = DefenseClient.isPlayerAfk(currentEntry.getProfile().id());
        if(currentEntry.getGameMode() == GameType.SPECTATOR)
        {
            if (isAfk) return 1358954495;
            return value;
        }
        if (isAfk) return value;
        return CommonColors.WHITE;
    }

    @Inject(method = "renderPingIcon", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V", shift = At.Shift.AFTER))
    private void onRenderLatencyIcon(GuiGraphics context, int width, int x, int y, PlayerInfo entry, CallbackInfo ci)
    {
        if (currentEntry == null || !DefenseClient.isPlayerImmune(currentEntry.getProfile().id())) return;
        context.blitSprite(RenderPipelines.GUI_TEXTURED, DefenseClient.getPlayerIcon(entry.getProfile().id()).getTexture(true), x + width - 21, y, 8, 9);
    }

    @Definition(id = "min", method = "Ljava/lang/Math;min(II)I")
    @Expression("min(?, ? - 50) / ?")
    @ModifyExpressionValue(method = "render", at = @At("MIXINEXTRAS:EXPRESSION"))
    private int changeWidth(int m)
    {
        return m + 9;
    }
}
