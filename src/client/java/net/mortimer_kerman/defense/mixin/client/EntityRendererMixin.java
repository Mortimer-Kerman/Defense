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
import net.mortimer_kerman.defense.DefenseClient;
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

            RenderSystem.setShaderTexture(0, DefenseClient.getPlayerIcon(player).getTexture(false));
            //RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapProgram);

            /*float lightFactor = (float)Character.digit(Integer.toHexString(light).toUpperCase().charAt(0), 16) / 15.0F;
            float lightLevel = lightFactor / (4.0F - 3.0F * lightFactor);*/

            if (sneaking) RenderSystem.enableBlend();
            else RenderSystem.disableBlend();

            RenderSystem.enableDepthTest();
            displayDefenseIcon(matrix4f, scaleX, scaleY, offsetY, offsetX, light);
            RenderSystem.disableDepthTest();

            if(!sneaking)
            {
                RenderSystem.enableBlend();
                displayDefenseIcon(matrix4f, scaleX, scaleY, offsetY, offsetX, light);
            }

            RenderSystem.disableBlend();
        }
    }

    @Unique private static void displayDefenseIcon(Matrix4f matrix4f, float scaleX, float scaleY, float offsetY, float offsetX, int light)
    {
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT);
        bufferBuilder.vertex(matrix4f, scaleX + offsetX,          offsetY, 0).color(1f, 1f, 1f, 0.3f).texture(0.0F, 0.0F).light(light);
        bufferBuilder.vertex(matrix4f,          offsetX,          offsetY, 0).color(1f, 1f, 1f, 0.3f).texture(1.0F, 0.0F).light(light);
        bufferBuilder.vertex(matrix4f,          offsetX, scaleY + offsetY, 0).color(1f, 1f, 1f, 0.3f).texture(1.0F, 1.0F).light(light);
        bufferBuilder.vertex(matrix4f, scaleX + offsetX, scaleY + offsetY, 0).color(1f, 1f, 1f, 0.3f).texture(0.0F, 1.0F).light(light);
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }
}