package de.r13g.jrkniedersachsen.plugin.modules.story;

import java.util.*;

public class StoryCheckpoint {

  private static Map<UUID, StoryCheckpoint> registeredCheckpoints = new HashMap<>();

  protected transient Story containingStory;

  protected UUID id = null;
  protected String name = null;
  protected String desc = null;
  protected UUID parentCheckpoint = null;

  public static StoryCheckpoint get(UUID id) {
    if (registeredCheckpoints.containsKey(id))
      return registeredCheckpoints.get(id);
    return null;
  }

  public static List<StoryCheckpoint> get(List<UUID> ids) {
    List<StoryCheckpoint> a = new ArrayList<>();
    ids.forEach(id -> a.add(get(id)));
    return a;
  }

  public static List<UUID> getDefaults(Story story) {
    List<UUID> a = new ArrayList<>();
    registeredCheckpoints.forEach((k,v) -> {
      if (v.containingStory == story && v.parentCheckpoint == null) a.add(k);
    });
    return a;
  }

  public static boolean register(UUID id, StoryCheckpoint checkpoint) {
    if (registeredCheckpoints.containsKey(id)) return false;
    registeredCheckpoints.put(id, checkpoint);
    return true;
  }

  void unregister() {
    registeredCheckpoints.remove(id);
  }

  List<StoryCheckpoint> getChildren() {
    List<StoryCheckpoint> a = new ArrayList<>();
    registeredCheckpoints.forEach((k, v) -> {
      if (v.parentCheckpoint == id) a.add(v);
    });
    return a;
  }
}
