package net.mortimer_kerman.defense.mixin.client;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.mortimer_kerman.defense.DefenseClient;
import net.mortimer_kerman.defense.RenderLayers;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity>
{
    @Shadow public abstract TextRenderer getTextRenderer();

    @Definition(id = "isSneaky", method = "Lnet/minecraft/entity/Entity;isSneaky()Z")
    @Definition(id = "entity", local = @Local(type = Entity.class, argsOnly = true))
    @Expression("entity.isSneaky()")
    @ModifyExpressionValue(method = "renderLabelIfPresent", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private boolean onSneakTest(boolean sneaky, @Local(type = Entity.class, argsOnly = true) Entity entity)
    {
        return sneaky || (entity instanceof PlayerEntity player && DefenseClient.isPlayerAfk(player.getUuid()));
    }

    @Inject(method = "renderLabelIfPresent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V", shift = At.Shift.BEFORE))
    private void renderDefenseIcon(T entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float tickDelta, CallbackInfo ci)
    {
        if(entity instanceof PlayerEntity player && DefenseClient.isPlayerImmune(player))
        {
            Matrix4f matrix4f = matrices.peek().getPositionMatrix();

            float scaleX = 8;
            float scaleY = 9;
            float offsetY = -0.5f;
            float offsetX = -getTextRenderer().getWidth(text)/2f - scaleX - 4;

            boolean sneaking = player.isSneaky() || DefenseClient.isPlayerAfk(player.getUuid());

            Identifier texture = DefenseClient.getPlayerIcon(player).getTexture(false);

            RenderLayer layer;

            if (sneaking) layer = RenderLayers.getIconTransparentDepth(texture);
            else layer = RenderLayers.getIconTransparentNoDepth(texture);

            displayDefenseIcon(vertexConsumers.getBuffer(layer), matrix4f, scaleX, scaleY, offsetY, offsetX, light);

            if(!sneaking)
            {
                layer = RenderLayers.getIconSolidDepth(texture);
                displayDefenseIcon(vertexConsumers.getBuffer(layer), matrix4f, scaleX, scaleY, offsetY, offsetX, light);
            }
        }
    }

    @Unique private static void displayDefenseIcon(VertexConsumer consumer, Matrix4f matrix4f, float scaleX, float scaleY, float offsetY, float offsetX, int light)
    {
        consumer.vertex(matrix4f, scaleX + offsetX,          offsetY, 0).color(1f, 1f, 1f, 0.3f).texture(0.0F, 0.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0,1, 0);
        consumer.vertex(matrix4f,          offsetX,          offsetY, 0).color(1f, 1f, 1f, 0.3f).texture(1.0F, 0.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0,1, 0);
        consumer.vertex(matrix4f,          offsetX, scaleY + offsetY, 0).color(1f, 1f, 1f, 0.3f).texture(1.0F, 1.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0,1, 0);
        consumer.vertex(matrix4f, scaleX + offsetX, scaleY + offsetY, 0).color(1f, 1f, 1f, 0.3f).texture(0.0F, 1.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0,1, 0);
    }
}