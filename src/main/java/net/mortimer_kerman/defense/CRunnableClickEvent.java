package net.mortimer_kerman.defense;

import net.minecraft.text.ClickEvent;

public class CRunnableClickEvent extends ClickEvent
{
    Runnable action;

    public CRunnableClickEvent(Runnable runnable)
    {
        super(null, null);
        this.action = runnable;
    }

    public void execute()
    {
        action.run();
    }
}