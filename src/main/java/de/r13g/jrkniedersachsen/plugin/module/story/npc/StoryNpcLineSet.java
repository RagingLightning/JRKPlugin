package de.r13g.jrkniedersachsen.plugin.module.story.npc;

import de.r13g.jrkniedersachsen.plugin.Plugin;
import de.r13g.jrkniedersachsen.plugin.module.story.Story;
import de.r13g.jrkniedersachsen.plugin.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Method;
import java.util.*;

public class StoryNpcLineSet {

  public static final String NAME = "---StoryLineSet";

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
   * @param npc      Npc
   * @param p        Player to tell message to
   * @param callback Java Refelct method with boolean argument as callback
   * @return Last message in the chain
   */
  public void tell(StoryNpc npc, Player p, Method callback) {
    BukkitTask t = Bukkit.getScheduler().runTaskAsynchronously(Plugin.INSTANCE, () -> {
      boolean success = true;
      for (StoryNpcLine line : lines) {
        if (line.isJson)
          Util.tellRaw(p, line.message.replace("@p", p.getName()).replace("@s", npc.name));
        else
          p.sendMessage(line.message.replace("@p", p.getDisplayName()).replace("@s", npc.name));
        try {
          Thread.sleep(line.msDelayAfter);
        } catch (InterruptedException e) {
          success = false;
          break;
        }
      }
      boolean finalSuccess = success;
      Bukkit.getScheduler().runTask(Plugin.INSTANCE, () -> {
        try {
          callback.invoke(npc, p, finalSuccess);
        } catch (Exception e) {
          Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Failed to invoke callback method:", ChatColor.RED));
          e.printStackTrace();
        }
      });
    });
  }
}
