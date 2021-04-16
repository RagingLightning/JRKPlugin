package de.r13g.jrkniedersachsen.plugin.module.story;

import de.r13g.jrkniedersachsen.plugin.Plugin;
import de.r13g.jrkniedersachsen.plugin.module.StoryModule;
import de.r13g.jrkniedersachsen.plugin.module.story.quest.QuestTask;
import de.r13g.jrkniedersachsen.plugin.module.story.util.QuestSave;
import de.r13g.jrkniedersachsen.plugin.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.io.*;
import java.nio.charset.StandardCharsets;
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
        progress = Story.gson.fromJson(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8), PlayerEntry.class);
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

    public List<UUID> finishedQuests;
    public Map<UUID, QuestSave> currentQuests;

    public PlayerEntry(OfflinePlayer p, List<StoryQuest> defaults) {
      this.finishedQuests = new ArrayList<>();
      this.currentQuests = new HashMap<>();
      this.player = p;
      finishQuests(defaults);
    }

    public void save(File file) {
      try {
        BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
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

    public Map<String, Object> getTaskData(QuestTask task) {
      return currentQuests.get(task.quest.id).tasks.get(task.id).data;
    }

    public void finishTask(StoryQuest quest, int taskId) {
      Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Finishing task " + taskId + " from quest " + quest.name + " (id:" + quest.id + ")..."));
      QuestSave save = currentQuests.get(quest.id);
      save.tasks.get(taskId).finished = true;
      if (player instanceof Player && quest.tasks.get(taskId).announceEnd)
        quest.tasks.get(taskId).announceEnd((Player) player);
      boolean allTasks = true;
      for (int t : quest.tasks.keySet()) {
        if (!save.tasks.get(t).finished) {
          allTasks = false;
          break;
        }
      }
      if (allTasks) {
        Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "All Quest tasks for quest are finished"));
        finishQuest(quest);
      }
    }

    public void finishQuest(StoryQuest quest) {
      Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Finishing quest " + quest.name + "..."));
      quest.getChildren().forEach(c -> {
        Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Unlocking child quest " + c.name + "..."));
        if (player instanceof Player && c.announceStart) {
          ((Player) player).sendMessage("Neue Quest ''" + c.name + "':");
          c.tasks.values().forEach(t -> {
            if (t.announceStart) t.announceStart((Player) player);
          });
        }
        currentQuests.put(c.id, new QuestSave(c.tasks.keySet()));
      });
      currentQuests.remove(quest.id);
      quest.activePlayers.remove(player.getUniqueId());
      finishedQuests.add(quest.id);
      if (quest.getParent() != null && finishedQuests.contains(quest.getParent().id)) {
        finishedQuests.remove(quest.getParent().id);
      }


      if (quest instanceof Listener && quest.activePlayers.size() == 0) {
        Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Unregistering listener for quest..."));
        HandlerList.unregisterAll((Listener) quest);
      }
      quest.getChildren().forEach(c -> {
        if (c instanceof Listener) {
          if (c.activePlayers.size() == 0) {
            Bukkit.getPluginManager().registerEvents((Listener) c, Plugin.INSTANCE);
            Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Registering listener for child quest " + c.name + "..."));
          }
          c.activePlayers.add(player.getUniqueId());
        }
      });

      if (player instanceof Player && quest.rewards != null) {
        Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Rewarding player..."));
        quest.rewards.forEach(r -> r.reward((Player) player));
      }
    }

    public void finishQuests(List<StoryQuest> quests) {
      quests.forEach(this::finishQuest);
    }
  }

}
