package com.atharion.commons.resourcepack;

import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public interface ResourcePackFactory {

    void sendResourcePack(@Nonnull Player player, @Nonnull ResourcePack resourcePack, @Nonnull ResponseHandler responseHandler);

    @FunctionalInterface
    interface ResponseHandler {

        /**
         * Handles the response
         *
         * @param lines the response content
         * @return the response
         */
        @Nonnull
        void handleResponse(@Nonnull Response response);
    }

    enum Response {
        SUCCESSFULLY_LOADED,
        DECLINED,
        FAILED_DOWNLOAD,
        ACCEPTED;
    }
}
