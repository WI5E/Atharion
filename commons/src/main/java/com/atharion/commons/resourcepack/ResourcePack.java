package com.atharion.commons.resourcepack;

import org.bukkit.NamespacedKey;

import javax.annotation.Nonnull;

public interface ResourcePack {

    String EMPTY_HASH = "";

    @Nonnull
    static ResourcePack create(@Nonnull String name, @Nonnull String url) {
        return create(name, url, EMPTY_HASH);
    }

    @Nonnull
    static ResourcePack create(@Nonnull String name, @Nonnull String url, @Nonnull String hash) {
        return new KeyedResourcePack(NamespacedKey.minecraft(name), url, hash.getBytes());
    }

    @Nonnull
    NamespacedKey getKey();

    @Nonnull
    String getUrl();

    byte[] getHash();
}