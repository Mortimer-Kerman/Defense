package net.mortimer_kerman.defense.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.joml.Matrix4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import net.mortimer_kerman.defense.DefenseClient;
import net.mortimer_kerman.defense.interfaces.EntityRenderStateMixinAccess;
import net.mortimer_kerman.defense.render.DefenseRenderLayers;

@Mixin(AvatarRenderer.class)
public abstract class PlayerEntityRendererMixin<T extends Entity, S extends EntityRenderState>
{
    @WrapOperation(method = "submitNameTag(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;isDiscrete:Z", opcode = Opcodes.GETFIELD))
    private boolean onSneakTest(AvatarRenderState state, Operation<Boolean> original)
    {
        EntityRenderStateMixinAccess stateAccess = (EntityRenderStateMixinAccess)state;

        return original.call(state) || stateAccess.defense$isAfk();
    }

    @Inject(method = "submitNameTag(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitNameTag(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZIDLnet/minecraft/client/renderer/state/CameraRenderState;)V"))
    private void onSubmitLabel(AvatarRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState, CallbackInfo ci) {
        EntityRenderStateMixinAccess stateAccess = (EntityRenderStateMixinAccess)state;

        if(stateAccess.defense$isImmune())
        {
            Minecraft minecraftClient = Minecraft.getInstance();
            matrices.pushPose();
            matrices.translate(state.nameTagAttachment.x, state.nameTagAttachment.y + 0.5, state.nameTagAttachment.z);
            matrices.mulPose(cameraState.orientation);
            matrices.scale(0.025F, -0.025F, 0.025F);
            Matrix4f matrix4f = new Matrix4f(matrices.last().pose());

            int light = state.lightCoords;
            int textWidth = minecraftClient.font.width(state.nameTag);
            Identifier texture = stateAccess.defense$getDefenseTexture();

            if(!state.isDiscrete && !stateAccess.defense$isAfk())
            {
                queue.submitCustomGeometry(matrices, DefenseRenderLayers.getIconSolidNoDepth(texture),
                        getIconRenderer(matrix4f, textWidth, LightTexture.lightCoordsWithEmission(light, 2), -1));
                queue.submitCustomGeometry(matrices, DefenseRenderLayers.getIconTransparentDepth(texture),
                        getIconRenderer(matrix4f, textWidth, light, -2130706433));

            }
            else {
                queue.submitCustomGeometry(matrices, DefenseRenderLayers.getIconTransparentDepth(texture),
                        getIconRenderer(matrix4f, textWidth, light, -2130706433));
            }

            matrices.popPose();
        }
    }

    private SubmitNodeCollector.CustomGeometryRenderer getIconRenderer(Matrix4f matrix4f, int textWidth, int light, int color) {
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
        consumer.addVertex(matrix4f, scaleX + offsetX,          offsetY, 0).setColor(color).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0,1, 0);
        consumer.addVertex(matrix4f,          offsetX,          offsetY, 0).setColor(color).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0,1, 0);
        consumer.addVertex(matrix4f,          offsetX, scaleY + offsetY, 0).setColor(color).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0,1, 0);
        consumer.addVertex(matrix4f, scaleX + offsetX, scaleY + offsetY, 0).setColor(color).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0,1, 0);
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("HEAD"))
    private void onUpdateRenderState(Avatar entity, AvatarRenderState state, float f, CallbackInfo ci)
    {
        EntityRenderStateMixinAccess stateAccess = (EntityRenderStateMixinAccess)state;

        if (entity instanceof Player player) {
            stateAccess.defense$setData(
                    DefenseClient.getPlayerIcon(player).getTexture(false),
                    DefenseClient.isPlayerImmune(player),
                    DefenseClient.isPlayerAfk(player.getUUID()));
        }
        else stateAccess.defense$setData(null, false, false);
    }
}