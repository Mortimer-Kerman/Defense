package net.mortimer_kerman.defense.mixin.client;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;

import net.mortimer_kerman.defense.DefenseClient;

import org.jetbrains.annotations.Nullable;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin
{
    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/PlayerSkinDrawer;draw(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/util/Identifier;IIIZZI)V"))
    private boolean onRenderPlayerHead(DrawContext context, Identifier texture, int x, int y, int size, boolean hatVisible, boolean upsideDown, int color)
    {
        if (currentEntry == null || !DefenseClient.isPlayerAfk(currentEntry.getProfile().getId())) return true;
        PlayerSkinDrawer.draw(context, texture, x, y, size, hatVisible, upsideDown, color);
        context.fill(x, y, x + size, y + size, 0x80_00_00_00); //ARGB: a=128, r=0, g=0, b=0
        return false;
    }

    @Unique @Nullable
    private static PlayerListEntry currentEntry = null;

    @Unique
    private static boolean renderingScoreboard = true;

    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;"))
    private <E> E retrieveEntry(E obj)
    {
        if (obj instanceof PlayerListEntry entry) currentEntry = entry;
        return obj;
    }

    @Definition(id = "SPECTATOR", field = "Lnet/minecraft/world/GameMode;SPECTATOR:Lnet/minecraft/world/GameMode;")
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
        boolean isAfk = DefenseClient.isPlayerAfk(currentEntry.getProfile().getId());
        if(currentEntry.getGameMode() == GameMode.SPECTATOR)
        {
            if (isAfk) return 1358954495;
            return value;
        }
        if (isAfk) return value;
        return Colors.WHITE;
    }

    @Inject(method = "renderLatencyIcon", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V", shift = At.Shift.BEFORE))
    private void onRenderLatencyIcon(DrawContext context, int width, int x, int y, PlayerListEntry entry, CallbackInfo ci)
    {
        if (currentEntry == null || !DefenseClient.isPlayerImmune(currentEntry.getProfile().getId())) return;
        context.drawGuiTexture(RenderLayer::getGuiTextured, DefenseClient.getPlayerIcon(entry.getProfile().getId()).getTexture(true), x + width - 21, y, 8, 9);
    }

    @Definition(id = "min", method = "Ljava/lang/Math;min(II)I")
    @Expression("min(?, ? - 50) / ?")
    @ModifyExpressionValue(method = "render", at = @At("MIXINEXTRAS:EXPRESSION"))
    private int changeWidth(int m)
    {
        return m + 9;
    }
}
