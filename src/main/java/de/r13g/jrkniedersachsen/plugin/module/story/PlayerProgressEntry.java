package de.r13g.jrkniedersachsen.plugin.module.story;

import de.r13g.jrkniedersachsen.plugin.Plugin;
import de.r13g.jrkniedersachsen.plugin.module.story.quest.QuestTask;
import de.r13g.jrkniedersachsen.plugin.module.story.util.QuestSave;
import de.r13g.jrkniedersachsen.plugin.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class PlayerProgressEntry {
  transient StoryProgress progress;
  transient OfflinePlayer player;

  public List<UUID> finishedQuests;
  public Map<UUID, QuestSave> currentQuests;

  public PlayerProgressEntry(OfflinePlayer p, List<StoryQuest> defaults) {
    this.finishedQuests = new ArrayList<>();
    this.currentQuests = new HashMap<>();
    this.player = p;
    finishQuests(defaults);
  }

  public void save(@Nonnull File file) {
    try {
      for (UUID k : currentQuests.keySet()) {
        StoryQuest q = progress.story.getQuest(k);
        q.activePlayers.remove(player.getUniqueId());
        if (q.activePlayers.isEmpty()) {
          q.tasks.forEach((tk, tv) -> {
            if (tv instanceof Listener) {
              Bukkit.getConsoleSender().sendMessage(Util.logLine(StoryProgress.NAME,
                      "Unregistering Listeners for quest " + q.name + " task " + tk));
              HandlerList.unregisterAll((Listener) tv);
            }
          });
        }
      }

      BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
      String json = Story.gson.toJson(this);
      w.write(json);
      w.close();
      Bukkit.getConsoleSender().sendMessage(Util.logLine(StoryProgress.NAME,
              "Successfully saved player save for story id:" + file.getName().replace(".json", "")));
    } catch (IOException e) {
      Bukkit.getConsoleSender().sendMessage(Util.logLine(StoryProgress.NAME,
              "<WARN> Unable to save player save for story id:" + file.getName().replace(".json", "")));
    }
  }

  public Map<String, Object> getTaskData(QuestTask task) {
    return currentQuests.get(task.quest.id).tasks.get(task.id).data;
  }

  public void finishTask(QuestTask task) {
    Bukkit.getConsoleSender().sendMessage(Util.logLine(StoryProgress.NAME,
            "Finishing task " + task.id + " from quest " + task.quest.name + " (id:" + task.quest.id + ")..."));
    QuestSave save = currentQuests.get(task.quest.id);
    assert save != null : "Task is not active, check Story setup";
    save.tasks.get(task.id).finished = true;
    if (player instanceof Player && !(task.type == QuestTask.Type.EXTERNAL) && task.announceEnd)
      task.announceEnd((Player) player);
    boolean allTasks = true;
    for (int t : task.quest.tasks.keySet()) {
      if (!save.tasks.get(t).finished) {
        allTasks = false;
        break;
      }
    }
    if (allTasks) {
      Bukkit.getConsoleSender().sendMessage(Util.logLine(StoryProgress.NAME, "All Quest tasks for quest are finished"));
      finishQuest(task.quest);
    }
  }

  public void finishQuest(StoryQuest quest) {
    Bukkit.getConsoleSender().sendMessage(Util.logLine(StoryProgress.NAME, "Finishing quest " + quest.name + "..."));
    quest.getChildren().forEach(c -> {
      Bukkit.getConsoleSender().sendMessage(Util.logLine(StoryProgress.NAME, "Unlocking child quest " + c.name + "..."));
      if (c.activePlayers.isEmpty()) {
        c.tasks.forEach((k, v) -> {
          if (v instanceof Listener) {
            Bukkit.getConsoleSender().sendMessage(Util.logLine(StoryProgress.NAME, "Registering Listeners for child quest " + c.name + " task " + k));
            Bukkit.getPluginManager().registerEvents((Listener) v, Plugin.INSTANCE);
          }
        });
      }
      c.activePlayers.add(player.getUniqueId());
      if (player instanceof Player && c.announceStart) {
        ((Player) player).sendMessage("Neue Quest: " + c.name + "");
        c.tasks.values().forEach(t -> {
          if (!(t.type == QuestTask.Type.EXTERNAL) && t.announceStart) t.announceStart((Player) player);
        });
      }
      currentQuests.put(c.id, new QuestSave(c.tasks.keySet()));
    });

    currentQuests.remove(quest.id);
    finishedQuests.add(quest.id);
    quest.activePlayers.remove(player.getUniqueId());

    if (quest.activePlayers.isEmpty() && quest.tasks != null) {
      quest.tasks.forEach((tk, tv) -> {
        if (tv instanceof Listener) {
          Bukkit.getConsoleSender().sendMessage(Util.logLine(StoryProgress.NAME, "Unregistering Listeners for quest " + quest.name + " task " + tk));
          HandlerList.unregisterAll((Listener) tv);
        }
      });
    }

    if (quest.getParent() != null && finishedQuests.contains(quest.getParent().id)) {
      finishedQuests.remove(quest.getParent().id);
    }

    if (player instanceof Player && quest.rewards != null) {
      Bukkit.getConsoleSender().sendMessage(Util.logLine(StoryProgress.NAME, "Rewarding player..."));
      quest.rewards.forEach(r -> r.reward((Player) player));
    }
  }

  public void finishQuests(List<StoryQuest> quests) {
    quests.forEach(this::finishQuest);
  }
}
