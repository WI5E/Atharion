package com.atharion.commons.quests.requirement;

import org.bukkit.entity.Player;

import java.util.function.Predicate;

@FunctionalInterface
public interface Requirement extends Predicate<Player> {}
