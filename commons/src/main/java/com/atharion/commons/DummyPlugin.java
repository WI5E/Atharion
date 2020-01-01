package com.atharion.commons;

import com.atharion.commons.hologram.BukkitHologramFactory;
import com.atharion.commons.hologram.HologramFactory;
import com.atharion.commons.hologram.individual.IndividualHologramFactory;
import com.atharion.commons.hologram.individual.PacketIndividualHologramFactory;
import com.atharion.commons.npc.CitizensNpcFactory;
import com.atharion.commons.npc.NpcFactory;
import com.atharion.commons.plugin.AtharionPlugin;
import com.atharion.commons.scheduler.HelperExecutors;
import com.atharion.commons.scoreboard.PacketScoreboardProvider;
import com.atharion.commons.scoreboard.ScoreboardProvider;
import com.atharion.commons.signprompt.PacketSignPromptFactory;
import com.atharion.commons.signprompt.SignPromptFactory;

public class DummyPlugin extends AtharionPlugin {

    @Override
    protected void enable() {
        provideService(HologramFactory.class, new BukkitHologramFactory());
        if (isPluginPresent("ProtocolLib")) {
            PacketScoreboardProvider scoreboardProvider = new PacketScoreboardProvider(this);
            provideService(ScoreboardProvider.class, scoreboardProvider);

            SignPromptFactory signPromptFactory = new PacketSignPromptFactory();
            provideService(SignPromptFactory.class, signPromptFactory);

            IndividualHologramFactory hologramFactory = new PacketIndividualHologramFactory();
            provideService(IndividualHologramFactory.class, hologramFactory);
        }
        if (isPluginPresent("Citizens")) {
            CitizensNpcFactory npcManager = bind(new CitizensNpcFactory());
            provideService(NpcFactory.class, npcManager);
        }
    }

    @Override
    protected void disable() {
        HelperExecutors.shutdown();
    }
}
