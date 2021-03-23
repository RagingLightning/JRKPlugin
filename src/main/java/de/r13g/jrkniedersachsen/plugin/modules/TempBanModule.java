package de.r13g.jrkniedersachsen.plugin.modules;

import de.r13g.jrkniedersachsen.plugin.Plugin;
import de.r13g.jrkniedersachsen.plugin.util.Util;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class TempBanModule implements Module {

  public static final String NAME = "TempBan";

  private static final String CFGKEY_Players = "players";           //Int
  private static final String CFGKEY_Warn_Levels = "warnLevels";    //List<String>

  private File configFile;
  private FileConfiguration config;

  private boolean ready = false;

  @Override
  public boolean load(Plugin plugin, File moduleDataFolder) {

    configFile = new File(moduleDataFolder.getParentFile(), "config.yml");
    if (!configFile.exists()) {
      try {
        YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("tempban.yml"))).save(configFile);
      } catch (IOException e) {
        plugin.getServer().getConsoleSender().sendMessage(Util.logLine(NAME, "<ERR> Could not initialize config, aborting...", ChatColor.RED));
        return false;
      }
    }
    config = YamlConfiguration.loadConfiguration(configFile);

    ready = true;
    return true;
  }

  @Override
  public boolean unload() {
    ready = false;

    try {
      config.save(configFile);
    } catch (IOException e) {
      Plugin.INSTANCE.getServer().getConsoleSender().sendMessage(Util.logLine(NAME, "<WARN> Could not save config", ChatColor.YELLOW));
    }

    return false;
  }

  @Override
  public boolean isReady() {
    return ready;
  }

  @Override
  public List<String[]> getCommands() {
    List<String[]> commands = new ArrayList<>();
    commands.add(new String[]{"/tempban", "list"});                           //List all currently active tempbans
    commands.add(new String[]{"/tempban", "ban", "<player>","<reason>"});                //Ban Player based on warning level
    commands.add(new String[]{"/tempban", "ban", "<player>", "<reason>", "<time>"});      //Ban Player with specified time
    commands.add(new String[]{"/tempban", "warn", "<player>", "get"});        //Get Current Warning Level - and resulting ban time - for Player
    commands.add(new String[]{"/tempban", "warn", "<player>", "set", "0"});    //Set Player Warning Level
    return commands;
  }

  @Override
  public List<String> getHelpText(Permissible p) {
    List<String> text = new ArrayList<>();
    text.add("--- " + NAME + "-Modul: Hilfe ---");
    text.add("");
    text.add("/tempban list - Listet alle aktiven tempBans auf");
    text.add("/tempban ban <player> - Bannt den Spieler für die Zeit seines Warn-Levels und erhöht dieses");
    text.add("/tempban ban <player> <time> - Bannt den Spieler für die angegebene Zeit");
    text.add("/tempban warn get <player> - Zeigt das Warnungslevel des Spielers an");
    text.add("/tempban warn set <player> <level> - Ändert das Warnungslevel des Spielers");
    return text;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length == 0) return false;
    if (args[0].equals("list") && args.length == 1) {
      sender.sendMessage(Util.logLine(NAME, "Liste aller 'bekannten' Spieler: "));
      for (String u : config.getConfigurationSection(CFGKEY_Players).getKeys(false)) {
        Player p = Plugin.INSTANCE.getServer().getPlayer(UUID.fromString(u));
        BanEntry e = Bukkit.getBanList(BanList.Type.NAME).getBanEntry(p.getName());
        int warnLevel = config.contains(CFGKEY_Players + "." + u) ? config.getInt(CFGKEY_Players + "." + u) : 0;
        if (e == null)
          sender.sendMessage(" - " + p.getName() + " ist nicht gebannt (Level " + warnLevel + ")");
        else if (e.getExpiration() == null)
          sender.sendMessage(" - " + p.getName() + " ist dauerhaft gebannt (Level " + warnLevel + ")");
        else
          sender.sendMessage(" - " + p.getName() + " ist bis " + e.getExpiration().toString() + " gebannt (Level " + warnLevel + ")");
      }
      return true;
    } else if (args[0].equals("ban") && args.length >= 3) {
      if (args.length == 3) {                                                                                                                         //ban <player> <reason>
        Player p = Plugin.INSTANCE.getServer().getPlayerExact(args[1]);
        if (p == null) {
          sender.sendMessage(args[1] + " ist nicht online");
          return true;
        }
        int warnLevel = config.contains(CFGKEY_Players + "." + p.getUniqueId().toString()) ?
                config.getInt(CFGKEY_Players + "." + p.getUniqueId()) : 0;
        String duration = config.getStringList(CFGKEY_Warn_Levels).get(warnLevel);
        Instant until = calcBanUntil(duration);
        config.set(CFGKEY_Players + "." + p.getUniqueId(), warnLevel+1);

        if (until == null) {
          Plugin.INSTANCE.getServer().getBanList(BanList.Type.NAME).addBan(p.getName(), "Perma-Bann, Grund: " + args[2],
                  null, "ADMIN");
          p.kickPlayer("Perma-Bann, Grund: " + args[2]);
        } else {
          Plugin.INSTANCE.getServer().getBanList(BanList.Type.NAME).addBan(p.getName(), "Temp-Bann bis " + until.toString() +
                  " Grund: " + args[2], Date.from(until), "ADMIN");
          p.kickPlayer("Temp-Bann bis " + until.toString() + " Grund: " + args[2]);
        }
      } else if (args.length == 4) {                                                                                                                  //ban <player> <reason> <time>
        Player p = Plugin.INSTANCE.getServer().getPlayerExact(args[1]);
        if (p == null) {
          sender.sendMessage(args[1] + " ist nicht online");
          return true;
        }
        Instant until = calcBanUntil(args[3]);
        if (until == null) {
          Plugin.INSTANCE.getServer().getBanList(BanList.Type.NAME).addBan(p.getName(), "Perma-Bann, Grund: " + args[2],
                  null, "ADMIN");
          p.kickPlayer("Perma-Bann, Grund: " + args[2]);
        } else {
          Plugin.INSTANCE.getServer().getBanList(BanList.Type.NAME).addBan(p.getName(), "Temp-Bann bis " + until.toString() +
                  " Grund: " + args[2], Date.from(until), "ADMIN");
          p.kickPlayer("Temp-Bann bis " + until.toString() + " Grund: " + args[2]);
        }
      }
      try {
        config.save(configFile);
      } catch (IOException e) {
        sender.sendMessage(Util.logLine(NAME, "<WARN> Der Bann ist noch nicht permanent, da die config nicht gespeichert werden konnte",
                ChatColor.YELLOW));
      }
      return true;
    } else if (args[0].equals("warn") && args.length >= 3) {
      if (args[1].equals("get") && args.length == 3) {                                                                                                //warn get <player>
        Player p = Plugin.INSTANCE.getServer().getPlayerExact(args[1]);
        if (p == null) {
          sender.sendMessage(args[1] + " ist nicht online");
          return true;
        }
        int level = config.contains(CFGKEY_Players + "." + p.getUniqueId()) ? config.getInt(CFGKEY_Players + "." + p.getUniqueId()) : 0;
        sender.sendMessage(Util.logLine(NAME, p.getDisplayName() + " ist im Warn-Level " + level));
      } else if (args[1].equals("set") && args.length == 4) {
        Player p = Plugin.INSTANCE.getServer().getPlayerExact(args[1]);
        if (p == null) {
          sender.sendMessage(args[1] + " ist nicht online");
          return true;
        }
        config.set(CFGKEY_Players + "." + p.getUniqueId(), Integer.parseInt(args[2]));
        sender.sendMessage(Util.logLine(NAME, p.getDisplayName() + " ist jetzt im Warn-Level " + args[2]));
      }
    }
    return false;
  }

  private Instant calcBanUntil(String duration) {
    if (duration.equalsIgnoreCase("p")) return null;
    HashMap<String, Integer> units = new HashMap<>();
    Pattern p = Pattern.compile("\\d+\\D");
    Matcher m = p.matcher(duration);
    int start = 0;
    while (m.find(start)) {
      String unit = m.group();
      String u = "" + unit.charAt(unit.length() - 1);
      int v = Integer.parseInt(unit.substring(0, unit.length() - 2));
      units.put(u.toUpperCase(), v);
    }
    Instant t = Instant.now();
    if (units.containsKey("D"))
      t = t.plusSeconds(units.get("D")*24*60*60);
    if (units.containsKey("H"))
      t = t.plusSeconds(units.get("H")*60*60);
    if (units.containsKey("M"))
      t = t.plusSeconds(units.get("M")*60);
    if (units.containsKey("S"))
      t = t.plusSeconds(units.get("S"));
    return t;
  }
}
