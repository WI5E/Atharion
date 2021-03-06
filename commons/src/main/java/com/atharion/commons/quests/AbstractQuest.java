package com.atharion.commons.quests;

import com.atharion.commons.quests.goal.Goal;
import com.atharion.commons.terminable.composite.CompositeTerminable;
import com.atharion.commons.terminable.module.TerminableModule;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class AbstractQuest implements Quest {

    private final CompositeTerminable compositeTerminable = CompositeTerminable.create();

    private String name;

    protected final List<Goal> goals = new ArrayList<>();
    private double progress;

    public AbstractQuest(@Nonnull String name) {
        Objects.requireNonNull(name, "name");
        this.name = name;
    }

    protected void addGoal(@Nonnull Goal goal) {
        Objects.requireNonNull(goal, "goal");
        this.goals.add(goal);
    }

    @Nonnull
    @Override
    public List<Goal> getGoals() {
        return Collections.unmodifiableList(this.goals);
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

    protected void addProgress(double amount) {
        this.progress = Math.min(1.0D, this.progress + amount);
    }

    protected void removeProgress(double amount) {
        this.progress = Math.max(0.0D, this.progress - amount);
    }

    @Override
    public void end(@Nonnull Player player) {
        this.onEnd(player);
        this.compositeTerminable.closeAndReportException();
    }
}
