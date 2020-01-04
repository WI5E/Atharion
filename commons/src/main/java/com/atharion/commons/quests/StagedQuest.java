package com.atharion.commons.quests;

import com.atharion.commons.quests.goal.Goal;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public abstract class StagedQuest extends AbstractQuest {

    private int current;

    public StagedQuest(@Nonnull String name) {
        super(name);
    }

    @Override
    public void start(@Nonnull Player player) {
        this.goals.get(this.current).start(player);
    }

    @Override
    public void update(@Nonnull Player player, @Nonnull Goal called, @Nonnull Response response) {
        Goal goal = this.goals.get(this.current);

        if (goal.isCompleted()) {
            goal.end(player);

            if (this.current == this.goals.size() - 1) {
                //last goal finished
                this.end(player);
                return;
            }

            this.current++;
            Goal nextGoal = this.goals.get(this.current);
            nextGoal.start(player);
        }
    }
}
