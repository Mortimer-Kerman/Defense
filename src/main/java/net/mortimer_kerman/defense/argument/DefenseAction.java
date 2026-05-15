package net.mortimer_kerman.defense.argument;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum DefenseAction implements StringRepresentable
{
    ON("on", 0),
    OFF("off", 1),
    REFRESH("refresh", 2);

    public static final Codec<DefenseAction> CODEC;
    public final String id;
    public final int tag;

    DefenseAction(String id, int tag) { this.id = id; this.tag = tag; }

    @Override
    public String getSerializedName() {
        return this.id;
    }

    static {
        CODEC = StringRepresentable.fromEnum(DefenseAction::values);
    }
}