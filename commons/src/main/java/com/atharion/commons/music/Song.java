package com.atharion.commons.music;

import com.atharion.commons.Services;
import com.atharion.commons.concurrent.promise.Promise;
import com.atharion.commons.music.model.Layer;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.Collection;

public interface Song {

    /**
     * Forms a {@link Promise<Song>} from the specified path
     * @param path target
     * @return the song
     */
    @Nonnull
    static Promise<Song> of(@Nonnull Path path) {
        return Services.load(NbsSongFactory.class).load(path);
    }

    /**
     * The path associated with this song
     * @return the path
     */
    @Nonnull
    Path getPath();

    /**
     * The name of the song
     * @return the name
     */
    @Nonnull
    String getName();

    /**
     * The length of the song in ticks
     * @return the length
     */
    short getLength();

    /**
     * The height of the song
     * @return the height
     */
    short getHeight();

    /**
     * The temp of the song
     * @return the tempo
     */
    float getTempo();

    /**
     * The delay of the song in respect to Minecraft's 20 ticks/second.
     * @return the delay
     */
    float getDelay();

    /**
     * Retrieves a layer from the specified key.
     * Creates a new layer if it doesn't already exist.
     * @param key
     * @return the layer
     */
    @Nonnull
    Layer getLayer(int key);

    /**
     * The layers of the song. Each layer contains
     * its own set of notes to play.
     * @return the layers
     */
    @Nonnull
    Collection<Layer> getLayers();
}
