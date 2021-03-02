package de.r13g.jrkniedersachsen.plugin.modules;

import de.r13g.jrkniedersachsen.plugin.Plugin;
import de.r13g.jrkniedersachsen.plugin.util.Log;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;

public class ColorsModule implements Module, Listener {

  public static final String NAME = "Colors";

  private File configFile;
  private FileConfiguration config;

  HashMap<String, Team> teams = new HashMap<>();

  private boolean ready = false;

  @Override
  public boolean load(Plugin plugin, File moduleDataFolder) {
    configFile = new File(plugin.getDataFolder(), "colors.yml");
    config = YamlConfiguration.loadConfiguration(configFile);
    if (!configFile.exists()) {
      InputStream s = plugin.getResource("colors.yml");
      config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(s)));
      try {
        config.save(configFile);
      } catch (IOException e) {
        plugin.getServer().getConsoleSender().sendMessage(Log.logLine(NAME, "<WARN> unable to save config", ChatColor.YELLOW));
      }
    }
    plugin.getServer().getPluginManager().registerEvents(this, plugin);

    createTeamsFromConfig();
    plugin.getServer().getConsoleSender().sendMessage(Log.logLine(NAME, "<DEBG> TEAMS:"));
    for (String k : teams.keySet()) {
      plugin.getServer().getConsoleSender().sendMessage(Log.logLine(NAME, " - " + k + ": " + teams.get(k).getColor() + "COLOR"));
    }

    ready = true;
    return true;
  }

  @Override
  public boolean unload() {
    ready = false;
    try {
      config.save(configFile);
    } catch (IOException e) {
      Plugin.INSTANCE.getServer().getConsoleSender().sendMessage(Log.logLine(NAME, "<WARN> unable to save config", ChatColor.YELLOW));
    }
    HandlerList.unregisterAll(this);
    for (String k : teams.keySet()){
      teams.get(k).unregister();
    }
    return true;
  }

  @Override
  public boolean isReady() {
    return ready;
  }

  @Override
  public List<String> onTabComplete(TabCompleteEvent ev) {
    return null;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    return false;
  }

  private void createTeamsFromConfig() {
    if (teams.isEmpty()) {
      for (String key : config.getConfigurationSection("teams").getKeys(false)) {
        Team team = Plugin.INSTANCE.getServer().getScoreboardManager().getMainScoreboard().registerNewTeam(key);
        team.setColor(ChatColor.valueOf(config.getString("teams." + key)));
        teams.put(key, team);
      }
    } else {
      for (String key : teams.keySet()){
        teams.get(key).setColor(ChatColor.valueOf(config.getString("teams." + key)));
      }
    }
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent ev) {
    if (config.contains("players." + ev.getPlayer().getUniqueId())) {
      teams.get(config.getString("players." + ev.getPlayer().getUniqueId())).addEntry(ev.getPlayer().getName());
    } else {
      teams.get(config.getString("default")).addEntry(ev.getPlayer().getName());
    }
  }
}
