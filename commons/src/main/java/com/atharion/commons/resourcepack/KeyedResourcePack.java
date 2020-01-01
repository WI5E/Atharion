package com.atharion.commons.resourcepack;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.NamespacedKey;

@RequiredArgsConstructor
@Getter
public class KeyedResourcePack implements ResourcePack {

    private final NamespacedKey key;
    private final String url;

    private final byte[] hash;
}
