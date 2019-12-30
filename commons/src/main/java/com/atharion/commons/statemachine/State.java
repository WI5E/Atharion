package com.atharion.commons.statemachine;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.Instant;

@Getter
@Setter
public abstract class State {

    private boolean started = false;
    private boolean ended = false;

    private boolean frozen = false;

    private boolean updating = false;

    private Instant startInstant;

    @Getter(value = AccessLevel.NONE)
    private final Object lock = new Object();

    public synchronized void start() {
        synchronized (this.lock) {
            if (this.started || this.ended) {
                return;
            }
            this.started = true;
        }
        this.startInstant = Instant.now();
        try {
            this.onStart();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public abstract void onStart();

    public void update() {
        synchronized(this.lock) {
            if(!this.started || this.ended || this.updating) {
                return;
            }
            this.updating = true;
        }
        if(this.isReadyToEnd() && !this.frozen) {
            this.end();
            return;
        }

        try {
            this.onUpdate();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        this.updating = false;
    }

    public abstract void onUpdate();

    public void end() {
        synchronized (this.lock) {
            if (!this.started || this.ended) {
                return;
            }
            this.ended = true;
        }
        try {
            this.onEnd();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public boolean isReadyToEnd() {
        return this.ended || this.getRemainingDuration() == Duration.ZERO;
    }

    public abstract void onEnd();

    public abstract Duration getDuration();

    public Duration getRemainingDuration() {
        Duration sinceStart = Duration.between(this.startInstant, Instant.now());
        Duration remaining = this.getDuration().minus(sinceStart);
        return remaining.isNegative() ? Duration.ZERO : remaining;
    }
}
