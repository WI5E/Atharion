package com.atharion.commons.music.model;

import org.bukkit.Sound;

/**
 * A music note
 *
 * @author Utarwyn
 * @since 1.0.0
 */
public class Note {

	private Sound sound;

	private NotePitch pitch;

	public Note(byte instrumentKey, byte pitch) {
		this.sound = Sounds.of(instrumentKey);
		this.pitch = NotePitch.get(pitch - 33);
	}

	public Sound getSound() {
		return this.sound;
	}

	public NotePitch getPitch() {
		return this.pitch;
	}

}