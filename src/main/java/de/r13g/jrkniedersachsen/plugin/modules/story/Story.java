package de.r13g.jrkniedersachsen.plugin.modules.story;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.datafixers.types.templates.Check;
import de.r13g.jrkniedersachsen.plugin.modules.StoryModule;
import de.r13g.jrkniedersachsen.plugin.util.Util;
import org.apache.logging.log4j.core.util.Assert;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.print.attribute.standard.MediaSize;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Story {

  private static final String NAME = StoryModule.NAME + "/Story";
  public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  public static Map<UUID, Story> registeredStories = new HashMap<>();

  public transient File storyRoot;


  UUID id;
  public String name;
  public String desc;

  public static boolean register(UUID id, Story story) {
    if (registeredStories.containsKey(id)) return false;
    registeredStories.put(id, story);
    return true;
  }

  public static Story get(UUID id) {
    if (registeredStories.containsKey(id))
      return registeredStories.get(id);
    return null;
  }

  public static void loadAll() {
    registeredStories.forEach((k,v) -> v.load());
  }

  public static void unloadAll() {
    registeredStories.forEach((k,v) -> v.unload());
  }

  public Story(String name) {
    this.name = name;
  }

  public boolean load() {
    Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Loading Story " + name + " (" + id + ")..."));

    File checkpointsDir = new File(storyRoot, "checkpoints");
    if (!checkpointsDir.exists()) {
      Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Story has no checkpoints folder, " +
              "asssuming there are none."));
    } else {
      Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Registering Story Checkpoints..."));
      for (File checkpointConfig : checkpointsDir.listFiles()) {
        UUID checkpointId = UUID.fromString(checkpointConfig.getName().replaceAll("\\.json",""));
        try {
          StoryCheckpoint checkpoint = new Gson().fromJson(new FileReader(checkpointConfig), StoryCheckpoint.class);
          checkpoint.containingStory = this;
          if (StoryCheckpoint.register(checkpointId, checkpoint)) {
            Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Checkpoint " + checkpoint.name + " (id:" + checkpointId + ") registered " +
                    "successfully"));
          } else {
            Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME,
                    "Checkpoint " + StoryCheckpoint.get(checkpointId).name + " (id:" + checkpointId + ") is already registered", ChatColor.YELLOW));
          }
        } catch (IOException e) {
          Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Unable to register Story Checkpoint id:" + checkpointId +":"));
          e.printStackTrace();
        }
      }
    }

    File npcDir = new File(storyRoot, "npcs");
    if (!npcDir.exists()) {
      Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Story has no NPC folder, " +
              "asssuming there are none."));
    } else {
      Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Registering Story NPCs..."));
      for (File npcConfig : npcDir.listFiles()) {
        UUID npcId = UUID.fromString(npcConfig.getName().replaceAll("\\.json",""));
        try {
          StoryNpc npc = gson.fromJson(new FileReader(npcConfig), StoryNpc.class);
          npc.containingStory = this;
          if (StoryNpc.register(npcId, npc)) {
            Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "NPC " + npc.name + " (id:" + npcId + ") registered " +
                    "successfully"));
          } else {
            Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME,
                    "NPC " + StoryCheckpoint.get(npcId).name + " (id:" + npcId + ") is already registered", ChatColor.YELLOW));
          }
        } catch (IOException e) {
          Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Unable to register Story NPC id:" + npcId +":"));
          e.printStackTrace();
        }
      }
    }

    StoryNpc.loadAll();

    File lineDir = new File(storyRoot, "lines");
    if (!lineDir.exists()) {
      Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Story has no NPC-Lines folder, " +
              "asssuming there are none."));
    } else {
      Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Registering Story NPC-Lines..."));
      for (File lineConfig : lineDir.listFiles()) {
        UUID lineId = UUID.fromString(lineConfig.getName().replaceAll("\\.json",""));
        try {
          StoryNpcLine line = gson.fromJson(new FileReader(lineConfig), StoryNpcLine.class);
          line.containingStory = this;
          if (StoryNpcLine.register(lineId, line)) {
            Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "NPC-Line id:" + lineId + " registered successfully"));
          } else {
            Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME,
                    "NPC-Line id:" + lineId + " is already registered", ChatColor.YELLOW));
          }
        } catch (IOException e) {
          Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Unable to register Story NPC-Line id:" + lineId +":"));
          e.printStackTrace();
        }
      }
    }

    File offerDir = new File(storyRoot, "offers");
    if (!offerDir.exists()) {
      Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Story has no NPC-Offers folder, " +
              "asssuming there are none."));
    } else {
      Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Registering Story NPC-Offers..."));
      for (File offerConfig : lineDir.listFiles()) {
        UUID offerId = UUID.fromString(offerConfig.getName().replaceAll("\\.json",""));
        try {
          StoryNpcOffer offer = gson.fromJson(new FileReader(offerConfig), StoryNpcOffer.class);
          if (StoryNpcOffer.register(offerId, offer)) {
            Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "NPC-Offer id:" + offerId + " registered successfully"));
          } else {
            Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME,
                    "NPC-Offer id:" + offerId + " is already registered", ChatColor.YELLOW));
          }
        } catch (IOException e) {
          Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Unable to register Story NPC-Offer id:" + offerId +":"));
          e.printStackTrace();
        }
      }
    }

    Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Story " + name + " (id:" + id + ") loaded successfully, reloading player saves..."));

    Bukkit.getOnlinePlayers().forEach(p -> {
      StoryProgress progress = StoryProgress.get(p, this);
      File progressFile = new File(storyRoot.getParentFile().getParentFile(), "saves/" + p.getUniqueId().toString() + "/" + id.toString() + ".json");
      if (progress != null) {
         progress.save(progressFile);
      }
      StoryProgress.load(p, progressFile);
    });
    return true;
  }

  public boolean unload() {
    throw new NotImplementedException();
  }

  public boolean reload() {
    unload();
    return load();
  }

}
