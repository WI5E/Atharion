package com.atharion.commons.music.model;

import lombok.RequiredArgsConstructor;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * A music layer
 *
 * @author Utarwyn
 * @since 1.0.0
 */
@RequiredArgsConstructor
public class Layer {

	private final int key;

	private final Int2ObjectMap<Note> notes = new Int2ObjectOpenHashMap<>();

	public int getKey() {
		return this.key;
	}

	@Nullable
	public Note getNote(int tick) {
		return this.notes.get(tick);
	}

	public synchronized void addNote(int tick, @Nonnull Note note) {
		Objects.requireNonNull(note, "note");
		this.notes.put(tick, note);
	}
}