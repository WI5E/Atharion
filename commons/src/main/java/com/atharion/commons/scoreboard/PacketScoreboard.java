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

package com.atharion.commons.scoreboard;

import com.atharion.commons.CompactPlugin;
import com.atharion.commons.event.Events;
import com.atharion.commons.utils.player.Players;
import com.google.common.base.Preconditions;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.DisplaySlot;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Implements {@link Scoreboard} using ProtocolLib.
 *
 * <p>This class as well as all returned instances are thread safe.</p>
 */
public class PacketScoreboard implements Scoreboard {

    // teams & objectives shared by all players.
    // these are automatically subscribed to when players join
    private final Map<String, PacketScoreboardTeam> teams = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, PacketScoreboardObjective> objectives = Collections.synchronizedMap(new HashMap<>());

    // per-player teams & objectives.
    private final Map<UUID, Map<String, PacketScoreboardTeam>> playerTeams = Collections.synchronizedMap(new HashMap<>());
    private final Map<UUID, Map<String, PacketScoreboardObjective>> playerObjectives = Collections.synchronizedMap(new HashMap<>());

    public PacketScoreboard(@Nonnull CompactPlugin plugin) {
        Events.subscribe(PlayerJoinEvent.class).handler(this::handlePlayerJoin).bindWith(plugin);
        Events.subscribe(PlayerQuitEvent.class).handler(this::handlePlayerQuit).bindWith(plugin);
    }

    private void handlePlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // auto subscribe to teams
        for (PacketScoreboardTeam t : this.teams.values()) {
            if (t.shouldAutoSubscribe()) {
                t.subscribe(player);
            }
        }

        // auto subscribe to objectives
        for (PacketScoreboardObjective o : this.objectives.values()) {
            if (o.shouldAutoSubscribe()) {
                o.subscribe(player);
            }
        }
    }

    private void handlePlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        this.teams.values().forEach(t -> {
            t.unsubscribe(player, true);
            t.removePlayer(player);
        });
        this.objectives.values().forEach(o -> o.unsubscribe(player, true));

        Map<String, PacketScoreboardObjective> playerObjectives = this.playerObjectives.remove(player.getUniqueId());
        if (playerObjectives != null) {
            playerObjectives.values().forEach(o -> o.unsubscribe(player, true));
        }

        Map<String, PacketScoreboardTeam> playerTeams = this.playerTeams.remove(player.getUniqueId());
        if (playerTeams != null) {
            playerTeams.values().forEach(t -> {
                t.unsubscribe(player, true);
                t.removePlayer(player);
            });
        }
    }

    @Override
    public PacketScoreboardTeam createTeam(String id, String title, boolean autoSubscribe) {
        Preconditions.checkArgument(id.length() <= 16, "id cannot be longer than 16 characters");
        Preconditions.checkState(!this.teams.containsKey(id), "id already exists");

        PacketScoreboardTeam team = new PacketScoreboardTeam(id, title, autoSubscribe);
        if (autoSubscribe) {
            Players.forEach(team::subscribe);
        }

        this.teams.put(id, team);
        return team;
    }

    @Override
    @Nullable
    public PacketScoreboardTeam getTeam(String id) {
        return this.teams.get(id);
    }

    @Override
    public boolean removeTeam(String id) {
        PacketScoreboardTeam team = this.teams.remove(id);
        if (team == null) {
            return false;
        }

        team.unsubscribeAll();
        return true;
    }

    @Override
    public PacketScoreboardObjective createObjective(String id, String title, DisplaySlot displaySlot, boolean autoSubscribe) {
        Preconditions.checkArgument(id.length() <= 16, "id cannot be longer than 16 characters");
        Preconditions.checkState(!this.objectives.containsKey(id), "id already exists");

        PacketScoreboardObjective objective = new PacketScoreboardObjective(id, title, displaySlot, autoSubscribe);
        if (autoSubscribe) {
            Players.forEach(objective::subscribe);
        }

        this.objectives.put(id, objective);
        return objective;
    }

    @Override
    @Nullable
    public PacketScoreboardObjective getObjective(String id) {
        return this.objectives.get(id);
    }

    @Override
    public boolean removeObjective(String id) {
        PacketScoreboardObjective objective = this.objectives.remove(id);
        if (objective == null) {
            return false;
        }

        objective.unsubscribeAll();
        return true;
    }

    @Override
    public PacketScoreboardTeam createPlayerTeam(Player player, String id, String title, boolean autoSubscribe) {
        Preconditions.checkArgument(id.length() <= 16, "id cannot be longer than 16 characters");
        Map<String, PacketScoreboardTeam> teams = this.playerTeams.computeIfAbsent(player.getUniqueId(), p -> new HashMap<>());
        Preconditions.checkState(!teams.containsKey(id), "id already exists");

        PacketScoreboardTeam team = new PacketScoreboardTeam(id, title, autoSubscribe);
        if (autoSubscribe) {
            team.subscribe(player);
        }
        teams.put(id, team);

        return team;
    }

    @Override
    @Nullable
    public PacketScoreboardTeam getPlayerTeam(Player player, String id) {
        Map<String, PacketScoreboardTeam> map = this.playerTeams.get(player.getUniqueId());
        if (map == null) {
            return null;
        }

        return map.get(id);
    }

    @Override
    public boolean removePlayerTeam(Player player, String id) {
        Map<String, PacketScoreboardTeam> map = this.playerTeams.get(player.getUniqueId());
        if (map == null) {
            return false;
        }

        PacketScoreboardTeam team = map.remove(id);
        if (team == null) {
            return false;
        }

        team.unsubscribeAll();
        return true;
    }

    @Override
    public PacketScoreboardObjective createPlayerObjective(Player player, String id, String title, DisplaySlot displaySlot, boolean autoSubscribe) {
        Preconditions.checkArgument(id.length() <= 16, "id cannot be longer than 16 characters");
        Map<String, PacketScoreboardObjective> objectives = this.playerObjectives.computeIfAbsent(player.getUniqueId(), p -> new HashMap<>());
        Preconditions.checkState(!objectives.containsKey(id), "id already exists");

        PacketScoreboardObjective objective = new PacketScoreboardObjective(id, title, displaySlot, autoSubscribe);
        if (autoSubscribe) {
            objective.subscribe(player);
        }
        objectives.put(id, objective);

        return objective;
    }

    @Override
    @Nullable
    public PacketScoreboardObjective getPlayerObjective(Player player, String id) {
        Map<String, PacketScoreboardObjective> map = this.playerObjectives.get(player.getUniqueId());
        if (map == null) {
            return null;
        }

        return map.get(id);
    }

    @Override
    public boolean removePlayerObjective(Player player, String id) {
        Map<String, PacketScoreboardObjective> map = this.playerObjectives.get(player.getUniqueId());
        if (map == null) {
            return false;
        }

        PacketScoreboardObjective objective = map.remove(id);
        if (objective == null) {
            return false;
        }

        objective.unsubscribeAll();
        return true;
    }

}