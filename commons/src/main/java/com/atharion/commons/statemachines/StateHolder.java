package com.atharion.commons.statemachines;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public abstract class StateHolder extends State implements Iterable<State> {

    protected final List<State> states = new ArrayList<>();

    public StateHolder(@Nonnull List<State> states) {
        this.states.addAll(states);
    }

    public void add(@Nonnull State state) {
        this.states.add(state);
    }

    @Override
    public void setFrozen(boolean frozen) {
        super.setFrozen(frozen);
        this.states.forEach(state -> state.setFrozen(frozen));
    }

    public void addAll(Collection<State> states) {
        this.states.addAll(states);
    }

    @Nonnull
    @Override
    public Iterator<State> iterator() {
        return this.states.iterator();
    }
}
