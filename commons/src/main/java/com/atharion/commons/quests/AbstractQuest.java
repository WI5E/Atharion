package com.atharion.commons.quests;

import com.atharion.commons.quests.requirements.Requirement;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public abstract class AbstractQuest implements Quest {

    private final String name;

    private final List<Requirement> requirements;

    public AbstractQuest(String name, Requirement... requirements) {
        this.name = name;
        this.requirements = Arrays.asList(requirements);
    }
}
