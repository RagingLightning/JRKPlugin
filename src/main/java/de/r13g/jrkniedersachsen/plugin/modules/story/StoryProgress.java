package de.r13g.jrkniedersachsen.plugin.modules.story;

import org.bukkit.entity.Player;

import java.util.*;

public class StoryProgress {

  private static Map<UUID, StoryProgress> registeredStoryProgresses = new HashMap<>();

  private Map<UUID, PlayerStoryProgress> playerProgresses = new HashMap<>();

  public static StoryProgress get(Story story) {
    if (registeredStoryProgresses.containsKey(story.id))
      return registeredStoryProgresses.get(story.id);
    return null;
  }

  public PlayerStoryProgress getPlayer(Player p) {
    if (playerProgresses.containsKey(p.getUniqueId()))
      return playerProgresses.get(p.getUniqueId());
    return null;
  }

}
