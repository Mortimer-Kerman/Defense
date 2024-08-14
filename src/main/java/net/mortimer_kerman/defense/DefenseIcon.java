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
    DIAMOND(3, "item.minecraft.diamond", "diamond"),
    NETHERITE(4, "defense_icon.netherite", "netherite"),
    LEATHER(5, "item.minecraft.leather", "leather"),
    COPPER(6, "defense_icon.copper", "copper"),
    OXIDIZED_COPPER(7, "block.minecraft.oxidized_copper", "oxidized_copper"),
    BATMAN(8, "defense_icon.batman", "batman"),
    ENDER_PEARL(9, "item.minecraft.ender_pearl", "ender_pearl"),
    TRIDENT(10, "item.minecraft.trident", "trident"),
    HEART_OF_THE_SEA(11, "item.minecraft.heart_of_the_sea", "heart_of_the_sea"),
    KELP(12, "block.minecraft.kelp", "kelp"),
    TURTLE_SHELL(13, "item.minecraft.turtle_helmet", "turtle_shell"),
    SLIME(14, "entity.minecraft.slime", "slime"),
    REDSTONE(15, "defense_icon.redstone", "redstone"),
    LAVA(16, "block.minecraft.lava", "lava"),
    SCULK(17, "block.minecraft.sculk", "sculk"),
    SPIRE(18, "defense_icon.spire", "spire"),
    CHERRY_WOOD(19,"block.minecraft.cherry_wood", "cherry_wood"),
    CHERRY_LEAVES(20, "block.minecraft.cherry_leaves", "cherry_leaves"),
    MOSS(21, "defense_icon.moss", "moss"),
    DARK_PRISMARINE(22, "block.minecraft.dark_prismarine", "dark_prismarine"),
    SALMON(23, "entity.minecraft.salmon", "salmon"),
    BREAD(24, "item.minecraft.bread", "bread"),
    FIRE(25, "block.minecraft.fire", "fire"),
    SOUL_FIRE(26, "block.minecraft.soul_fire", "soul_fire"),
    LIGHT_0(27, "defense_icon.light_0", "light_0"),
    LIGHT_15(28, "defense_icon.light_15", "light_15"),
    AMETHYST(29, "defense_icon.amethyst", "amethyst"),
    OMINOUS_BOTTLE(30, "item.minecraft.ominous_bottle", "ominous_bottle"),
    OMINOUS_KEY(31, "item.minecraft.ominous_trial_key", "ominous_key"),
    TRIAL_KEY(32, "item.minecraft.trial_key", "trial_key"),
    BREEZE(33, "entity.minecraft.breeze", "breeze"),
    BREEZE_ROD(34, "item.minecraft.breeze_rod", "breeze_rod"),
    WIND_CHARGE(35, "item.minecraft.wind_charge", "wind_charge"),
    END_CRYSTAL(36, "entity.minecraft.end_crystal", "end_crystal"),
    TOTEM(37, "item.minecraft.totem_of_undying", "totem"),
    WARPED_FUNGUS(38, "block.minecraft.warped_fungus", "warped_fungus"),
    CRIMSON_FUNGUS(39, "block.minecraft.crimson_fungus", "crimson_fungus"),
    KNARFY_PURPLE(40, "defense_icon.knarfy_purple", "knarfy_purple");

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