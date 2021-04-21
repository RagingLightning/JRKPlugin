package de.r13g.jrkniedersachsen.plugin.module.story;

import de.r13g.jrkniedersachsen.plugin.Plugin;
import de.r13g.jrkniedersachsen.plugin.module.StoryModule;
import de.r13g.jrkniedersachsen.plugin.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Listener;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class StoryProgress {

  public static final String NAME = "--Progress";

  transient Story story;

  // Player -> Story -> Progress
  Map<UUID, PlayerProgressEntry> entries = new HashMap<>();

  public StoryProgress(Story story) {
    this.story = story;
  }

  public boolean register(OfflinePlayer player, PlayerProgressEntry progress) {
    //if (entries.containsKey(player.getUniqueId())) return false;
    progress.player = player;
    entries.put(player.getUniqueId(), progress);
    return true;
  }

  public PlayerProgressEntry get(OfflinePlayer player) {
    if (entries.containsKey(player.getUniqueId()))
      return entries.get(player.getUniqueId());
    return null;
  }

  public void load(OfflinePlayer p, File file) {
    try {
      PlayerProgressEntry entry;
      if (file.exists())
        entry = Story.gson.fromJson(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8), PlayerProgressEntry.class);
      else
        entry = new PlayerProgressEntry(p, story.getDefaultQuests());
      entry.progress = this;
      if (register(p, entry)) {
        Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Successfully loaded player save for Story id:" + story.getId()));
      } else {
        Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Successfully updated player save for story id:" + story.getId()));
      }
      entries.get(p.getUniqueId()).currentQuests.forEach((k, v) -> {
        StoryQuest q = story.getQuest(k);
        if (q.activePlayers.isEmpty()) {
          q.tasks.forEach((tk, tv) -> {
            if (tv instanceof Listener) {
              Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Registering Listeners for quest " + q.name + " task " + tk));
              Bukkit.getPluginManager().registerEvents((Listener) tv, Plugin.INSTANCE);
            }
          });
        }
        q.activePlayers.add(p.getUniqueId());
      });
    } catch (IOException e) {
      Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "<WARN> Unable to load player save for story id:" + story.getId()));
    }
  }

  public boolean unload() {
    entries.forEach((k, v) -> v.save(new File(StoryModule.saveDir, story.getId().toString() + "/" + k.toString() + ".json")));
    return true;
  }

}
