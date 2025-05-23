package net.mortimer_kerman.defense.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.util.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.util.Util;

import java.util.function.Function;

public class DefenseRenderLayers
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
            texture -> {
                RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder()
                        .texture(new RenderPhase.Texture(texture, TriState.FALSE, false))
                        .lightmap(RenderPhase.ENABLE_LIGHTMAP)
                        .overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
                        .build(true);

                return RenderLayer.of("icon_solid_depth", 1536, true, false, net.minecraft.client.gl.RenderPipelines.ENTITY_CUTOUT_NO_CULL, multiPhaseParameters);
            });

    private static final Function<Identifier, RenderLayer> ICON_TRANSPARENT_DEPTH = Util.memoize(
            (texture) -> {
                RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder()
                        .texture(new RenderPhase.Texture(texture, TriState.FALSE, false))
                        .lightmap(RenderPhase.ENABLE_LIGHTMAP)
                        .overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
                        .build(true);
                return RenderLayer.of("icon_transparent_depth", 1536, true, true, net.minecraft.client.gl.RenderPipelines.RENDERTYPE_ITEM_ENTITY_TRANSLUCENT_CULL, multiPhaseParameters);
            });

    private static final Function<Identifier, RenderLayer> ICON_TRANSPARENT_NO_DEPTH = Util.memoize(
            (texture) -> {
                RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder()
                        .texture(new RenderPhase.Texture(texture, TriState.FALSE, false))
                        .lightmap(RenderPhase.ENABLE_LIGHTMAP)
                        .overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
                        .build(true);
                return RenderLayer.of("icon_solid_no_depth", 1536, true, true, DefenseRenderPipelines.RENDERTYPE_ITEM_ENTITY_TRANSLUCENT_CULL_ALWAYS_DEPTH, multiPhaseParameters);
            });

    public static void init() { /*yes it is empty, yes it is normal*/ }
}
