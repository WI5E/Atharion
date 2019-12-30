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

package com.atharion.commons.hologram.individual;

import com.atharion.commons.event.Events;
import com.atharion.commons.packet.Protocol;
import com.atharion.commons.reflect.ServerReflection;
import com.atharion.commons.terminable.composite.CompositeTerminable;
import com.atharion.commons.utils.text.Text;
import com.atharion.commons.world.Position;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PacketIndividualHologramFactory implements IndividualHologramFactory {
    private static final Method GET_HANDLE_METHOD;
    private static final Method GET_ID_METHOD;

    private static final WrappedDataWatcher.Serializer BOOLEAN_SERIALISER = WrappedDataWatcher.Registry.get(Boolean.class);
    private static final WrappedDataWatcher.Serializer STRING_SERIALISER = WrappedDataWatcher.Registry.get(String.class);

    private static WrappedDataWatcher.WrappedDataWatcherObject toWatcherObject(int index, WrappedDataWatcher.Serializer serializer) {
        return new WrappedDataWatcher.WrappedDataWatcherObject(index, serializer);
    }

    static {
        try {
            Class<?> entityClass = ServerReflection.nmsClass("Entity");
            GET_ID_METHOD = entityClass.getDeclaredMethod("getId");
            GET_ID_METHOD.setAccessible(true);

            Class<?> craftEntityClass = ServerReflection.obcClass("entity.CraftEntity");
            GET_HANDLE_METHOD = craftEntityClass.getDeclaredMethod("getHandle");
            GET_HANDLE_METHOD.setAccessible(true);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static int getEntityId(Entity entity) {
        try {
            Object handle = GET_HANDLE_METHOD.invoke(entity);
            return (int) GET_ID_METHOD.invoke(handle);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    @Override
    public IndividualHologram newHologram(@Nonnull Position position, @Nonnull List<HologramLine> lines) {
        return new PacketHologram(position, lines);
    }

    private static final class HologramEntity {
        private ArmorStand armorStand;
        private HologramLine line;
        private int entityId;

        private final Map<Integer, WrappedWatchableObject> cachedMetadata = new HashMap<>();

        private HologramEntity(HologramLine line) {
            this.line = line;
        }

        public ArmorStand getArmorStand() {
            return this.armorStand;
        }

        public void setArmorStand(ArmorStand armorStand) {
            this.armorStand = armorStand;
        }

        public HologramLine getLine() {
            return this.line;
        }

        public void setLine(HologramLine line) {
            this.line = line;
        }

        public int getId() {
            return this.entityId;
        }

        public void setId(int entityId) {
            this.entityId = entityId;
        }

        public Map<Integer, WrappedWatchableObject> getCachedMetadata() {
            return this.cachedMetadata;
        }
    }

    private static final class PacketHologram implements IndividualHologram {

        private Position position;
        private final List<HologramLine> lines = new ArrayList<>();
        private final List<HologramEntity> spawnedEntities = new ArrayList<>();
        private final Set<Player> viewers = Collections.synchronizedSet(new HashSet<>());
        private boolean spawned = false;

        private CompositeTerminable listeners = null;
        private Consumer<Player> clickCallback = null;

        PacketHologram(Position position, List<HologramLine> lines) {
            this.position = Objects.requireNonNull(position, "position");
            updateLines(lines);
        }

        private Position getNewLinePosition() {
            if (this.spawnedEntities.isEmpty()) {
                return this.position;
            } else {
                // get the last entry
                ArmorStand last = this.spawnedEntities.get(this.spawnedEntities.size() - 1).getArmorStand();
                return Position.of(last.getLocation()).subtract(0.0d, 0.25d, 0.0d);
            }
        }

        @Override
        public void spawn() {
            // ensure listening
            if (this.listeners == null) {
                setupPacketListeners();
            }

            // resize to fit any new lines
            int linesSize = this.lines.size();
            int spawnedSize = this.spawnedEntities.size();

            // remove excess lines
            if (linesSize < spawnedSize) {
                int diff = spawnedSize - linesSize;
                for (int i = 0; i < diff; i++) {

                    // get and remove the last entry
                    int index = this.spawnedEntities.size() - 1;

                    // remove the armorstand first
                    ArmorStand as = this.spawnedEntities.get(index).getArmorStand();
                    as.remove();

                    // then remove from the list
                    this.spawnedEntities.remove(index);
                }
            }

            // now enough armorstands are spawned, we can now update the text
            for (int i = 0; i < this.lines.size(); i++) {
                HologramLine line = this.lines.get(i);

                String generatedName = "hologramline-" + ThreadLocalRandom.current().nextInt(100000000);

                if (i >= this.spawnedEntities.size()) {
                    // add a new line
                    Location loc = getNewLinePosition().toLocation();

                    // init the holo entity before actually spawning (so the listeners can catch it)
                    HologramEntity holoEntity = new HologramEntity(line);
                    this.spawnedEntities.add(holoEntity);

                    // ensure the hologram's chunk is loaded.
                    Chunk chunk = loc.getChunk();
                    if (!chunk.isLoaded()) {
                        chunk.load();
                    }

                    // spawn the armorstand
                    loc.getWorld().spawn(loc, ArmorStand.class, as -> {
                        int eid = getEntityId(as);
                        holoEntity.setId(eid);
                        holoEntity.setArmorStand(as);

                        as.setSmall(true);
                        as.setMarker(true);
                        as.setArms(false);
                        as.setBasePlate(false);
                        as.setGravity(false);
                        as.setVisible(false);
                        as.setCustomName(generatedName);
                        as.setCustomNameVisible(true);

                        as.setAI(false);
                        as.setCollidable(false);
                        as.setInvulnerable(true);
                    });

                } else {
                    // update existing line if necessary
                    HologramEntity as = this.spawnedEntities.get(i);

                    if (!as.getLine().equals(line)) {
                        as.setLine(line);
                        as.getArmorStand().setCustomName(generatedName);
                    }
                }
            }

            this.spawned = true;
        }

        @Override
        public void despawn() {
            this.spawnedEntities.forEach(e -> e.getArmorStand().remove());
            this.spawnedEntities.clear();
            this.spawned = false;

            if (this.listeners != null) {
                this.listeners.closeAndReportException();
            }
            this.listeners = null;
        }

        @Override
        public boolean isSpawned() {
            if (!this.spawned) {
                return false;
            }

            for (HologramEntity stand : this.spawnedEntities) {
                if (!stand.getArmorStand().isValid()) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public void updatePosition(@Nonnull Position position) {
            Objects.requireNonNull(position, "position");
            if (this.position.equals(position)) {
                return;
            }

            this.position = position;
            despawn();
            spawn();
        }

        @Override
        public void updateLines(@Nonnull List<HologramLine> lines) {
            Objects.requireNonNull(lines, "lines");
            Preconditions.checkArgument(!lines.isEmpty(), "lines cannot be empty");
            for (HologramLine line : lines) {
                Preconditions.checkArgument(line != null, "null line");
            }

            if (this.lines.equals(lines)) {
                return;
            }

            this.lines.clear();
            this.lines.addAll(lines);
        }

        @Nonnull
        @Override
        public Set<Player> getViewers() {
            return ImmutableSet.copyOf(this.viewers);
        }

        @Override
        public void addViewer(@Nonnull Player player) {
            if (!this.viewers.add(player)) {
                return;
            }

            // handle resending
            for (HologramEntity entity : this.spawnedEntities) {
                PacketContainer spawnPacket = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);
                spawnPacket.getModifier().writeDefaults();

                // write entity id
                spawnPacket.getIntegers().write(0, entity.getId());

                // write unique id
                spawnPacket.getUUIDs().write(0, entity.getArmorStand().getUniqueId());

                // write coordinates
                Location loc = entity.getArmorStand().getLocation();
                spawnPacket.getDoubles().write(0, loc.getX());
                spawnPacket.getDoubles().write(1, loc.getY());
                spawnPacket.getDoubles().write(2, loc.getZ());
                spawnPacket.getIntegers().write(4, (int) ((loc.getPitch()) * 256.0F / 360.0F));
                spawnPacket.getIntegers().write(5, (int) ((loc.getYaw()) * 256.0F / 360.0F));

                // write type
                spawnPacket.getIntegers().write(6, 78);

                // write object data
                spawnPacket.getIntegers().write(7, 0);

                // send it
                Protocol.sendPacket(player, spawnPacket);


                // send missed metadata
                PacketContainer metadataPacket = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);

                // write entity id
                metadataPacket.getIntegers().write(0, entity.getId());

                // write metadata
                WrappedDataWatcher dataWatcher = new WrappedDataWatcher();

                // set custom name
                dataWatcher.setObject(toWatcherObject(2, STRING_SERIALISER), Text.colorize(entity.getLine().resolve(player)));
                // set custom name visible
                dataWatcher.setObject(toWatcherObject(3, BOOLEAN_SERIALISER), true);

                List<WrappedWatchableObject> watchableObjects = new ArrayList<>(dataWatcher.getWatchableObjects());
                for (Map.Entry<Integer, WrappedWatchableObject> ent : entity.getCachedMetadata().entrySet()) {
                    if (ent.getKey() != 2 && ent.getKey() != 3) {
                        watchableObjects.add(ent.getValue());
                    }
                }

                metadataPacket.getWatchableCollectionModifier().write(0, watchableObjects);

                // send it
                Protocol.sendPacket(player, metadataPacket);
            }
        }

        @Override
        public void removeViewer(@Nonnull Player player) {
            if (!this.viewers.remove(player)) {
                return;
            }

            // handle removing the existing entity?
            PacketContainer destroyPacket = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);

            // set ids
            int[] ids = this.spawnedEntities.stream().mapToInt(HologramEntity::getId).toArray();
            destroyPacket.getIntegerArrays().write(0, ids);

            Protocol.sendPacket(player, destroyPacket);
        }

        @Override
        public void setClickCallback(@Nullable Consumer<Player> clickCallback) {
            this.clickCallback = clickCallback;
        }

        @Override
        public void close() {
            despawn();
        }

        @Override
        public boolean isClosed() {
            return !this.spawned;
        }

        private HologramEntity getHologramEntity(int entityId) {
            for (HologramEntity entity : PacketHologram.this.spawnedEntities) {
                if (entity.getId() == entityId) {
                    return entity;
                }
            }
            return null;
        }

        private void setupPacketListeners() {
            this.listeners = CompositeTerminable.create();

            // remove players when they quit
            Events.subscribe(PlayerQuitEvent.class)
                    .handler(e -> this.viewers.remove(e.getPlayer()))
                    .bindWith(this.listeners);

            Protocol.subscribe(ListenerPriority.HIGH, PacketType.Play.Server.ENTITY_METADATA)
                    .handler(e -> {
                        PacketContainer packet = e.getPacket();
                        Player player = e.getPlayer();

                        // get entity id
                        int entityId = packet.getIntegers().read(0);

                        // find a matching hologram line
                        HologramEntity hologram = getHologramEntity(entityId);
                        if (hologram == null) {
                            return;
                        }

                        // get metadata
                        List<WrappedWatchableObject> metadata = new ArrayList<>(packet.getWatchableCollectionModifier().read(0));

                        // process metadata
                        for (WrappedWatchableObject value : metadata) {
                            if (value.getIndex() == 2) {
                                value.setValue(Text.colorize(hologram.getLine().resolve(player)));
                            } else {
                                // cache the metadata
                                hologram.getCachedMetadata().put(value.getIndex(), value);
                            }
                        }

                        if (!this.viewers.contains(player)) {
                            e.setCancelled(true);
                            return;
                        }

                        packet.getWatchableCollectionModifier().write(0, metadata);
                    })
                    .bindWith(this.listeners);

            Protocol.subscribe(ListenerPriority.HIGH, PacketType.Play.Server.SPAWN_ENTITY)
                    .handler(e -> {
                        PacketContainer packet = e.getPacket();
                        Player player = e.getPlayer();

                        // get entity id
                        int entityId = packet.getIntegers().read(0);

                        // find a matching hologram
                        HologramEntity hologram = getHologramEntity(entityId);
                        if (hologram == null) {
                            return;
                        }

                        if (!this.viewers.contains(player)) {
                            e.setCancelled(true);
                        }
                    })
                    .bindWith(this.listeners);

            Protocol.subscribe(ListenerPriority.HIGH, PacketType.Play.Client.USE_ENTITY)
                    .handler(e -> {
                        PacketContainer packet = e.getPacket();
                        Player player = e.getPlayer();

                        // get entity id
                        int entityId = packet.getIntegers().read(0);

                        // find a matching hologram
                        HologramEntity hologram = getHologramEntity(entityId);
                        if (hologram == null) {
                            return;
                        }

                        // always cancel interacts involving hologram objects
                        e.setCancelled(true);

                        if (this.clickCallback == null) {
                            return;
                        }

                        // if the player isn't a viewer, don't process the click
                        if (!this.viewers.contains(player)) {
                            return;
                        }

                        Location location = hologram.getArmorStand().getLocation();
                        if (player.getLocation().distance(location) > 5) {
                            return;
                        }

                        this.clickCallback.accept(player);
                    })
                    .bindWith(this.listeners);
        }
    }
}