package net.mortimer_kerman.defense;

import org.jetbrains.annotations.NotNull;

import net.minecraft.network.chat.ClickEvent;

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
    @NotNull
    public Action action() {
        return Action.RUN_COMMAND;
    }
}