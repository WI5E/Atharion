package com.atharion.commons.statemachine;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Arrays;

public class StateSeries extends StateHolder {

    protected int current;
    protected boolean skipping = false;

    public StateSeries(@Nonnull State... states) {
        super(Arrays.asList(states));
    }

    public void addNext(@Nonnull State state) {
        this.states.add(this.current + 1, state);
    }

    public void addNext(@Nonnull State... states) {
        int i = 1;
        for (State state : states) {
            this.states.add(this.current + i, state);
            ++i;
        }
    }

    public void skip() {
        this.skipping = true;
    }

    @Override
    public void onStart() {
        if (this.states.isEmpty()) {
            this.end();
            return;
        }
        this.states.get(this.current).start();
    }

    @Override
    public void onUpdate() {
        State currentState = this.states.get(this.current);
        currentState.update();

        if ((currentState.isReadyToEnd() && !currentState.isFrozen()) || this.skipping) {
            if (this.skipping) {
                this.skipping = false;
            }
            currentState.end();
            ++this.current;
            if (this.current >= this.states.size()) {
                this.end();
                return;
            }
            this.states.get(this.current).start();
        }
    }

    @Override
    public boolean isReadyToEnd() {
        return this.current == this.states.size() - 1 && this.states.get(this.current).isReadyToEnd();
    }

    @Override
    public void onEnd() {
        if (this.current < this.states.size()) {
            this.states.get(this.current).end();
        }
    }

    @Override
    public Duration getDuration() {
        Duration duration = Duration.ZERO;
        for (State state : this.states) {
            duration = duration.plus(state.getDuration());
        }
        return duration;
    }
}
