package net.mortimer_kerman.defense;

import net.minecraft.text.ClickEvent;

public class CRunnableClickEvent implements ClickEvent
{
    Runnable action;

    public CRunnableClickEvent(Runnable runnable)
    {
        this.action = runnable;
    }

    public void execute()
    {
        action.run();
    }

    @Override
    public Action getAction() {
        return Action.RUN_COMMAND;
    }
}