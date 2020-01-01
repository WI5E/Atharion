package com.atharion.commons.quests.registry;

import com.atharion.commons.quests.Quest;
import com.google.common.reflect.TypeToken;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleQuestRegistry implements QuestRegistry {

    private final Map<TypeToken<?>, Quest> quests = new ConcurrentHashMap<>();

    @Nonnull
    @Override
    public <T> Optional<T> find(@Nonnull TypeToken<T> type) {
        Objects.requireNonNull(type, "type");
        Quest quest = this.quests.get(type);

        //noinspection unchecked
        return quest == null ? Optional.empty() : Optional.of((T) quest);
    }

    @Override
    public <T extends Quest> void register(@Nonnull TypeToken<T> type, T quest) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(quest, "quest");
        this.quests.put(type, quest);
    }
}
