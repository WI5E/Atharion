/*
 * This file is part of helper, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.atharion.commons.utils.player;

import com.atharion.commons.concurrent.promise.Promise;
import com.atharion.commons.utils.text.Text;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A collection of Player related utilities
 */
public final class Players {

    /**
     * Gets a player by uuid.
     *
     * @param uuid the uuid
     * @return a player, or null
     */
    @Nullable
    public static Player getNullable(UUID uuid) {
        return Bukkit.getPlayer(uuid);
    }

    /**
     * Gets a player by uuid.
     *
     * @param uuid the uuid
     * @return an optional player
     */
    public static Optional<Player> get(UUID uuid) {
        return Optional.ofNullable(getNullable(uuid));
    }

    /**
     * Gets a player by username.
     *
     * @param username the players username
     * @return the player, or null
     */
    @Nullable
    public static Player getNullable(String username) {
        //noinspection deprecation
        return Bukkit.getPlayerExact(username);
    }

    /**
     * Gets a player by username.
     *
     * @param username the players username
     * @return an optional player
     */
    public static Optional<Player> get(String username) {
        return Optional.ofNullable(getNullable(username));
    }

    /**
     * Gets all players on the server.
     *
     * @return all players on the server
     */
    public static Collection<Player> all() {
        //noinspection unchecked
        return (Collection<Player>) Bukkit.getOnlinePlayers();
    }

    /**
     * Gets a stream of all players on the server.
     *
     * @return a stream of all players on the server
     */
    public static Stream<Player> stream() {
        return all().stream();
    }

    /**
     * Applies a given action to all players on the server
     *
     * @param consumer the action to apply
     */
    public static void forEach(Consumer<Player> consumer) {
        all().forEach(consumer);
    }

    /**
     * Applies an action to each object in the iterable, if it is a player.
     *
     * @param objects the objects to iterate
     * @param consumer the action to apply
     */
    public static void forEachIfPlayer(Iterable<Object> objects, Consumer<Player> consumer) {
        for (Object o : objects) {
            if (o instanceof Player) {
                consumer.accept(((Player) o));
            }
        }
    }

    /**
     * Gets a stream of all players within a given radius of a point
     *
     * @param center the point
     * @param radius the radius
     * @return a stream of players
     */
    public static Stream<Player> streamInRange(Location center, double radius) {
        return center.getWorld().getNearbyEntities(center, radius, radius, radius).stream()
                .filter(e -> e instanceof Player)
                .map(e -> ((Player) e));
    }

    /**
     * Applies an action to all players within a given radius of a point
     *
     * @param center the point
     * @param radius the radius
     * @param consumer the action to apply
     */
    public static void forEachInRange(Location center, double radius, Consumer<Player> consumer) {
        streamInRange(center, radius)
                .forEach(consumer);
    }

    /**
     * Messages a sender a set of messages.
     *
     * @param sender the sender
     * @param msgs the messages to send
     */
    public static void msg(CommandSender sender, String... msgs) {
        for (String s : msgs) {
            sender.sendMessage(Text.colorize(s));
        }
    }

    @Nonnull
    public static Promise<OfflinePlayer> getOfflineNullable(UUID uuid) {
        return Promise.start().thenApplyAsync(aVoid -> Bukkit.getOfflinePlayer(uuid));
    }

    @Nonnull
    public static Promise<Optional<OfflinePlayer>> getOffline(UUID uuid) {
        return Promise.start().thenApplyAsync(aVoid -> {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            return offlinePlayer.getName() == null ? Optional.empty() : Optional.of(offlinePlayer);
        });
    }

    @Nonnull
    public static Promise<OfflinePlayer> getOfflineNullable(String username) {
        //noinspection deprecation
        return Promise.start().thenApplyAsync(aVoid -> Bukkit.getOfflinePlayer(username));
    }

    public static Promise<Optional<OfflinePlayer>> getOffline(String username) {
        return Promise.start().thenApplyAsync(aVoid -> {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(username);
            return offlinePlayer.getName() == null ? Optional.empty() : Optional.of(offlinePlayer);
        });
    }

    public static Promise<Collection<OfflinePlayer>> allOffline() {
        return Promise.start().thenApplyAsync(aVoid -> ImmutableList.copyOf(Bukkit.getOfflinePlayers()));
    }

    public static void streamOffline(Consumer<Stream<OfflinePlayer>> consumer) {
        allOffline().thenAcceptSync(offlinePlayers -> consumer.accept(offlinePlayers.stream()));
    }

    public static void forEachOffline(Consumer<OfflinePlayer> consumer) {
        allOffline().thenAcceptSync(offlinePlayers -> offlinePlayers.forEach(consumer));
    }

    public static void playSound(Player player, Sound sound) {
        player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
    }

    public static void playSound(Player player, Location location, Sound sound) {
        player.playSound(location, sound, 1.0f, 1.0f);
    }

    public static void playSound(Location location, Sound sound) {
        location.getWorld().playSound(location, sound, 1.0f, 1.0f);
    }

    public static void sendBlockChange(Player player, Location loc, BlockData blockData) {
        player.sendBlockChange(loc, blockData);
    }

    public static void sendBlockChange(Player player, Location loc, BlockState blockState) {
        sendBlockChange(player, loc, blockState.getBlockData());
    }

    public static void spawnParticle(Player player, Location location, Particle particle) {
        player.spawnParticle(particle, location, 1);
    }

    public static void spawnParticle(Location location, Particle particle) {
        location.getWorld().spawnParticle(particle, location, 1);
    }

    public static void spawnParticle(Player player, Location location, Particle particle, int amount) {
        Preconditions.checkArgument(amount > 0, "amount > 0");
        player.spawnParticle(particle, location, amount);
    }

    public static void spawnParticle(Location location, Particle particle, int amount) {
        Preconditions.checkArgument(amount > 0, "amount > 0");
        location.getWorld().spawnParticle(particle, location, amount);
    }

    public static void spawnParticleOffset(Player player, Location location, Particle particle, double offset) {
        player.spawnParticle(particle, location, 1, offset, offset, offset);
    }

    public static void spawnParticleOffset(Location location, Particle particle, double offset) {
        location.getWorld().spawnParticle(particle, location, 1, offset, offset, offset);
    }

    public static void spawnParticleOffset(Player player, Location location, Particle particle, int amount, double offset) {
        Preconditions.checkArgument(amount > 0, "amount > 0");
        player.spawnParticle(particle, location, amount, offset, offset, offset);
    }

    public static void spawnParticleOffset(Location location, Particle particle, int amount, double offset) {
        Preconditions.checkArgument(amount > 0, "amount > 0");
        location.getWorld().spawnParticle(particle, location, amount, offset, offset, offset);
    }

    public static void spawnEffect(Player player, Location location, Effect effect) {
        player.playEffect(location, effect, null);
    }

    public static void spawnEffect(Location location, Effect effect) {
        location.getWorld().playEffect(location, effect, null);
    }

    public static void spawnEffect(Player player, Location location, Effect effect, int amount) {
        Preconditions.checkArgument(amount > 0, "amount > 0");
        for (int i = 0; i < amount; i++) {
            player.playEffect(location, effect, null);
        }
    }

    public static void spawnEffect(Location location, Effect effect, int amount) {
        Preconditions.checkArgument(amount > 0, "amount > 0");
        for (int i = 0; i < amount; i++) {
            location.getWorld().playEffect(location, effect, null);
        }
    }

    public static void resetWalkSpeed(Player player) {
        player.setWalkSpeed(0.2f);
    }

    public static void resetFlySpeed(Player player) {
        player.setFlySpeed(0.1f);
    }

    private Players() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}