package com.atharion.commons.music.model;

import io.netty.util.collection.ByteObjectHashMap;
import io.netty.util.collection.ByteObjectMap;
import org.bukkit.Sound;

import javax.annotation.Nonnull;

public final class Sounds {

    private static final Sound DEFAULT = Sound.BLOCK_NOTE_BLOCK_HARP;

    private static final ByteObjectMap<Sound> INSTRUMENTS = new ByteObjectHashMap<>(16);

    static {
        INSTRUMENTS.put((byte) 0, DEFAULT);
        INSTRUMENTS.put((byte) 1, Sound.BLOCK_NOTE_BLOCK_BASS);
        INSTRUMENTS.put((byte) 2, Sound.BLOCK_NOTE_BLOCK_BASEDRUM);
        INSTRUMENTS.put((byte) 3, Sound.BLOCK_NOTE_BLOCK_SNARE);
        INSTRUMENTS.put((byte) 4, Sound.BLOCK_NOTE_BLOCK_HAT);
        INSTRUMENTS.put((byte) 5, Sound.BLOCK_NOTE_BLOCK_GUITAR);
        INSTRUMENTS.put((byte) 6, Sound.BLOCK_NOTE_BLOCK_FLUTE);
        INSTRUMENTS.put((byte) 7, Sound.BLOCK_NOTE_BLOCK_BELL);
        INSTRUMENTS.put((byte) 8, Sound.BLOCK_NOTE_BLOCK_CHIME);
        INSTRUMENTS.put((byte) 9, Sound.BLOCK_NOTE_BLOCK_XYLOPHONE);
        INSTRUMENTS.put((byte) 10, Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE);
        INSTRUMENTS.put((byte) 11, Sound.BLOCK_NOTE_BLOCK_COW_BELL);
        INSTRUMENTS.put((byte) 12, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO);
        INSTRUMENTS.put((byte) 13, Sound.BLOCK_NOTE_BLOCK_BIT);
        INSTRUMENTS.put((byte) 14, Sound.BLOCK_NOTE_BLOCK_BANJO);
        INSTRUMENTS.put((byte) 15, Sound.BLOCK_NOTE_BLOCK_PLING);
    }

    @Nonnull
    public static Sound of(byte key) {
        return INSTRUMENTS.getOrDefault(key, DEFAULT);
    }

    private Sounds() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}
