package net.mortimer_kerman.defense.render;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.function.Function;

public class DefenseRenderLayers
{
    public static RenderLayer getIconSolidDepth(Identifier texture)
    {
        return ICON_SOLID_DEPTH.apply(texture);
    }

    public static RenderLayer getIconSolidNoDepth(Identifier texture)
    {
        return ICON_SOLID_NO_DEPTH.apply(texture);
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
                RenderSetup renderSetup = RenderSetup.builder(RenderPipelines.ENTITY_CUTOUT_NO_CULL)
                        .texture("Sampler0", texture)
                        .useLightmap()
                        .useOverlay()
                        .crumbling()
                        .outlineMode(RenderSetup.OutlineMode.AFFECTS_OUTLINE).build();
                return RenderLayer.of("icon_solid_depth", renderSetup);
            });

    private static final Function<Identifier, RenderLayer> ICON_SOLID_NO_DEPTH = Util.memoize(
            texture -> {
                RenderSetup renderSetup = RenderSetup.builder(DefenseRenderPipelines.ENTITY_CUTOUT_NO_CULL_ALWAYS_DEPTH)
                        .texture("Sampler0", texture)
                        .useLightmap()
                        .useOverlay()
                        .crumbling()
                        .outlineMode(RenderSetup.OutlineMode.AFFECTS_OUTLINE).build();
                return RenderLayer.of("icon_solid_depth", renderSetup);
            });

    private static final Function<Identifier, RenderLayer> ICON_TRANSPARENT_DEPTH = Util.memoize(
            (texture) -> {
                RenderSetup renderSetup = RenderSetup.builder(RenderPipelines.RENDERTYPE_ITEM_ENTITY_TRANSLUCENT_CULL)
                        .texture("Sampler0", texture)
                        .useLightmap()
                        .useOverlay()
                        .crumbling()
                        .translucent()
                        .outlineMode(RenderSetup.OutlineMode.AFFECTS_OUTLINE).build();
                return RenderLayer.of("icon_transparent_depth", renderSetup);
            });

    private static final Function<Identifier, RenderLayer> ICON_TRANSPARENT_NO_DEPTH = Util.memoize(
            (texture) -> {
                RenderSetup renderSetup = RenderSetup.builder(DefenseRenderPipelines.RENDERTYPE_ITEM_ENTITY_TRANSLUCENT_CULL_ALWAYS_DEPTH)
                        .texture("Sampler0", texture)
                        .useLightmap()
                        .useOverlay()
                        .crumbling()
                        .translucent()
                        .outlineMode(RenderSetup.OutlineMode.AFFECTS_OUTLINE).build();
                return RenderLayer.of("icon_solid_no_depth", renderSetup);
            });

    public static void init() { /*yes it is empty, yes it is normal*/ }
}
