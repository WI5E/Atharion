package com.atharion.commons.quests;

import com.atharion.commons.quests.goal.Goal;
import com.atharion.commons.quests.requirement.Requirement;
import com.atharion.commons.terminable.TerminableConsumer;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.List;

public interface Quest extends TerminableConsumer {

    /**
     * The name of the quest
     * @return the name
     */
    @Nonnull
    String getName();

    /**
     * The goals that must be finished to finish the quest
     * @return goals
     */
    @Nonnull
    List<Goal> getGoals();

    /**
     * The requirements needed to start the quest
     * @return requirements
     */
    @Nonnull
    List<Requirement> getRequirement();

    /**
     * Called when the quest is started
     * @param player target
     */
    void onStart(@Nonnull Player player);

    /**
     * Used to start the quest
     */
    default void start(@Nonnull Player player) {
        this.onStart(player);
    }

    /**
     * Called when the quest is completed
     * @param player target
     */
    void onEnd(@Nonnull Player player);

    /**
     * Used to end the quest
     * @param player target
     */
    default void end(@Nonnull Player player) {
        this.onEnd(player);
    }

    /**
     * Called when the quest is updated in some way
     * @param player target
     * @param response update response
     */
    void onUpdate(@Nonnull Player player, @Nonnull Goal goal, @Nonnull Response response);

    /**
     * Used to update the quest
     * @param player target
     * @param response update response
     */
    default void update(@Nonnull Player player, @Nonnull Goal goal, @Nonnull Response response) {
        this.onUpdate(player, goal, response);
    }

    /**
     * The quest's current progress
     * @return progress (0.0 - 1.0)
     */
    double getProgress();

    /**
     * Checks if the quest has been completed
     * @return completed
     */
    boolean isComplete();

    enum Response {
        QUEST_STARTED,
        REQUIREMENT_UPDATED,
        QUEST_FINISHED,
        QUEST_CANCELLED
    }
}