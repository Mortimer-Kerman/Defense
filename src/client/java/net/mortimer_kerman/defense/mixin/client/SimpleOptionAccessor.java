package net.mortimer_kerman.defense.mixin.client;

import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Function;

@Mixin(SimpleOption.class)
public interface SimpleOptionAccessor<T>
{
    @Accessor Text getText();
    @Accessor Function<T, Text> getTextGetter();
}
