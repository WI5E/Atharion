package com.atharion.commons.music;

import com.atharion.commons.music.model.Layer;
import com.atharion.commons.scheduler.Ticks;
import lombok.Getter;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

@Getter
public final class SimpleSong implements Song {

    private final Path path;
    private final String name;

    private final short length, height;
    private final float tempo, delay;

    private Int2ObjectMap<Layer> keyedLayers = new Int2ObjectOpenHashMap<>();

    public SimpleSong(Path path, String name, short length, short height, float tempo) {
        this.path = path;
        this.name = name;
        this.length = length;
        this.height = height;
        this.tempo = tempo;
        this.delay = Ticks.TICKS_PER_SECOND / tempo;
    }

    @Nonnull
    @Override
    public synchronized Layer getLayer(int key) {
        return this.keyedLayers.computeIfAbsent(key, Layer::new);
    }

    @Nonnull
    @Override
    public Collection<Layer> getLayers() {
        return Collections.unmodifiableCollection(this.keyedLayers.values());
    }
}
