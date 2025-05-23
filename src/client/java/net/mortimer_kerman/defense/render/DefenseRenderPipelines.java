package net.mortimer_kerman.defense.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.render.VertexFormats;

public class DefenseRenderPipelines
{
    public static final com.mojang.blaze3d.pipeline.RenderPipeline RENDERTYPE_ITEM_ENTITY_TRANSLUCENT_CULL_ALWAYS_DEPTH = net.minecraft.client.gl.RenderPipelines.register(
            com.mojang.blaze3d.pipeline.RenderPipeline.builder(net.minecraft.client.gl.RenderPipelines.MATRICES_COLOR_FOG_LIGHT_DIR_SNIPPET)
                    .withLocation("pipeline/item_entity_translucent_cull")
                    .withVertexShader("core/rendertype_item_entity_translucent_cull")
                    .withFragmentShader("core/rendertype_item_entity_translucent_cull")
                    .withSampler("Sampler0")
                    .withSampler("Sampler2")
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withVertexFormat(VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS)
                    .build()
    );

    public static void init() { /*yes it is empty, yes it is normal*/ }
}
