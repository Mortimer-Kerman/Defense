package net.mortimer_kerman.defense.interfaces;

import net.minecraft.util.Identifier;

public interface EntityRenderStateMixinAccess
{
    void defense$setData(Identifier defenseTexture, boolean immune, boolean afk);
    Identifier defense$getDefenseTexture();
    boolean defense$isImmune();
    boolean defense$isAfk();
}
