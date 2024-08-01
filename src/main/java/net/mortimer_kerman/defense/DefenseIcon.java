package net.mortimer_kerman.defense;

import net.minecraft.util.Identifier;
import net.minecraft.util.TranslatableOption;
import net.minecraft.util.function.ValueLists;

import java.util.function.IntFunction;

public enum DefenseIcon implements TranslatableOption
{
    DEFAULT(0, "defense_icon.default", "default"),
    IRON(1, "defense_icon.iron", "iron"),
    GOLD(2, "defense_icon.gold", "gold"),
    DIAMOND(3, "defense_icon.diamond", "diamond"),
    NETHERITE(4, "defense_icon.netherite", "netherite"),
    LEATHER(5, "defense_icon.leather", "leather"),
    COPPER(6, "defense_icon.copper", "copper"),
    OXIDIZED_COPPER(7, "defense_icon.oxidized_copper", "oxidized_copper"),
    BATMAN(8, "defense_icon.batman", "batman"),
    ENDER_PEARL(9, "defense_icon.ender_pearl", "ender_pearl"),
    TRIDENT(10, "defense_icon.trident", "trident"),
    HEART_OF_THE_SEA(11, "defense_icon.heart_of_the_sea", "heart_of_the_sea"),
    KELP(12, "defense_icon.kelp", "kelp"),
    TURTLE_SHELL(13, "defense_icon.turtle_shell", "turtle_shell"),
    SLIME(14, "defense_icon.slime", "slime"),
    REDSTONE(15, "defense_icon.redstone", "redstone"),
    LAVA(16, "defense_icon.lava", "lava"),
    SCULK(17, "defense_icon.sculk", "sculk"),
    SPIRE(18, "defense_icon.spire", "spire"),
    CHERRY_WOOD(19,"defense_icon.cherry_wood", "cherry_wood"),
    CHERRY_LEAVES(20, "defense_icon.cherry_leaves", "cherry_leaves");

    private static final IntFunction<DefenseIcon> BY_ID = ValueLists.createIdToValueFunction(DefenseIcon::getId, values(), ValueLists.OutOfBoundsHandling.WRAP);
    private final int id;
    private final String translationKey;
    private final Identifier TEXTURE;
    private final Identifier TEXTURE_GUI;

    DefenseIcon(final int id, final String translationKey, final String texId)
    {
        this.id = id;
        this.translationKey = translationKey;
        this.TEXTURE = Identifier.of(Defense.MOD_ID, "textures/gui/sprites/defense_toggle/" + texId + ".png");
        this.TEXTURE_GUI = Identifier.of(Defense.MOD_ID, "defense_toggle/" + texId);
    }

    public int getId()
    {
        return this.id;
    }

    public String getTranslationKey()
    {
        return this.translationKey;
    }

    public static DefenseIcon byId(int id)
    {
        return BY_ID.apply(id);
    }

    public Identifier getTexture(boolean guiContext)
    {
        return guiContext ? TEXTURE_GUI : TEXTURE;
    }
}