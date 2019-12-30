package com.atharion.commons.statemachine;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;

@Getter
@Setter
public class StateSwitch {

    protected State state;

    public void changeState(@Nonnull State next) {
        this.state.end();
        this.state = next;
        next.start();
    }

    public void update() {
        this.state.update();
    }
}
