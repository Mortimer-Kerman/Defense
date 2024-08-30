package net.mortimer_kerman.defense;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.function.Function;

public class RenderLayers
{
    public static RenderLayer getIconSolidDepth(Identifier texture)
    {
        return ICON_SOLID_DEPTH.apply(texture);
    }

    public static RenderLayer getIconTransparentDepth(Identifier texture)
    {
        return ICON_TRANSPARENT_DEPTH.apply(texture);
    }

    public static RenderLayer getIconTransparentNoDepth(Identifier texture)
    {
        return ICON_TRANSPARENT_NO_DEPTH.apply(texture);
    }

    private static final Function<Identifier, RenderLayer> ICON_SOLID_DEPTH = Util.memoize(
            (texture) -> {
                RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder()
                        .program(RenderPhase.ENTITY_CUTOUT_NONULL_PROGRAM)
                        .texture(new RenderPhase.Texture(texture, false, false))
                        .transparency(RenderPhase.NO_TRANSPARENCY)
                        .cull(RenderPhase.DISABLE_CULLING)
                        .lightmap(RenderPhase.ENABLE_LIGHTMAP)
                        .overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
                        .depthTest(RenderPhase.LEQUAL_DEPTH_TEST)
                        .build(true);
                return RenderLayer.of(
                        "icon_solid_depth",
                        VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
                        VertexFormat.DrawMode.QUADS,
                        1536, true, false, multiPhaseParameters
                );
            });

    private static final Function<Identifier, RenderLayer> ICON_TRANSPARENT_DEPTH = Util.memoize(
            (texture) -> {
                RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder()
                        .program(RenderPhase.ENTITY_TRANSLUCENT_CULL_PROGRAM)
                        .texture(new RenderPhase.Texture(texture, false, false))
                        .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                        .cull(RenderPhase.ENABLE_CULLING)
                        .lightmap(RenderPhase.ENABLE_LIGHTMAP)
                        .overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
                        .depthTest(RenderPhase.LEQUAL_DEPTH_TEST)
                        .build(true);
                return RenderLayer.of(
                        "icon_transparent_depth",
                        VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
                        VertexFormat.DrawMode.QUADS,
                        1536, true, true, multiPhaseParameters
                );
            });

    private static final Function<Identifier, RenderLayer> ICON_TRANSPARENT_NO_DEPTH = Util.memoize(
            (texture) -> {
                RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder()
                        .program(RenderPhase.ENTITY_TRANSLUCENT_CULL_PROGRAM)
                        .texture(new RenderPhase.Texture(texture, false, false))
                        .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                        .cull(RenderPhase.ENABLE_CULLING)
                        .lightmap(RenderPhase.ENABLE_LIGHTMAP)
                        .overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
                        .depthTest(RenderPhase.ALWAYS_DEPTH_TEST)
                        .build(true);
                return RenderLayer.of(
                        "icon_solid_no_depth",
                        VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
                        VertexFormat.DrawMode.QUADS,
                        1536, true, true, multiPhaseParameters
                );
            });

    public static void init() { /*yes it is empty, yes it is normal*/ }
}
