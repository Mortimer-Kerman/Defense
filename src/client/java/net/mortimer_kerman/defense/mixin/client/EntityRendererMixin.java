package net.mortimer_kerman.defense.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.mortimer_kerman.defense.DefenseClient;
import net.mortimer_kerman.defense.render.DefenseRenderLayers;
import net.mortimer_kerman.defense.interfaces.EntityRenderStateMixinAccess;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity, S extends EntityRenderState>
{
    @Shadow public abstract TextRenderer getTextRenderer();

    @WrapOperation(method = "renderLabelIfPresent", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/entity/state/EntityRenderState;sneaking:Z"))
    private boolean onSneakTest(EntityRenderState state, Operation<Boolean> original)
    {
        EntityRenderStateMixinAccess stateAccess = (EntityRenderStateMixinAccess)state;

        return original.call(state) || stateAccess.defense$isAfk();
    }

    @Inject(method = "renderLabelIfPresent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"))
    private void renderDefenseIcon(S state, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci)
    {
        EntityRenderStateMixinAccess stateAccess = (EntityRenderStateMixinAccess)state;

        if(stateAccess.defense$isImmune())
        {
            Matrix4f matrix4f = matrices.peek().getPositionMatrix();

            float scaleX = 8;
            float scaleY = 9;
            float offsetY = -0.5f;
            float offsetX = -getTextRenderer().getWidth(text)/2f - scaleX - 4;

            boolean sneaking = state.sneaking || stateAccess.defense$isAfk();

            Identifier texture = stateAccess.defense$getDefenseTexture();

            RenderLayer layer;

            if (sneaking) layer = DefenseRenderLayers.getIconTransparentDepth(texture);
            else layer = DefenseRenderLayers.getIconTransparentNoDepth(texture);

            displayDefenseIcon(vertexConsumers.getBuffer(layer), matrix4f, scaleX, scaleY, offsetY, offsetX, light);

            if(!sneaking)
            {
                layer = DefenseRenderLayers.getIconSolidDepth(texture);
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

    @Inject(method = "updateRenderState", at = @At("HEAD"))
    private void onUpdateRenderState(T entity, S state, float tickDelta, CallbackInfo ci)
    {
        EntityRenderStateMixinAccess stateAccess = (EntityRenderStateMixinAccess)state;

        if (entity instanceof PlayerEntity player) {
            stateAccess.defense$setData(
                    DefenseClient.getPlayerIcon(player).getTexture(false),
                    DefenseClient.isPlayerImmune(player),
                    DefenseClient.isPlayerAfk(player.getUuid()));
        }
        else stateAccess.defense$setData(null, false, false);
    }
}