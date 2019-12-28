package com.atharion.commons.statemachines;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class StateGroup extends StateHolder {

    public StateGroup(@Nonnull State... states) {
        super(Arrays.asList(states));
    }

    @Override
    public void onStart() {
        this.states.forEach(State::start);
    }

    @Override
    public void onUpdate() {
        this.states.forEach(State::update);
        if (this.states.stream().allMatch(State::isEnded)) {
            this.end();
        }

    }

    @Override
    public void onEnd() {
        this.states.forEach(State::end);
    }

    @Override
    public boolean isReadyToEnd() {
        return this.states.stream().allMatch(State::isReadyToEnd);
    }

    @Override
    public Duration getDuration() {
        return Collections.max(this.states, Comparator.comparing(State::getDuration)).getDuration();
    }
}
