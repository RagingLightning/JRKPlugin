package de.r13g.jrkniedersachsen.plugin.modules.story;

import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class StoryNpcLine {

  private static Map<UUID, StoryNpcLine> registeredLines = new HashMap<>();

  transient StoryNpc parentNpc;

  boolean isJson = false;
  String message = null;
  UUID nextLine = null;
  UUID dependsOn = null;
  List<UUID> unlocks = null;

  public static StoryNpcLine get(UUID id) {
    if (registeredLines.containsKey(id))
      return registeredLines.get(id);
    return null;
  }

  public StoryNpcLine(String message, boolean isJson) {
    this.message = message;
    this.isJson = isJson;
  }

  /**
   * tells all lines connected with nextLine to the Player
   *
   * @param p Player to tell message to
   * @return Last message in the chain
   */
  public StoryNpcLine tell(Player p) {
    if (isJson) {
      p.sendRawMessage(message);
    } else {
      p.sendMessage("<" + parentNpc.base.getCustomName() + "> " + message);
    }
    if (nextLine == null) return this;
    return StoryNpcLine.get(nextLine).tell(p);
  }
}
