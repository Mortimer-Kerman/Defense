package net.mortimer_kerman.defense.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.PlayerLikeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.mortimer_kerman.defense.DefenseClient;
import net.mortimer_kerman.defense.interfaces.EntityRenderStateMixinAccess;
import net.mortimer_kerman.defense.render.DefenseRenderLayers;
import org.joml.Matrix4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin<T extends Entity, S extends EntityRenderState>
{
    @WrapOperation(method = "renderLabelIfPresent(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;sneaking:Z", opcode = Opcodes.GETFIELD))
    private boolean onSneakTest(PlayerEntityRenderState state, Operation<Boolean> original)
    {
        EntityRenderStateMixinAccess stateAccess = (EntityRenderStateMixinAccess)state;

        return original.call(state) || stateAccess.defense$isAfk();
    }

    @Inject(method = "renderLabelIfPresent(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;submitLabel(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/math/Vec3d;ILnet/minecraft/text/Text;ZIDLnet/minecraft/client/render/state/CameraRenderState;)V"))
    private void onSubmitLabel(PlayerEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState, CallbackInfo ci) {
        EntityRenderStateMixinAccess stateAccess = (EntityRenderStateMixinAccess)state;

        if(stateAccess.defense$isImmune())
        {
            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            matrices.push();
            matrices.translate(state.nameLabelPos.x, state.nameLabelPos.y + 0.5, state.nameLabelPos.z);
            matrices.multiply(cameraState.orientation);
            matrices.scale(0.025F, -0.025F, 0.025F);
            Matrix4f matrix4f = new Matrix4f(matrices.peek().getPositionMatrix());

            int light = state.light;
            int textWidth = minecraftClient.textRenderer.getWidth(state.displayName);
            Identifier texture = stateAccess.defense$getDefenseTexture();

            if(!state.sneaking && !stateAccess.defense$isAfk())
            {
                queue.submitCustom(matrices, DefenseRenderLayers.getIconSolidNoDepth(texture),
                        getIconRenderer(matrix4f, textWidth, LightmapTextureManager.applyEmission(light, 2), -1));
                queue.submitCustom(matrices, DefenseRenderLayers.getIconTransparentDepth(texture),
                        getIconRenderer(matrix4f, textWidth, light, -2130706433));

            }
            else {
                queue.submitCustom(matrices, DefenseRenderLayers.getIconTransparentDepth(texture),
                        getIconRenderer(matrix4f, textWidth, light, -2130706433));
            }

            matrices.pop();
        }
    }

    private OrderedRenderCommandQueue.Custom getIconRenderer(Matrix4f matrix4f, int textWidth, int light, int color) {
        return (matricesEntry, vertexConsumer) -> this.renderDefenseIcon(matrix4f, vertexConsumer, textWidth, light, color);
    }

    @Unique
    private void renderDefenseIcon(Matrix4f matrix4f, VertexConsumer vertexConsumer, int textWidth, int light, int color) {

        float scaleX = 8;
        float scaleY = 9;
        float offsetY = -0.5f;
        float offsetX = -textWidth/2f - scaleX - 4;

        displayDefenseIcon(vertexConsumer, matrix4f, scaleX, scaleY, offsetY, offsetX, light, color);
    }

    @Unique private static void displayDefenseIcon(VertexConsumer consumer, Matrix4f matrix4f, float scaleX, float scaleY, float offsetY, float offsetX, int light, int color)
    {
        consumer.vertex(matrix4f, scaleX + offsetX,          offsetY, 0).color(color).texture(0.0F, 0.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0,1, 0);
        consumer.vertex(matrix4f,          offsetX,          offsetY, 0).color(color).texture(1.0F, 0.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0,1, 0);
        consumer.vertex(matrix4f,          offsetX, scaleY + offsetY, 0).color(color).texture(1.0F, 1.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0,1, 0);
        consumer.vertex(matrix4f, scaleX + offsetX, scaleY + offsetY, 0).color(color).texture(0.0F, 1.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0,1, 0);
    }

    @Inject(method = "updateRenderState(Lnet/minecraft/entity/PlayerLikeEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V", at = @At("HEAD"))
    private void onUpdateRenderState(PlayerLikeEntity entity, PlayerEntityRenderState state, float f, CallbackInfo ci)
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