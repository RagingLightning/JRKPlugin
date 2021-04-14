package de.r13g.jrkniedersachsen.plugin.module.story.npc;

import de.r13g.jrkniedersachsen.plugin.customnpc.CustomTrader;
import de.r13g.jrkniedersachsen.plugin.customnpc.CustomVillager;
import de.r13g.jrkniedersachsen.plugin.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.List;

public class StoryTrader extends StoryNpc {

  public static final String NAME = "--Trader";

  public List<StoryNpcOffer> offers;

  @Override
  public boolean load() {
    Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Loading StoryTrader " + name + " (id:" + id + ")..."));
    base = new CustomTrader(location.getLocation());
    behaviour.applyPathfinderGoals(base, 2);

    setup();

    Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "initialized, spawning..."));
    if (spawn()) {
      Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "spawned in " + location.world + " at x:" + location.x + " y:" +
              location.y + " z:" + location.z + " with entityId " + base.getUniqueID()));
      return true;
    } else {
      Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "failed to spawn", ChatColor.YELLOW));
      return true;
    }
  }

  @Override
  public boolean unload() {
    return despawn();
  }
}
