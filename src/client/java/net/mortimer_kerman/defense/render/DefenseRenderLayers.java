package net.mortimer_kerman.defense.render;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.function.Function;

public class DefenseRenderLayers
{
    public static RenderType getIconSolidDepth(Identifier texture)
    {
        return ICON_SOLID_DEPTH.apply(texture);
    }

    public static RenderType getIconSolidNoDepth(Identifier texture)
    {
        return ICON_SOLID_NO_DEPTH.apply(texture);
    }

    public static RenderType getIconTransparentDepth(Identifier texture)
    {
        return ICON_TRANSPARENT_DEPTH.apply(texture);
    }

    public static RenderType getIconTransparentNoDepth(Identifier texture)
    {
        return ICON_TRANSPARENT_NO_DEPTH.apply(texture);
    }

    private static final Function<Identifier, RenderType> ICON_SOLID_DEPTH = Util.memoize(
            texture -> {
                RenderSetup renderSetup = RenderSetup.builder(RenderPipelines.ENTITY_CUTOUT_NO_CULL)
                        .withTexture("Sampler0", texture)
                        .useLightmap()
                        .useOverlay()
                        .affectsCrumbling()
                        .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE).createRenderSetup();
                return RenderType.create("icon_solid_depth", renderSetup);
            });

    private static final Function<Identifier, RenderType> ICON_SOLID_NO_DEPTH = Util.memoize(
            texture -> {
                RenderSetup renderSetup = RenderSetup.builder(DefenseRenderPipelines.ENTITY_CUTOUT_NO_CULL_ALWAYS_DEPTH)
                        .withTexture("Sampler0", texture)
                        .useLightmap()
                        .useOverlay()
                        .affectsCrumbling()
                        .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE).createRenderSetup();
                return RenderType.create("icon_solid_depth", renderSetup);
            });

    private static final Function<Identifier, RenderType> ICON_TRANSPARENT_DEPTH = Util.memoize(
            (texture) -> {
                RenderSetup renderSetup = RenderSetup.builder(RenderPipelines.ITEM_ENTITY_TRANSLUCENT_CULL)
                        .withTexture("Sampler0", texture)
                        .useLightmap()
                        .useOverlay()
                        .affectsCrumbling()
                        .sortOnUpload()
                        .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE).createRenderSetup();
                return RenderType.create("icon_transparent_depth", renderSetup);
            });

    private static final Function<Identifier, RenderType> ICON_TRANSPARENT_NO_DEPTH = Util.memoize(
            (texture) -> {
                RenderSetup renderSetup = RenderSetup.builder(DefenseRenderPipelines.RENDERTYPE_ITEM_ENTITY_TRANSLUCENT_CULL_ALWAYS_DEPTH)
                        .withTexture("Sampler0", texture)
                        .useLightmap()
                        .useOverlay()
                        .affectsCrumbling()
                        .sortOnUpload()
                        .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE).createRenderSetup();
                return RenderType.create("icon_solid_no_depth", renderSetup);
            });

    public static void init() { /*yes it is empty, yes it is normal*/ }
}
