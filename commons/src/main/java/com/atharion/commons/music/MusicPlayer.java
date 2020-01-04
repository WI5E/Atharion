package com.atharion.commons.music;

import com.atharion.commons.Schedulers;
import com.atharion.commons.concurrent.promise.Promise;
import com.atharion.commons.event.Events;
import com.atharion.commons.event.Subscription;
import com.atharion.commons.music.model.Note;
import com.atharion.commons.terminable.Terminable;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class MusicPlayer implements Terminable {

    private boolean closed;

    private final Song song;
    private int tick;

    private final Set<Player> listeners = new HashSet<>();

    private Promise<Void> task;
    private Subscription quitListener;

    public MusicPlayer(@Nonnull Song song) {
        Objects.requireNonNull(song, "song");
        this.song = song;
    }

    public synchronized void play() {
        if (this.closed) {
            this.closed = false;
        }
        if (this.task != null) {
            return;
        }
        this.quitListener = Events.subscribe(PlayerQuitEvent.class)
                .handler(e -> this.listeners.remove(e.getPlayer()));
        this.task = Schedulers.async()
                .run(() -> {
                    while (!this.closed) {
                        long start = System.currentTimeMillis();

                        synchronized (MusicPlayer.this) {
                            this.tick++;

                            if (this.tick > this.song.getLength()) {
                                this.tick = -1;
                                continue;
                            }

                            Schedulers.sync().run(MusicPlayer.this::playTick);
                        }

                        if (this.closed) {
                            break;
                        }

                        long diff = System.currentTimeMillis() - start;
                        float delay = this.song.getDelay() * 50.0F;

                        if (diff < delay) {
                            try {
                                Thread.sleep((long) (delay - diff));
                            } catch (InterruptedException ex) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    }
                });
    }

    private void playTick() {
        this.song.getLayers().forEach(layer -> {
            Note note = layer.getNote(this.tick);

            if (note == null) {
                return;
            }
            Sound sound = note.getSound();
            float pitch = note.getPitch().asFloat();

            this.listeners.forEach(listener -> {
                if (listener.isOnline()) {
                    listener.playSound(listener.getLocation(), sound, 1.0F, pitch);
                }
            });
        });
    }

    public boolean addListener(@Nonnull Player player) {
        Objects.requireNonNull(player, "player");
        return this.listeners.add(player);
    }

    public boolean removeListener(@Nonnull Player player) {
        Objects.requireNonNull(player, "player");
        return this.listeners.remove(player);
    }

    @Override
    public void close() {
        this.closed = true;
        this.task.closeAndReportException();
        this.quitListener.closeAndReportException();
        this.task = null;
        this.quitListener = null;
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }
}
