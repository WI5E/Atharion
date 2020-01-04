package com.atharion.commons.quests.goal;

import com.atharion.commons.terminable.composite.CompositeTerminable;
import com.atharion.commons.terminable.module.TerminableModule;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public abstract class AbstractGoal implements Goal {

    private CompositeTerminable compositeTerminable = CompositeTerminable.create();
    private double progress;

    private boolean ended;

    @Override
    public double getProgress() {
        return this.progress;
    }

    @Nonnull
    @Override
    public <T extends AutoCloseable> T bind(@Nonnull T terminable) {
        return this.compositeTerminable.bind(terminable);
    }

    @Nonnull
    @Override
    public <T extends TerminableModule> T bindModule(@Nonnull T module) {
        return this.compositeTerminable.bindModule(module);
    }

    @Override
    public void end(@Nonnull Player player) {
        this.onEnd(player);
        this.compositeTerminable.closeAndReportException();
        this.ended = true;
    }

    @Override
    public boolean isCompleted() {
        return this.ended;
    }
}