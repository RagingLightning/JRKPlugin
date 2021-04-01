package de.r13g.jrkniedersachsen.plugin.modules;

import de.r13g.jrkniedersachsen.plugin.Plugin;
import de.r13g.jrkniedersachsen.plugin.util.Util;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.permissions.Permissible;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({"unused", "deprecation"})
public class TempBanModule implements Module {

  public static final String NAME = "TempBan";

  private static final String CFGKEY_Players = "players";           //Int
  private static final String CFGKEY_Warn_Levels = "levels";    //List<String>

  private File configFile;
  private FileConfiguration config;

  private boolean ready = false;

  @Override
  public boolean load(Plugin plugin, File moduleDataFolder) {

    configFile = new File(moduleDataFolder.getParentFile(), "tempban.yml");
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
    commands.add(new String[]{"/tempban", "ban", "<player>", "<time>", "<reason..>"});      //Ban Player with specified time
    commands.add(new String[]{"/tempban", "ban", "<player>", "0", "<reason..>"});   //ban player based on warning status, increasing it by 1
    commands.add(new String[]{"/tempban", "warn", "get", "<player>"});        //Get Current Warning Level - and resulting ban time - for Player
    commands.add(new String[]{"/tempban", "warn", "set", "<player>", "0"});    //Set Player Warning Level
    return commands;
  }

  @Override
  public List<String> getHelpText(Permissible p) {
    List<String> text = new ArrayList<>();
    text.add("--- " + NAME + "-Modul: Hilfe ---");
    text.add("");
    text.add("/tempban list - Listet die Verwarnungsstufen aller Spieler auf");
    text.add("/tempban ban <player> 0 <reason..> - Bannt den Spieler für die Zeit seines Verwarnungslevels und erhöht dieses");
    text.add("/tempban ban <player> <time> <reason..> - Bannt den Spieler für die angegebene Zeit");
    text.add("/tempban warn get <player> - Zeigt das Verwarnungslevel des Spielers an");
    text.add("/tempban warn set <player> <level> - Ändert das Verwarnungslevel des Spielers");
    return text;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length == 0) return false;
    if (args[0].equals("list") && args.length == 1) {
      sender.sendMessage(Util.logLine(NAME, "Liste aller 'bekannten' Spieler: "));
      for (String u : config.getConfigurationSection(CFGKEY_Players).getKeys(false)) {
        OfflinePlayer p = Plugin.INSTANCE.getServer().getOfflinePlayer(UUID.fromString(u));
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
      if (args.length >= 4) {     //ban <player> <time> <reason..>
        String playerName = args[1];

        OfflinePlayer p = Plugin.INSTANCE.getServer().getPlayerExact(args[1]);
        if (p == null) {
          p = Plugin.INSTANCE.getServer().getOfflinePlayer(args[1]);
          if(!Plugin.INSTANCE.getServer().getWhitelistedPlayers().contains(p))
            sender.sendMessage("NOTE: " + args[1] + " ist nicht auf der Whitelist, ist der Name korrekt?");
        }

        String duration = args[2];
        if (duration.equals("0")) {    //auto time from level
          int warnLevel = config.contains(CFGKEY_Players + "." + p.getUniqueId().toString()) ?
                  config.getInt(CFGKEY_Players + "." + p.getUniqueId()) : 0;
          Plugin.INSTANCE.getServer().getConsoleSender().sendMessage(Util.logLine(NAME, "<DEBG> Player " + p.getName() + " hat Warn-Level " + warnLevel));
          duration = config.getStringList(CFGKEY_Warn_Levels).get(warnLevel);
          config.set(CFGKEY_Players + "." + p.getUniqueId(), Math.min(warnLevel + 1, config.getStringList(CFGKEY_Warn_Levels).size()-1));
        }
        Instant until = calcBanUntil(duration);

        StringBuilder reason = new StringBuilder();
        for (int i = 3; i < args.length; i++) {
          reason.append(args[i]);
          reason.append(" ");
        }

        if (until == null) {
          sender.sendMessage(Util.logLine(NAME, p.getName() + " wird für immer gebannt"));
          Plugin.INSTANCE.getServer().getBanList(BanList.Type.NAME).addBan(p.getName(), "Perma-Bann, Grund: " + reason.toString().trim(),
                  null, "ADMIN");
          if (p instanceof Player) ((Player) p).kickPlayer("Perma-Bann, Grund: " + reason.toString().trim());
        } else {
          sender.sendMessage(Util.logLine(NAME, p.getName() + " wird bis " + until.toString() + "gebannt"));
          Plugin.INSTANCE.getServer().getBanList(BanList.Type.NAME).addBan(p.getName(), reason.toString().trim(), Date.from(until), "ADMIN");
          if (p instanceof Player) ((Player) p).kickPlayer("Temp-Bann bis " + until.toString().split("\\.")[0] + " Grund: " + reason.toString().trim());
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
        OfflinePlayer p = Plugin.INSTANCE.getServer().getPlayerExact(args[2]);
        if (p == null) {
          p = Plugin.INSTANCE.getServer().getOfflinePlayer(args[2]);
          if(!Plugin.INSTANCE.getServer().getWhitelistedPlayers().contains(p))
            sender.sendMessage("NOTE: " + args[2] + " ist nicht auf der Whitelist, ist der Name korrekt?");
        }
        int level = config.contains(CFGKEY_Players + "." + p.getUniqueId()) ? config.getInt(CFGKEY_Players + "." + p.getUniqueId()) : 0;
        sender.sendMessage(Util.logLine(NAME, p.getName() + " ist im Warn-Level " + level));
      } else if (args[1].equals("set") && args.length == 4) {
        if (Integer.parseInt(args[3]) >= config.getStringList(CFGKEY_Warn_Levels).size()){
          sender.sendMessage("So viele Verwarnungsstufen gibt es nicht!");
          return true;
        }
        OfflinePlayer p = Plugin.INSTANCE.getServer().getPlayerExact(args[2]);
        if (p == null) {
          p = Plugin.INSTANCE.getServer().getOfflinePlayer(args[2]);
          if(!Plugin.INSTANCE.getServer().getWhitelistedPlayers().contains(p))
            sender.sendMessage("NOTE: " + args[2] + " ist nicht auf der Whitelist, ist der Name korrekt?");
        }
        config.set(CFGKEY_Players + "." + p.getUniqueId(), Integer.parseInt(args[3]));
        sender.sendMessage(Util.logLine(NAME, p.getName() + " ist jetzt im Warn-Level " + args[3]));
      }
      return true;
    }
    return false;
  }

  private Instant calcBanUntil(String duration) {
    Plugin.INSTANCE.getServer().getConsoleSender().sendMessage(Util.logLine(NAME,
            "<DEBG> Ban Duration for TimeCode " + duration + " wird bestimmt..."));
    if (duration.equalsIgnoreCase("p")) return null;
    HashMap<String, Integer> units = new HashMap<>();
    Pattern p = Pattern.compile("\\d+\\D");
    Matcher m = p.matcher(duration);
    int start = 0;
    while (m.find(start)) {
      String unit = m.group();
      String u = "" + unit.charAt(unit.length() - 1);
      int v = Integer.parseInt(unit.substring(0, unit.length() - 1));
      Plugin.INSTANCE.getServer().getConsoleSender().sendMessage(Util.logLine(NAME,
              "<DEBG> Unit: '" + u + "', value: '" + v + "'"));
      units.put(u.toUpperCase(), v);
      start += m.group().length();
    }
    Instant t = Instant.now();
    if (units.containsKey("D"))
      t = t.plusSeconds(units.get("D")*24*60*60);
    Plugin.INSTANCE.getServer().getConsoleSender().sendMessage(Util.logLine(NAME,
            "<DEBG> End after n Days: " + t.toString()));
    if (units.containsKey("H"))
      t = t.plusSeconds(units.get("H")*60*60);
    Plugin.INSTANCE.getServer().getConsoleSender().sendMessage(Util.logLine(NAME,
            "<DEBG> End after n Hours: " + t.toString()));
    if (units.containsKey("M"))
      t = t.plusSeconds(units.get("M")*60);
    Plugin.INSTANCE.getServer().getConsoleSender().sendMessage(Util.logLine(NAME,
            "<DEBG> End after n Mins: " + t.toString()));
    if (units.containsKey("S"))
      t = t.plusSeconds(units.get("S"));
    Plugin.INSTANCE.getServer().getConsoleSender().sendMessage(Util.logLine(NAME,
            "<DEBG> End after n Secs: " + t.toString()));
    return t;
  }
}
