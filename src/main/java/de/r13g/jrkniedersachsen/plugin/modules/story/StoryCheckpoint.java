package de.r13g.jrkniedersachsen.plugin.modules.story;

import de.r13g.jrkniedersachsen.plugin.modules.story.util.SimpleItem;

import java.util.*;

public class StoryCheckpoint {

  private static Map<UUID, StoryCheckpoint> registeredLines = new HashMap<>();

  UUID id = null;
  String name = null;
  String desc = null;
  UUID containingStory = null;
  UUID parentCheckpoint = null;
  SimpleItem itemDrop = null;

  public static StoryCheckpoint get(UUID id) {
    if (registeredLines.containsKey(id))
      return registeredLines.get(id);
    return null;
  }

  public static List<StoryCheckpoint> get(List<UUID> ids) {
    List<StoryCheckpoint> a = new ArrayList<>();
    ids.forEach(id -> a.add(get(id)));
    return a;
  }

}
