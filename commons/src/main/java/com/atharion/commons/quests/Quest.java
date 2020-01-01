package com.atharion.commons.quests;

import com.atharion.commons.quests.requirements.Requirement;
import com.google.gson.reflect.TypeToken;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public interface Quest {

    /**
     * The quest name
     * @return The name
     */
    @Nonnull
    String getName();

    /**
     * Required requests needed to start the quest
     * @param <T> the quests
     * @return
     */
    @Nonnull
    default <T extends Quest> Set<TypeToken<T>> getRequiredQuests() {
        return Collections.emptySet();
    }

    /**
     * Determines if the player can start the quest
     * @param player
     * @return can start
     */
    boolean canStart(@Nonnull Player player);

    /**
     * The requirements or goals for the quest
     * @return
     */
    @Nonnull
    List<Requirement> getRequirements();

    /**
     * Determines if the quest is ready to end
     * @param player
     * @return ready to end
     */
    default boolean isReadyToEnd(@Nonnull Player player) {
        for (Requirement requirement : this.getRequirements()) {
            if (!requirement.isReadyToEnd(player)) {
                return false;
            }
        }
        return true;
    }
}
