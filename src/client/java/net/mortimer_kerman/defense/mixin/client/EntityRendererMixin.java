package net.mortimer_kerman.defense.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import net.minecraft.util.Identifier;
import net.mortimer_kerman.defense.Defense;
import net.mortimer_kerman.defense.DefenseClient;

import org.joml.Matrix4f;

import org.lwjgl.opengl.GL11;
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

            boolean sneaking = player.isSneaky();

            RenderSystem.setShaderTexture(0, DefenseClient.getPlayerIcon(player).getTexture(false));
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);

            RenderSystem.setShaderColor(1, 1, 1, 0.5f);

            if(sneaking) RenderSystem.enableBlend();

            RenderSystem.enableDepthTest();
            displayDefenseIcon(matrix4f, scaleX, scaleY, offsetY, offsetX);
            RenderSystem.disableDepthTest();

            if(!sneaking)
            {
                RenderSystem.enableBlend();
                displayDefenseIcon(matrix4f, scaleX, scaleY, offsetY, offsetX);
            }

            RenderSystem.disableBlend();

            RenderSystem.setShaderColor(1, 1, 1, 1);
        }
    }

    @Unique private static void displayDefenseIcon(Matrix4f matrix4f, float scaleX, float scaleY, float offsetY, float offsetX)
    {
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(matrix4f, scaleX + offsetX, offsetY, 0).texture(0.0F, 0.0F);
        bufferBuilder.vertex(matrix4f, offsetX, offsetY, 0).texture(1.0F, 0.0F);
        bufferBuilder.vertex(matrix4f, offsetX, scaleY + offsetY, 0).texture(1.0F, 1.0F);
        bufferBuilder.vertex(matrix4f, scaleX + offsetX, scaleY + offsetY, 0).texture(0.0F, 1.0F);
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }
}