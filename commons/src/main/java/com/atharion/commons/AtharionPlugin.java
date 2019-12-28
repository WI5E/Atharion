package com.atharion.commons;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class AtharionPlugin extends JavaPlugin implements Plugin {

    protected void load() {}
    protected void enable() {}
    protected void disable() {}

    @Override
    public void onLoad() {
        //setup services

        this.load();
    }

    @Override
    public void onEnable() {
        //setup services

        this.enable();
    }

    @Override
    public void onDisable() {
        //setup services

        this.disable();
    }
}
