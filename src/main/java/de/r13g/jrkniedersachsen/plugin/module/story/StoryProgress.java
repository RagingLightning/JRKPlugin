package de.r13g.jrkniedersachsen.plugin.module.story;

import de.r13g.jrkniedersachsen.plugin.Plugin;
import de.r13g.jrkniedersachsen.plugin.module.StoryModule;
import de.r13g.jrkniedersachsen.plugin.module.story.quest.QuestReward;
import de.r13g.jrkniedersachsen.plugin.module.story.quest.QuestTask;
import de.r13g.jrkniedersachsen.plugin.module.story.util.QuestSave;
import de.r13g.jrkniedersachsen.plugin.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.io.*;
import java.util.*;

public class StoryProgress {

  private static final String NAME = "--Progress";

  transient Story story;

  // Player -> Story -> Progress
  Map<UUID, PlayerEntry> entries = new HashMap<>();

  public StoryProgress(Story story) {
    this.story = story;
  }

  public boolean register(OfflinePlayer player, PlayerEntry progress) {
    //if (entries.containsKey(player.getUniqueId())) return false;
    progress.player = player;
    entries.put(player.getUniqueId(), progress);
    return true;
  }

  public PlayerEntry get(OfflinePlayer player) {
    if (entries.containsKey(player.getUniqueId()))
      return entries.get(player.getUniqueId());
    return null;
  }

  public void load(OfflinePlayer p, File file) {
    try {
      PlayerEntry progress;
      if (file.exists())
        progress = Story.gson.fromJson(new FileReader(file), PlayerEntry.class);
      else
        progress = new PlayerEntry(p, story.getDefaultQuests());
      if (register(p, progress)) {
        Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Successfully loaded player save for Story id:" + story.getId()));
      } else {
        Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Successfully updated player save for story id:" + story.getId()));
      }
    } catch (IOException e) {
      Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "<WARN> Unable to load player save for story id:" + story.getId()));
    }
  }

  public boolean unload() {
    entries.forEach((k, v) -> v.save(new File(StoryModule.saveDir, story.getId().toString() + "/" + k.toString() + ".json")));
    return true;
  }

  public class PlayerEntry {
    transient OfflinePlayer player;

    public Map<UUID, QuestSave> currentQuests;

    public PlayerEntry(OfflinePlayer p, List<StoryQuest> defaults) {
      this.currentQuests = new HashMap<>();
      this.player = p;
      unlock(defaults, null);
    }

    public void save(File file) {
      try {
        BufferedWriter w = new BufferedWriter(new FileWriter(file));
        String json = Story.gson.toJson(this);
        w.write(json);
        w.close();
        Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME,
                "Successfully saved player save for story id:" + file.getName().replaceAll("\\.json", "")));
      } catch (IOException e) {
        Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME,
                "<WARN> Unable to save player save for story id:" + file.getName().replaceAll("\\.json", "")));
      }
    }

    public void finishTask(StoryQuest quest, int taskId) {
      QuestSave save = currentQuests.get(quest.id);
      save.finishedTasks.add(taskId);
      boolean allTasks = true;
      for (int t : quest.tasks.keySet()) {
        if (!save.finishedTasks.contains(t)) {
          allTasks = false;
          break;
        }
      }
      if (allTasks)
        unlock(quest.getChildren(), quest);
    }

    public void unlock(StoryQuest quest, StoryQuest parent) {
      currentQuests.put(quest.id, new QuestSave());
      if (parent != null && currentQuests.keySet().contains(parent.id)) {
        currentQuests.remove(parent.id);
        parent.activePlayers.remove(player.getUniqueId());
        if (parent instanceof Listener && parent.activePlayers.size() == 0) {
          HandlerList.unregisterAll((Listener) parent);
        }
      }
      if (quest instanceof Listener) {
        if (quest.activePlayers.size() == 0)
          Bukkit.getPluginManager().registerEvents((Listener) quest, Plugin.INSTANCE);
        quest.activePlayers.add(player.getUniqueId());
      }
      if (player instanceof Player && quest.rewards != null) {
        quest.rewards.forEach(r -> r.reward((Player) player));
      }
    }

    public void unlock(List<StoryQuest> quests, StoryQuest parent) {
      quests.forEach(q -> unlock(q, parent));
    }
  }

}
