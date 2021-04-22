package de.r13g.jrkniedersachsen.plugin.module;

import com.google.gson.Gson;
import de.r13g.jrkniedersachsen.plugin.Plugin;
import de.r13g.jrkniedersachsen.plugin.customnpc.CustomPlayerPacketReader;
import de.r13g.jrkniedersachsen.plugin.customnpc.event.PlayerJoinQuitListener;
import de.r13g.jrkniedersachsen.plugin.module.story.Story;
import de.r13g.jrkniedersachsen.plugin.module.story.npc.StoryNpc;
import de.r13g.jrkniedersachsen.plugin.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class StoryModule implements Module, Listener {

  public static final String NAME = "Story";

  public static final String MDAT_StoryNpcLeast = "StoryNpcLeast";
  public static final String MDAT_StoryNpcMost = "StoryNpcMost";

  private File configFile;
  private FileConfiguration config;

  public static File storyDir;
  public static File saveDir;

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
    createDrafts(new File(moduleDataFolder, "templates"),
            new String[]{"defaultSimpleX.json", "defaultStory.json", "defaultQuest.json", "defaultQuestReward.json", "defaultQuestTask.json",
                    "defaultOffer.json", "defaultNpcLineSet.json", "defaultVillager.json"});

    Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME + "/L", "Registering stories..."));
    for (File storyConfigFolder : storyDir.listFiles()) {
      UUID storyId = UUID.fromString(storyConfigFolder.getName());
      try {
        Story story = new Gson().fromJson(new InputStreamReader(new FileInputStream(new File(storyConfigFolder, "story.json")), StandardCharsets.UTF_8), Story.class);
        if (!story.active) {
          Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME + "/L", "Didn't register story " + story.name + " (id:" + storyId + "), it was disabled"));
          continue;
        }
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

    Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME + "/L", "Story registration finished, loading stories..."));
    if (Story.loadAll())
      Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME + "/L", "All stories successfully loaded"));
    else
      Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME + "/L", "Some stories could not be loaded", ChatColor.YELLOW));

    Bukkit.getPluginManager().registerEvents(this, plugin);
    Bukkit.getPluginManager().registerEvents(new PlayerJoinQuitListener(), plugin);

    for (Player p : Bukkit.getOnlinePlayers()) {
      CustomPlayerPacketReader r = new CustomPlayerPacketReader();
      r.inject(p);
    }

    ready = true;
    return true;
  }

  @Override
  public boolean unload() {
    ready = false;

    for (Player p : Bukkit.getOnlinePlayers()) {
      CustomPlayerPacketReader.uninject(p);
    }

    HandlerList.unregisterAll(this);

    Story.unloadAll();

    return true;
  }

  @Override
  public boolean isReady() {
    return ready;
  }

  @Override
  public List<String[]> getCommands() {
    List<String[]> commands = new ArrayList<>();
    commands.add(new String[]{"/story", "reload", "<storyId>"});
    return commands;
  }

  @Override
  public List<String> getHelpText(Permissible p) {
    return new ArrayList<>();
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length == 0)
      return false;
    else {
      if (args[0].equalsIgnoreCase("reload")) {
        if (args.length == 1) {
          Story.unloadAll();
          if (Story.loadAll()) {
            sender.sendMessage(Util.logLine(NAME, "Alle Stories wurden neu geladen"));
          } else {
            sender.sendMessage(Util.logLine(NAME, "Beim neu laden der Stories ist ein Fehler aufgetreten", ChatColor.YELLOW));
          }
        } else {
          Story s = Story.get(UUID.fromString(args[1]));
          if (s != null) {
            s.unload();
            s.load();
          }
        }
      }
    }
    return false;
  }

  @EventHandler
  public void onPlayerInteractEntity(PlayerInteractEntityEvent ev) { //RIGHT CLICK
    if (ev.getRightClicked().hasMetadata(MDAT_StoryNpcLeast) && ev.getRightClicked().hasMetadata(MDAT_StoryNpcMost)) {
      ev.setCancelled(true);
      long most = ev.getRightClicked().getMetadata(MDAT_StoryNpcMost).get(0).asLong();
      long least = ev.getRightClicked().getMetadata(MDAT_StoryNpcLeast).get(0).asLong();
      UUID storyId = new UUID(most, 0);
      UUID npcId = new UUID(most, least);
      StoryNpc npc = Story.get(storyId).getNpc(npcId);
      if (npc == null) {
        Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "<WARN> Entity has Story Metadata, but no StoryNpc id:" + npcId.toString() + "exists", ChatColor.YELLOW));
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
      UUID storyId = new UUID(most, 0);
      UUID npcId = new UUID(most, least);
      StoryNpc npc = Story.get(storyId).getNpc(npcId);
      if (npc == null) {
        Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "<WARN> Entity has Story Metadata, but no StoryNpc id:" + npcId.toString() +
                "exists", ChatColor.YELLOW));
      } else {
        npc.onEntityDamage(ev);
      }
    }
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent ev) {
    Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Loading player story progress..."));
    Story.registeredStories.forEach((k, v) -> {
      File storyDir = new File(saveDir, k.toString());
      if (!storyDir.exists())
        storyDir.mkdirs();
      v.progress.load(ev.getPlayer(), new File(storyDir, ev.getPlayer().getUniqueId().toString() + ".json"));
    });
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent ev) {
    Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Saving player story progress..."));
    Story.registeredStories.forEach((k, v) -> {
      File storyDir = new File(saveDir, k.toString());
      if (!storyDir.exists())
        throw new AssertionError("Story id:" + k.toString() + " save folder doesn't exist, something went wrong on join");
      v.progress.get(ev.getPlayer()).save(new File(storyDir, ev.getPlayer().getUniqueId().toString() + ".json"));
    });

  }

  private void createDrafts(File root, String[] defaults) {
    if (!root.exists())
      root.mkdirs();
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
