package de.r13g.jrkniedersachsen.plugin.modules.story;

import de.r13g.jrkniedersachsen.plugin.Plugin;
import de.r13g.jrkniedersachsen.plugin.modules.StoryModule;
import de.r13g.jrkniedersachsen.plugin.util.Util;
import org.apache.logging.log4j.core.util.Assert;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredListener;

import java.io.*;
import java.util.*;

public class StoryProgress {

  private static final String NAME = StoryModule.NAME + "/Progress";

  // Player -> Story -> Progress
  private static Map<UUID, Map<UUID, StoryProgress>> registeredProgresses = new HashMap<>();

  public transient UUID storyId;
  public transient UUID playerId;
  public List<UUID> checkpoints = new ArrayList<>();

  transient OfflinePlayer player;

  public StoryProgress(Story story, OfflinePlayer player) {
    this.storyId = story.id;
    this.playerId = player.getUniqueId();
    this.player = player;

    this.checkpoints = StoryCheckpoint.getDefaults(story);

  }

  public static boolean register(Story story, OfflinePlayer player, StoryProgress progress) {
    if (progress.playerId == null)
      throw new AssertionError("Tried to register StoryProgress without playerId");
    if (progress.storyId == null)
      throw new AssertionError("Tried to register StoryProgress without storyId");
    progress.player = Bukkit.getPlayer(progress.playerId);
    if (!registeredProgresses.containsKey(player.getUniqueId())) {
      Map<UUID, StoryProgress> p = new HashMap<>();
      p.put(story.id, progress);
      registeredProgresses.put(player.getUniqueId(), p);
      return true;
    } else if (registeredProgresses.get(player.getUniqueId()).containsKey(story.id)) {
      registeredProgresses.get(player.getUniqueId()).put(story.id, progress);
      return false;
    } else {
      registeredProgresses.get(player.getUniqueId()).put(story.id, progress);
      return true;
    }
  }

  public static StoryProgress get(OfflinePlayer player, Story story) {
    if (registeredProgresses.containsKey(player.getUniqueId()) && registeredProgresses.get(player.getUniqueId()).containsKey(story.id))
      return registeredProgresses.get(player.getUniqueId()).get(story.id);
    return null;
  }

  public static Map<UUID, StoryProgress> get(OfflinePlayer p) {
    if (registeredProgresses.containsKey(p.getUniqueId()))
      return registeredProgresses.get(p.getUniqueId());
    return null;
  }

  /**
   * determines, whether any player has a checkpoint active
   *
   * @param cp
   * @return
   */
  public static boolean checkpointActive(StoryCheckpoint cp) {
    for (UUID playerId : registeredProgresses.keySet()) {
      StoryProgress playerProgress = get(Bukkit.getOfflinePlayer(playerId), cp.containingStory);
      if (playerProgress != null && playerProgress.checkpoints.contains(cp.id))
        return true;
    }
    return false;
  }

  public static void load(OfflinePlayer p, File file) {
    UUID storyId = UUID.fromString(file.getName().replaceAll("\\.json", ""));
    if (Story.get(storyId) == null) {
      Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Player has save for unavailable story id:" + storyId + ", skipping"));
    } else {
      try {
        StoryProgress progress = Story.gson.fromJson(new FileReader(file), StoryProgress.class);
        progress.playerId = p.getUniqueId();
        progress.storyId = storyId;
        if (register(Story.get(storyId), p, progress)) {
          Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Successfully loaded player save for Story id:" + storyId));
        } else {
          Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Successfully updated player save for story id:" + storyId));
        }
      } catch (IOException e) {
        Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "<WARN> Unable to load player save for story id:" + storyId));
      }
    }
  }

  public void save(File file) {
    try {
      BufferedWriter w = new BufferedWriter(new FileWriter(file));
      String json = Story.gson.toJson(this);
      w.write(json);
      w.flush();
      w.close();
      Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Successfully saved player save for story id:" + storyId));
    } catch (IOException e) {
      Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "<WARN> Unable to save player save for story id:" + storyId));
    }
  }

  public void unlock(StoryCheckpoint cp) {
    checkpoints.add(cp.id);
    if (checkpoints.contains(cp.parentCheckpoint)) {
      checkpoints.remove(cp.parentCheckpoint);
      StoryCheckpoint parent = StoryCheckpoint.get(cp.parentCheckpoint);
      if (parent instanceof Listener && !StoryProgress.checkpointActive(parent)) {
        HandlerList.unregisterAll((Listener) parent);
      }
      if (cp instanceof Listener) {
        Bukkit.getPluginManager().registerEvents((Listener) cp, Plugin.INSTANCE);
      }
    }
    //TODO: Reward handling
  }

  public void unlock(List<StoryCheckpoint> cps) {
    cps.forEach(this::unlock);
  }

}
