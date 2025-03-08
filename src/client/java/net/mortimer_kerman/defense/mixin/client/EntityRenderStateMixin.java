package net.mortimer_kerman.defense.mixin.client;

import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.util.Identifier;
import net.mortimer_kerman.defense.interfaces.EntityRenderStateMixinAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public class EntityRenderStateMixin implements EntityRenderStateMixinAccess
{
    @Unique Identifier defenseTexture = null;
    @Unique boolean immune = false;
    @Unique boolean afk = false;

    @Override
    public void defense$setData(Identifier defenseTexture, boolean immune, boolean afk) {
        this.defenseTexture = defenseTexture;
        this.immune = immune;
        this.afk = afk;
    }

    @Override
    public Identifier defense$getDefenseTexture() {
        return defenseTexture;
    }

    @Override
    public boolean defense$isImmune() {
        return immune;
    }

    @Override
    public boolean defense$isAfk() {
        return afk;
    }
}
