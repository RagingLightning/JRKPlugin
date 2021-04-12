package de.r13g.jrkniedersachsen.plugin.modules;

import com.google.gson.Gson;
import de.r13g.jrkniedersachsen.plugin.Plugin;
import de.r13g.jrkniedersachsen.plugin.modules.story.Story;
import de.r13g.jrkniedersachsen.plugin.modules.story.StoryNpc;
import de.r13g.jrkniedersachsen.plugin.modules.story.StoryProgress;
import de.r13g.jrkniedersachsen.plugin.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.Permissible;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class StoryModule implements Module, Listener {

  public static final String NAME = "Story";

  public static final String MDAT_StoryNpcLeast = "StoryNpcLeast";
  public static final String MDAT_StoryNpcMost = "StoryNpcMost";

  private File configFile;
  private FileConfiguration config;

  public File storyDir;
  public File saveDir;

  private boolean ready = false;

  @Override
  public boolean load(Plugin plugin, File moduleDataFolder) {
    configFile = new File(moduleDataFolder, "config.yml");
    storyDir = new File(moduleDataFolder, "stories");
    saveDir = new File(moduleDataFolder, "saves");

    if (!saveDir.exists()) {
      saveDir.mkdirs();
    }

    if (!storyDir.exists()) {
      storyDir.mkdirs();
    }
    try {
      config = YamlConfiguration.loadConfiguration(configFile);
      config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("story.yml"))));
      config.save(configFile);
    } catch (IOException e) {
      Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME + "/L", "Unable to load config, aborting loading process"));
      return false;
    }

    Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME + "/L", "Creating default configuration templates..."));
    createDefaultConfigurations(moduleDataFolder,
            new String[]{"defaultCheckpoint.json", "defaultItemDetectCheckpoint.json", "defaultNpc.json", "defaultNpcLine.json",
                    "defaultVillager.json", "defaultOffer.json", "defaultStory.json"});

    Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME + "/L", "Registering stories..."));
    for (File storyConfigFolder : storyDir.listFiles()) {
      UUID storyId = UUID.fromString(storyConfigFolder.getName());
      if (config.getStringList("stories").contains(storyId.toString())) {
        try {
          Story story = new Gson().fromJson(new FileReader(new File(storyConfigFolder, "story.json")), Story.class);
          story.storyRoot = storyConfigFolder;
          if (Story.register(storyId, story))
            Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME + "/L", "Successfully registered story " + story.name + " (id:" + storyId + ")"));
          else
            Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME + "/L", "Story " + Story.get(storyId) + " (id:" + storyId +
                    ") is already registered"));
        } catch (IOException e) {
          Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME + "/L", "Unable to register story id:" + storyId + ":", ChatColor.YELLOW));
          e.printStackTrace();
        }
      }
    }

    Story.loadAll();
    Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME + "/L", "All stories successfully loaded"));

    Bukkit.getPluginManager().registerEvents(this, plugin);

    ready = true;
    return true;
  }

  @Override
  public boolean unload() {
    ready = false;

    HandlerList.unregisterAll(this);

    //Story.unloadAll();

    return true;
  }

  @Override
  public boolean isReady() {
    return ready;
  }

  @Override
  public List<String[]> getCommands() {
    return null;
  }

  @Override
  public List<String> getHelpText(Permissible p) {
    return null;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    return false;
  }

  @EventHandler
  public void onPlayerInteractEntity(PlayerInteractEntityEvent ev) { //RIGHT CLICK
    if (ev.getRightClicked().hasMetadata(MDAT_StoryNpcLeast) && ev.getRightClicked().hasMetadata(MDAT_StoryNpcMost)) {
      ev.setCancelled(true);
      long most = ev.getRightClicked().getMetadata(MDAT_StoryNpcMost).get(0).asLong();
      long least = ev.getRightClicked().getMetadata(MDAT_StoryNpcLeast).get(0).asLong();
      UUID id = new UUID(most, least);
      StoryNpc npc = StoryNpc.get(id);
      if (npc == null) {
        Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "<WARN> Entity has Story Metadata, but no StoryNpc id:" + id.toString() + " exists", ChatColor.YELLOW));
      } else {
        npc.onPlayerInteractEntity(ev);
      }
    }
  }

  @EventHandler
  public void onEntityDamage(EntityDamageEvent ev) { //LEFT CLICK
    if (ev.getEntity().hasMetadata(MDAT_StoryNpcLeast) && ev.getEntity().hasMetadata(MDAT_StoryNpcMost)) {
      long most = ev.getEntity().getMetadata(MDAT_StoryNpcMost).get(0).asLong();
      long least = ev.getEntity().getMetadata(MDAT_StoryNpcLeast).get(0).asLong();
      UUID id = new UUID(most, least);
      StoryNpc npc = StoryNpc.get(id);
      if (npc == null) {
        Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "<WARN> Entity has Story Metadata, but no StoryNpc id:" + id.toString() + " exists", ChatColor.YELLOW));
      } else {
        npc.onEntityDamage(ev);
      }
    }
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent ev) {
    Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Loading player story progress..."));
    File playerSaveDir = new File(saveDir, ev.getPlayer().getUniqueId().toString());

    if (!playerSaveDir.exists()) {
      playerSaveDir.mkdirs();
      Story.registeredStories.forEach((k, v) -> {
        StoryProgress.register(v, ev.getPlayer(), new StoryProgress(v, ev.getPlayer()));
      });
    } else {
      for (File playerSave : playerSaveDir.listFiles()) {
        StoryProgress.load(ev.getPlayer(), playerSave);
      }
    }
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent ev) {
    if (StoryProgress.get(ev.getPlayer()) == null) return;
    Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Saving player story progress..."));
    File playerSaveDir = new File(saveDir, ev.getPlayer().getUniqueId().toString());
    if (!playerSaveDir.exists())
      throw new AssertionError("Player save folder doesn't exist, something went wrong on join");
    StoryProgress.get(ev.getPlayer()).forEach((k, v) -> {
      v.save(new File(playerSaveDir, k.toString() + ".json"));
    });

  }

  private void createDefaultConfigurations(File root, String[] defaults) {
    for (String s : defaults) {
      File f = new File(root, s);
      if (!f.exists()) {
        try {
          FileWriter w = new FileWriter(f);
          String def = new BufferedReader(
                  new InputStreamReader(Plugin.INSTANCE.getResource("story/" + s), StandardCharsets.UTF_8))
                  .lines().collect(Collectors.joining("\n"));
          w.write(def);
          w.flush();
          w.close();
        } catch (IOException e) {
          Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME + "/L", "Unable to create '" + s + "':", ChatColor.YELLOW));
          e.printStackTrace();
        }
      }
    }
  }
}
