package de.r13g.jrkniedersachsen.plugin.modules.story;

import org.bukkit.entity.Player;

import java.util.*;

public class Story {

  public static Map<UUID, Story> registeredStories = new HashMap<>();

  UUID id;
  public String name = "<none>";
  public String desc = "<none>";

  public List<UUID> checkpoints = new ArrayList<>();
  public List<UUID> npcs = new ArrayList<>();

  public static Story get(UUID id) {
    if (registeredStories.containsKey(id))
      return registeredStories.get(id);
    return null;
  }

  public Story(String name) {
    this.name = name;
  }

}
