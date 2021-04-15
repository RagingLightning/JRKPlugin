package de.r13g.jrkniedersachsen.plugin.module.story.npc;

import de.r13g.jrkniedersachsen.plugin.Plugin;
import de.r13g.jrkniedersachsen.plugin.module.story.Story;
import de.r13g.jrkniedersachsen.plugin.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class StoryNpcLineSet {

  public transient Story story;

  List<StoryNpcLine> lines;
  UUID dependsOn;
  Map<UUID, Integer> unlocks;

  StoryNpcLineSet(StoryNpcLine line) {
    this.lines = new ArrayList<>();
    this.unlocks = new HashMap<>();
    lines.add(line);
  }

  StoryNpcLineSet() {
    this.lines = new ArrayList<>();
  }

  /**
   * tells all lines connected with nextLine to the Player
   *
   * @param p Player to tell message to
   * @return Last message in the chain
   */
  public boolean tell(StoryNpc npc, Player p) {
    final boolean[] success = {true};
    Bukkit.getScheduler().runTaskAsynchronously(Plugin.INSTANCE, () -> {
      for (StoryNpcLine line : lines) {
        if (line.isJson)
          Util.tellRaw(p, line.message.replaceAll("@p", p.getName()).replaceAll("@s", npc.name));
        else
          p.sendMessage(line.message.replaceAll("@p", p.getDisplayName()).replaceAll("@s", npc.name));
        try {
          Thread.sleep(line.msDelayAfter);
        } catch (InterruptedException e) {
          success[0] = false;
        }
      }
    });
    return success[0];
  }
}
