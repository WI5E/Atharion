package com.atharion.commons.quests.requirements;

import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;

public interface Requirement extends Predicate<Player> {

    @Nonnull
    default Set<Requirement> getNeededRequirements() {
        return Collections.emptySet();
    }

    double getProgress();

    void start(@Nonnull Player player);

    void end(@Nonnull Player player);

    default boolean isReadyToEnd(@Nonnull Player player) {
        return this.test(player);
    }
}
