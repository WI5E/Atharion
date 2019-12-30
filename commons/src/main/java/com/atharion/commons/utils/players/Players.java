package com.atharion.commons.utils.players;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class Players {

    private Players() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    public static Collection<? extends Player> all() {
        return Bukkit.getOnlinePlayers();
    }

    public static Collection<? extends Player> allOf(@Nonnull Predicate<Player> predicate) {
        return Bukkit.getOnlinePlayers()
                .stream()
                .filter(predicate)
                .collect(Collectors.toCollection(HashSet::new));
    }

    public static int size() {
        return all().size();
    }

    public boolean any() {
        return size() > 0;
    }

    public void forEach(@Nonnull Consumer<Player> consumer) {
        all().forEach(consumer);
    }
}
