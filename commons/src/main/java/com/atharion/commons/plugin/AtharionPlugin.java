package com.atharion.commons.plugin;

import com.atharion.commons.Atharion;
import com.atharion.commons.Schedulers;
import com.atharion.commons.Services;
import com.atharion.commons.terminable.composite.CompositeTerminable;
import com.atharion.commons.terminable.module.TerminableModule;
import com.atharion.commons.utils.command.CommandMaps;
import com.atharion.commons.utils.function.LoaderUtils;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

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
        Schedulers.builder()
                .async()
                .after(10, TimeUnit.SECONDS)
                .every(30, TimeUnit.SECONDS)
                .run(this.compositeTerminable::cleanup)
                .bindWith(this.compositeTerminable);

        this.enable();
    }

    @Override
    public void onDisable() {
        //setup services

        this.disable();

        this.compositeTerminable.closeAndReportException();
    }

    @Nonnull
    @Override
    public <T extends AutoCloseable> T bind(@Nonnull T terminable) {
        return this.compositeTerminable.bind(terminable);
    }

    @Nonnull
    @Override
    public <T extends TerminableModule> T bindModule(@Nonnull T module) {
        return this.compositeTerminable.bindModule(module);
    }

    @Nonnull
    @Override
    public <T extends Listener> T registerListener(@Nonnull T listener) {
        Atharion.plugins().registerEvents(listener, this);
        return listener;
    }

    @Nonnull
    @Override
    public <T extends CommandExecutor> T registerCommand(@Nonnull T command, String permission, String permissionMessage, String description, @Nonnull String... aliases) {
        return CommandMaps.registerCommand(this, command, permission, permissionMessage, description, aliases);
    }

    @Nonnull
    @Override
    public <T> T getService(@Nonnull Class<T> service) {
        return Services.load(service);
    }

    @Nonnull
    @Override
    public <T> T provideService(@Nonnull Class<T> clazz, @Nonnull T instance, @Nonnull ServicePriority priority) {
        return Services.provide(clazz, instance, this, priority);
    }

    @Nonnull
    @Override
    public <T> T provideService(@Nonnull Class<T> clazz, @Nonnull T instance) {
        return provideService(clazz, instance, ServicePriority.Normal);
    }

    @Override
    public boolean isPluginPresent(@Nonnull String name) {
        return Atharion.plugins().getPlugin(name) != null;
    }

    @Nullable
    @Override
    public <T> T getPlugin(@Nonnull String name, @Nonnull Class<T> pluginClass) {
        return null;
    }

    private File getRelativeFile(@Nonnull String name) {
        getDataFolder().mkdirs();
        return new File(getDataFolder(), name);
    }

    @Nonnull
    @Override
    public File getBundledFile(@Nonnull String name) {
        Objects.requireNonNull(name, "name");
        File file = getRelativeFile(name);
        if (!file.exists()) {
            saveResource(name, false);
        }
        return file;
    }

    @Nonnull
    @Override
    public ClassLoader getClassloader() {
        return super.getClassLoader();
    }
}
