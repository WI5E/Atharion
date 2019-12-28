package com.atharion.commons;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public interface CompactPlugin extends Plugin {

    <T extends Listener> void registerListener(T listener);
}
