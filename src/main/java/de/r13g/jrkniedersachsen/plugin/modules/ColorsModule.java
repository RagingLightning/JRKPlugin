package de.r13g.jrkniedersachsen.plugin.modules;

import de.r13g.jrkniedersachsen.plugin.Plugin;
import de.r13g.jrkniedersachsen.plugin.util.Log;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

@SuppressWarnings({"unused", "deprecation"})
public class ColorsModule implements Module, Listener {

  public static final String NAME = "Colors";

  public static final String PERM_ColorsAdmin  = "jrk.colors.admin";

  private final List<String> colors = new ArrayList<>();

  private File configFile;
  private FileConfiguration config;

  HashMap<String, Team> teams = new HashMap<>();

  private boolean ready = false;

  @Override
  public boolean load(Plugin plugin, File moduleDataFolder) {
    configFile = new File(plugin.getDataFolder(), "colors.yml");
    if (!configFile.exists()) {
      try {
        YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("colors.yml"))).save(configFile);
      } catch (IOException e) {
        plugin.getServer().getConsoleSender().sendMessage(Log.logLine(NAME, "<WARN> unable to save config", ChatColor.YELLOW));
      }
    }
    config = YamlConfiguration.loadConfiguration(configFile);
    plugin.getServer().getPluginManager().registerEvents(this, plugin);

    createTeamsFromConfig();

    for (ChatColor c : ChatColor.values()) {
      if (c == ChatColor.RESET || c == ChatColor.MAGIC) continue;
      colors.add(c.toString());
    }

    for (Player p : plugin.getServer().getOnlinePlayers()) {
      applyPlayerColors(p);
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
  public List<String[]> getCommands() {
    List<String[]> commands = new ArrayList<>();
    commands.add(new String[]{"/colors","teams","<team>","<color>"});
    commands.add(new String[]{"/colors","players","<player>","<team>"});
    commands.add(new String[]{"/colors","players","<player>","default"});
    commands.add(new String[]{"/colors","default","<team>"});
    return commands;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length > 0){
      if (args[0].equals("teams")) {
        if (args.length == 1) { //colors teams -- List all teams with color
          sender.sendMessage(Log.logLine(NAME, "Liste der verfügbaren Teams: "));
          for (String s : getTeamOverwiew()) {
            sender.sendMessage(Log.logLine(NAME, " - " + s));
          }
        } else if (args.length == 2) { //colors teams <team> -- List all players in team Team
          if (!teams.containsKey(args[1])) {
            sender.sendMessage(Log.logLine(NAME, "Das Team " + args[1] + " existiert nicht!",ChatColor.YELLOW)); return true;
          }
          sender.sendMessage(Log.logLine(NAME, "Spieler in Team " + teams.get(args[1]).getColor() + args[1] + ChatColor.RESET + ":"));
          for (String s : getPlayerOverwiew(args[1])) {
            sender.sendMessage(Log.logLine(NAME, " - " + s));
          }
        } else if (args.length == 3) { //colors teams <team> <color> -- Set team color
          if (colors.contains(args[2])) {
            sender.sendMessage(Log.logLine(NAME, args[2] + " ist keine valide Farbe!",ChatColor.YELLOW)); return true;
          }
          if (!teams.containsKey(args[1])) {
            Team t = Plugin.INSTANCE.getServer().getScoreboardManager().getMainScoreboard().registerNewTeam(args[1]);
            teams.put(args[1], t);
            config.set("teams." + args[1], args[2]);
            sender.sendMessage(Log.logLine(NAME, "Neues Team " + ChatColor.valueOf(args[2]) + args[1] + ChatColor.RESET + " erstellt"));
          }
          teams.get(args[1]).setColor(ChatColor.valueOf(args[2]));
          for (String name : teams.get(args[1]).getEntries()){
            Player p = Plugin.INSTANCE.getServer().getPlayerExact(name);
            if (p != null) applyPlayerColors(p);
          }
          sender.sendMessage(Log.logLine(NAME, "Team " + args[1] + " ist jetzt " + ChatColor.valueOf(args[2]) + args[2]));
        } else return false;
        return true;
      } else if (args[0].equals("players")) {
        if (args.length == 1) return false;
        if (args.length == 2) { //colors players <player> -- get player's team
          OfflinePlayer p = Plugin.INSTANCE.getServer().getPlayerExact(args[1]);
          if (p == null)
            p = Plugin.INSTANCE.getServer().getOfflinePlayer(args[1]);
          String id = p.getUniqueId().toString();
          if (config.contains("players." + id)) {
            String team = config.getString("players." + id);
            sender.sendMessage(Log.logLine(NAME, "Spieler " + args[1] + " ist im Team " + teams.get(team).getColor() + team));
          } else {
            String team = config.getString("default");
            sender.sendMessage(Log.logLine(NAME, "Spieler " + args[1] + " ist keinem Team zugewiesen, er ist im Standardteam " + teams.get(team).getColor() + team));
          }
        } else if (args.length == 3) { //colors players <player> <team> -- assign player to team
          if (args[2].equals("default")) {
            OfflinePlayer p = Plugin.INSTANCE.getServer().getPlayerExact(args[1]);
            if (p == null)
              p = Plugin.INSTANCE.getServer().getOfflinePlayer(args[1]);
            String id = p.getUniqueId().toString();
            ConfigurationSection cOld = config.getConfigurationSection("players");
            ConfigurationSection cNew = config.createSection("players");
            cOld.getValues(false).forEach((k,v) -> {if (!k.equals(id)) cNew.set(k, v);});
            sender.sendMessage(Log.logLine(NAME, args[1] + " ist jetzt keinem Team mehr zugewiesen"));
            if (p instanceof Player) {
              applyPlayerColors((Player) p);
              if (p != sender)
                ((Player) p).sendMessage(Log.logLine(NAME, "Du bist jetzt keinem Team mehr zugewiesen"));
            }
          }
          if (!teams.containsKey(args[2])) {
            sender.sendMessage(Log.logLine(NAME, "Das Team " + args[2] + " existiert nicht!", ChatColor.YELLOW)); return true;
          }
          OfflinePlayer p = Plugin.INSTANCE.getServer().getPlayerExact(args[1]);
          if (p == null)
            p = Plugin.INSTANCE.getServer().getOfflinePlayer(args[1]);
          String id = p.getUniqueId().toString();
          config.set("players." + id, args[2]);
          sender.sendMessage(Log.logLine(NAME, args[1] + " ist jetzt im Team " + teams.get(args[2]).getColor() + args[2]));
          if (p instanceof Player) {
            applyPlayerColors((Player) p);
            if (p != sender)
              ((Player) p).sendMessage(Log.logLine(NAME, "Du bist jetzt im Team " + teams.get(args[2]).getColor() + args[2]));
          }
        } else return false;
        return true;
      } else if (args[0].equals("default")) {
        if (args.length == 1) { //colors default
          String team = config.getString("default");
          sender.sendMessage(Log.logLine(NAME, "Das Standardteam ist " + teams.get(team).getColor() + team));
        } else if (args.length == 2) { //colors default <team>
          if (!teams.containsKey(args[1])) {
            sender.sendMessage(Log.logLine(NAME, "Das Team " + args[1] + " existiert nicht!", ChatColor.YELLOW)); return true;
          }
          config.set("default", args[1]);
          sender.sendMessage(Log.logLine(NAME, "Das Team " + teams.get(args[1]).getColor() + args[1] + ChatColor.RESET + " ist jetzt das Standardteam"));
          for (Player p : Plugin.INSTANCE.getServer().getOnlinePlayers()) {
            if (!config.contains("players." + p.getUniqueId()))
              applyPlayerColors(p);
          }
        } else return false;
        return true;
      }
    }
    return false;
  }

  private void createTeamsFromConfig() {
    if (teams.isEmpty()) {
      for (String key : config.getConfigurationSection("teams").getKeys(false)) {
        String color = config.getString("teams." + key);
        Plugin.INSTANCE.getServer().getConsoleSender().sendMessage(Log.logLine(NAME, "Creating Team " + key + " with color " + color));
        Team team = Plugin.INSTANCE.getServer().getScoreboardManager().getMainScoreboard().registerNewTeam(key);
        team.setColor(ChatColor.valueOf(color));
        teams.put(key, team);
      }
    } else {
      for (String key : teams.keySet()){
        teams.get(key).setColor(ChatColor.valueOf(config.getString("teams." + key)));
      }
    }
  }

  private List<String> getTeamOverwiew() {
    List<String> overview = new ArrayList<>();
    for (Map.Entry<String, Team> e : teams.entrySet()) {
      overview.add(e.getKey() + ": " + e.getValue().getColor() + e.getValue().getColor().name());
    }
    return overview;
  }

  private List<String> getPlayerOverwiew(String team) {
    List<String> overview = new ArrayList<>();
    for (String id : config.getConfigurationSection("players").getKeys(false)) {
      if (config.getString("players." + id).equals(team)){
        Player p = Plugin.INSTANCE.getServer().getPlayer(UUID.fromString(id));
        String name = "id:" + id;
        if (p != null)
          name = p.getDisplayName();
        overview.add(name);
      }
    }
    return overview;
  }

  public List<String> getTeamNames() {
    return new ArrayList<>(teams.keySet());
  }

  private void applyPlayerColors(Player p) {
    String team = config.contains("players." + p.getUniqueId()) ? config.getString("players." + p.getUniqueId()) : config.getString("default");
    teams.get(team).addEntry(p.getName());
    p.setDisplayName(teams.get(team).getColor() + p.getName() + ChatColor.RESET);
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent ev) {
    applyPlayerColors(ev.getPlayer());
  }
}
