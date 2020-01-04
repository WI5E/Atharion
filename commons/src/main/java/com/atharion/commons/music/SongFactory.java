package com.atharion.commons.music;

import com.atharion.commons.concurrent.promise.Promise;

import javax.annotation.Nonnull;
import java.nio.file.Path;

public interface SongFactory {

    @Nonnull
    Promise<Song> loadSong(@Nonnull Path path);
}
