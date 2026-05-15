package net.mortimer_kerman.defense.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;

import net.mortimer_kerman.defense.interfaces.EntityRenderStateMixinAccess;

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
