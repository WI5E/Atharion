package com.atharion.commons.quests;

import com.atharion.commons.quests.goal.Goal;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public abstract class UnstagedQuest extends AbstractQuest {

    public UnstagedQuest(@Nonnull String name) {
        super(name);
    }

    @Override
    public void start(@Nonnull Player player) {
        this.goals.forEach(goal -> goal.start(player));
    }

    @Override
    public void update(@Nonnull Player player, @Nonnull Goal goal, @Nonnull Response response) {
        if (goal.isCompleted()) {
            goal.end(player);

            if (this.allGoalsCompleted()) {
                this.end(player);
            }
        }
    }

    private boolean allGoalsCompleted() {
        for (Goal goal : this.goals) {
            if (!goal.isCompleted()) {
                return false;
            }
        }
        return true;
    }
}
