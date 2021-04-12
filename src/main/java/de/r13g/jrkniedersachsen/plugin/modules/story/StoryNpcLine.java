package de.r13g.jrkniedersachsen.plugin.modules.story;

import de.r13g.jrkniedersachsen.plugin.util.Util;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class StoryNpcLine {

  private static Map<UUID, StoryNpcLine> registeredLines = new HashMap<>();

  transient Story containingStory;

  UUID id;
  String message = null;
  boolean isJson = false;
  UUID nextLine = null;
  UUID dependsOn = null;
  List<UUID> unlocks = null;

  public static StoryNpcLine get(UUID id) {
    if (registeredLines.containsKey(id))
      return registeredLines.get(id);
    return null;
  }

  StoryNpcLine(Story story, String message, boolean isJson) {
    this.containingStory = story;
    this.message = message;
    this.isJson = isJson;
  }

  public static boolean register(UUID id, StoryNpcLine line) {
    if (registeredLines.containsKey(id)) return false;
    registeredLines.put(id, line);
    return true;
  }

  void unregister() {
    registeredLines.remove(id);
  }

  /**
   * tells all lines connected with nextLine to the Player
   *
   * @param p Player to tell message to
   * @return Last message in the chain
   */
  public StoryNpcLine tell(StoryNpc npc, Player p) {
    if (isJson) {
      Util.tellRaw(p, message);
    } else {
      p.sendMessage("<" + npc.name + "> " + message);
    }
    if (nextLine == null) return this;
    return StoryNpcLine.get(nextLine).tell(npc, p);
  }
}
