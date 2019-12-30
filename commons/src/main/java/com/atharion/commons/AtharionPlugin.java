package com.atharion.commons;

import com.atharion.commons.terminable.composite.CompositeTerminable;
import com.atharion.commons.terminable.module.TerminableModule;
import com.atharion.commons.utils.function.LoaderUtils;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;

public class AtharionPlugin extends JavaPlugin implements CompactPlugin {

    private CompositeTerminable compositeTerminable;

    protected void load() {
    }

    protected void enable() {
    }

    protected void disable() {
    }

    @Override
    public void onLoad() {
        LoaderUtils.getPlugin();
        this.compositeTerminable = CompositeTerminable.create();

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

    @Nonnull
    @Override
    public <T extends AutoCloseable> T bind(@Nonnull T terminable) {
        return this.compositeTerminable.bind(terminable);
    }

    @Nonnull
    @Override
    public <T extends TerminableModule> T bindModule(@Nonnull T module) {
        this.compositeTerminable.bindModule(module);
    }
}
