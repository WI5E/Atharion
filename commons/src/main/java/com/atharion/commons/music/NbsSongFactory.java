package com.atharion.commons.music;

import com.atharion.commons.concurrent.promise.Promise;
import com.atharion.commons.music.model.Note;
import com.atharion.commons.utils.function.InputStreams;

import javax.annotation.Nonnull;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class NbsSongFactory implements SongFactory {

    private final Map<Path, Song> songCache = new ConcurrentHashMap<>();

    @Nonnull
    @Override
    public Promise<Song> load(@Nonnull Path path) {
        Objects.requireNonNull(path, "path");

        Song cached = this.songCache.get(path);

        if (cached != null) {
            Path cachedPath = cached.getPath();

            try {
                if (!this.mismatch(path, cachedPath)) {
                    return Promise.completed(cached);
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return Promise.start()
                .thenApplyAsync(aVoid -> {
                    Song song = null;
                    try {
                        try (InputStream fileInputStream = Files.newInputStream(path); DataInputStream dataInputStream = new DataInputStream(fileInputStream)) {
                            short length = 0, height = 0, tempo = 0;
                            String name = "undefined";

                            try {
                                length = InputStreams.readShort(dataInputStream);
                                // Max height of the music
                                height = InputStreams.readShort(dataInputStream);
                                // Music name
                                name = InputStreams.readString(dataInputStream);
                                // Author
                                InputStreams.readString(dataInputStream);
                                // OG Author
                                InputStreams.readString(dataInputStream);
                                // Description
                                InputStreams.readString(dataInputStream);
                                // Tempo
                                tempo = InputStreams.readShort(dataInputStream);
                                // Auto-save and more data not needed
                                dataInputStream.readByte();
                                dataInputStream.readByte();
                                dataInputStream.readByte();
                                InputStreams.readInt(dataInputStream, 5);
                                InputStreams.readString(dataInputStream);
                            } catch (Throwable throwable) {
                                throwable.printStackTrace();
                            }

                            song = new SimpleSong(path, name, length, height, tempo / 100.0F);

                            short tick = -1;

                            try {
                                while (true) {
                                    // Read all layers of the song
                                    short jumpsT = InputStreams.readShort(dataInputStream);
                                    if (jumpsT == 0) {
                                        break;
                                    }

                                    tick += jumpsT;

                                    short layer = -1;
                                    while (true) {
                                        // Read all notes for this layer
                                        short jumpsL = InputStreams.readShort(dataInputStream);
                                        if (jumpsL == 0) {
                                            break;
                                        }
                                        layer += jumpsL;

                                        // Read the instrument and the note pitch!
                                        byte instrument = dataInputStream.readByte();
                                        byte note = dataInputStream.readByte();

                                        song.getLayer(layer).addNote(tick, new Note(instrument, note));
                                    }
                                }
                            } catch (IOException exception) {
                                exception.printStackTrace();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    this.songCache.put(path, song);
                    return song;
                });
    }

    private boolean mismatch(Path path1, Path path2) throws IOException {
        byte[] f1 = Files.readAllBytes(path1);
        byte[] f2 = Files.readAllBytes(path2);
        return !Arrays.equals(f1, f2);
    }
}
