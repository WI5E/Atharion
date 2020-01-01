package com.atharion.commons.quests.requirements;

import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.Instant;

public interface TimedRequirement extends Requirement {

    @Nonnull
    Instant getStart();

    @Nonnull
    Duration getDuration();

    void update();

    @Nonnull
    default Duration getRemainingDuration() {
        Duration sinceStart = Duration.between(this.getStart(), Instant.now());
        Duration remaining = this.getDuration().minus(sinceStart);
        return remaining.isNegative() ? Duration.ZERO : remaining;
    }

    @Override
    default boolean isReadyToEnd(@Nonnull Player player) {
        return this.test(player) && this.getRemainingDuration() == Duration.ZERO;
    }
}
