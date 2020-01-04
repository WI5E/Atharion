package com.atharion.commons.quests.goal;

import com.atharion.commons.quests.Quest;
import com.atharion.commons.terminable.TerminableConsumer;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

public interface Goal extends Predicate<Player>, TerminableConsumer {

    /**
     * Called when the goal is started
     * @param player target
     */
    void onStart(@Nonnull Player player);

    /**
     * Used to start the goal
     * @param player target
     */
    default void start(@Nonnull Player player) {
        this.onStart(player);
    }

    /**
     * Called when the goal is completed
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

    default void update(@Nonnull Player player, @Nonnull Quest quest) {
        quest.update(player, this, Quest.Response.REQUIREMENT_UPDATED);
    }

    /**
     * The goal's current progress
     * @return progress (0.0 - 1.0)
     */
    double getProgress();

    /**
     * Checks if the goal has been completed
     * @return completed
     */
    boolean isCompleted();
}
