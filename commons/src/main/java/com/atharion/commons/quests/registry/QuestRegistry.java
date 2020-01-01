package com.atharion.commons.quests.registry;

import com.atharion.commons.quests.Quest;
import com.google.common.reflect.TypeToken;

import javax.annotation.Nonnull;
import java.util.Optional;

public interface QuestRegistry {

    @Nonnull
    <T> Optional<T> find(@Nonnull TypeToken<T> type);

    @Nonnull
    default <T> Optional<T> find(@Nonnull Class<T> clazz) {
        return find(TypeToken.of(clazz));
    }

    <T extends Quest> void register(@Nonnull TypeToken<T> type, T quest);

    default <T extends Quest> void register(@Nonnull Class<T> clazz, T quest) {
        register(TypeToken.of(clazz), quest);
    }
}
