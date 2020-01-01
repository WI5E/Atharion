package com.atharion.commons.resourcepack;

import com.atharion.commons.packet.Protocol;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicBoolean;

public class PacketResourcePackFactory implements ResourcePackFactory {

    @Override
    public void sendResourcePack(@Nonnull Player player, @Nonnull ResourcePack resourcePack, @Nonnull ResponseHandler responseHandler) {
        player.setResourcePack(resourcePack.getUrl(), resourcePack.getHash());

        final AtomicBoolean active = new AtomicBoolean(true);

        Protocol.subscribe(PacketType.Play.Client.RESOURCE_PACK_STATUS)
                .filter(e -> e.getPlayer().equals(player))
                .biHandler((sub, event) -> {
                    if (!active.getAndSet(false)) {
                        return;
                    }
                    PacketContainer container = event.getPacket();

                    EnumWrappers.ResourcePackStatus resourcePackStatus = container.getResourcePackStatus().read(0);
                    responseHandler.handleResponse(Response.valueOf(resourcePackStatus.name()));

                    sub.close();
                });
    }
}
