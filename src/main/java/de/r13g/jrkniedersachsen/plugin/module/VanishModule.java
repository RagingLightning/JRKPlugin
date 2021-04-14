package de.r13g.jrkniedersachsen.plugin.module;

import de.r13g.jrkniedersachsen.plugin.Plugin;
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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.Permissible;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class VanishModule implements Module, Listener {

  public static final String NAME = "Vanish";

  File configFile;
  FileConfiguration config;

  boolean ready = false;

  @Override
  public boolean load(Plugin plugin, File moduleDataFolder) {
    configFile = new File(plugin.getDataFolder(), "vanish.yml");

    try {
      config = YamlConfiguration.loadConfiguration(configFile);
      config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("story.yml"))));
      config.save(configFile);
    } catch (IOException e) {
      Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME + "/L", "Unable to load config, aborting loading process"));
      return false;
    }

    plugin.getServer().getPluginManager().registerEvents(this, plugin);

    ready = true;
    return true;
  }

  @Override
  public boolean unload() {
    ready = false;
    try {
      config.save(configFile);
    } catch (IOException e) {
      Plugin.INSTANCE.getServer().getConsoleSender().sendMessage(Util.logLine(NAME, "<WARN> unable to save config", ChatColor.YELLOW));
    }
    HandlerList.unregisterAll(this);

    /*for (String u : config.getStringList("players")) {
      unvanish(Plugin.INSTANCE.getServer().getPlayer(UUID.fromString(u)));
    }*/

    return true;
  }

  @Override
  public boolean isReady() {
    return ready;
  }

  @Override
  public List<String[]> getCommands() {
    List<String[]> commands = new ArrayList<>();
    commands.add(new String[]{"/" + NAME.toLowerCase(),"<player>"});
    return null;
  }

  @Override
  public List<String> getHelpText(Permissible p) {
    List<String> text = new ArrayList<>();
    text.add("--- " + NAME + "-Modul: Hilfe ---");
    text.add("");
    text.add("/vanish - Ändert den eigenen Status");
    text.add("/vanish <player> - Ändert den Status des Spielers");
    return text;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player)) return true;
    if (args.length == 0) {
      if (config.getConfigurationSection("players").getKeys(false).contains(((Player) sender).getUniqueId().toString()))
        unvanish((Player) sender, config.getBoolean("players." + ((Player) sender).getUniqueId().toString()));
      else
        vanish((Player) sender);
      return true;
    } else if (args.length == 1) {
      Player p = Plugin.INSTANCE.getServer().getPlayerExact(args[0]);
      if (p != null)
        if (config.getConfigurationSection("players").getKeys(false).contains(p.getUniqueId().toString()))
          unvanish(p, config.getBoolean("players." + p.getUniqueId().toString()));
        else
          vanish(p);
      return true;
    }
    return false;
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent ev) {
    Set<String> vanished = config.getConfigurationSection("players").getKeys(false);
    if (vanished.contains(ev.getPlayer().getUniqueId().toString())) {
      ev.setJoinMessage(null);
      ev.getPlayer().sendMessage(Util.logLine(NAME, "Du bist unsichtbar"));
      for (Player p : Plugin.INSTANCE.getServer().getOnlinePlayers()) {
        if (!vanished.contains(p.getUniqueId().toString()))
          p.hidePlayer(Plugin.INSTANCE,ev.getPlayer());
        if (p.isOp())
          p.sendMessage(Util.logLine(NAME, ev.getPlayer().getDisplayName() + " ist dem Spiel beigetreten, ist aber vanished."));
      }
    } else {
      for (Player p : Plugin.INSTANCE.getServer().getOnlinePlayers()) {
        if (vanished.contains(p.getUniqueId().toString()))
          ev.getPlayer().hidePlayer(Plugin.INSTANCE,p);
      }
    }
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent ev) {
    if (config.getConfigurationSection("players").getKeys(false).contains(ev.getPlayer().getUniqueId().toString())) {
      ev.setQuitMessage(null);
      for (Player p : Plugin.INSTANCE.getServer().getOnlinePlayers()) {
        if (p.isOp())
          p.sendMessage(Util.logLine(NAME, ev.getPlayer().getDisplayName() + " hat das Spiel verlassen, ist aber vanished."));
      }
    }
  }

  /*@EventHandler
  public void onServerListPing(ServerListPingEvent ev) {
    while (ev.iterator().hasNext()) {
      Player p = ev.iterator().next();
      if (config.getConfigurationSection("players").getKeys(false).contains(p.getUniqueId().toString()))
        ev.iterator().remove();
    }
  }*/

  private void unvanish(Player p, boolean bypassMorpheus) {
    config.set("players." + p.getUniqueId().toString(), null);
    for (Player o : Plugin.INSTANCE.getServer().getOnlinePlayers()) {
      o.showPlayer(Plugin.INSTANCE, p);
      if (o.isOp())
        o.sendMessage(Util.logLine(NAME, p.getDisplayName() + " ist sichtbar geworden"));
      if (config.getConfigurationSection("players").getKeys(false).contains(o.getUniqueId().toString()))
        p.hidePlayer(Plugin.INSTANCE, o);
      else
        p.showPlayer(Plugin.INSTANCE, o);
    }
    PermissionsModule pm = (PermissionsModule) Plugin.INSTANCE.getModule(PermissionsModule.NAME);
    if (!bypassMorpheus && pm != null) {
      pm.playerAttachment(p.getUniqueId()).setPermission(MorpheusModule.PERM_MorpheusBypass, false);
    }
    p.sendMessage(Util.logLine(NAME, "Du bist nicht mehr unsichtbar"));
    sendFakeJoin(p);
  }

  private void sendFakeJoin(Player p) {
    Plugin.INSTANCE.getServer().dispatchCommand(p, "tellraw @a {\"color\":\"yellow\",\"translate\":\"multiplayer.player.joined\",\"with\":[\"" + p.getName() + "\"]}");
  }

  private void sendFakeQuit(Player p) {
    Plugin.INSTANCE.getServer().dispatchCommand(p, "tellraw @a {\"color\":\"yellow\",\"translate\":\"multiplayer.player.left\",\"with\":[\"" + p.getName() + "\"]}");
  }

  private void vanish(Player p) {
    config.set("players." + p.getUniqueId().toString(), p.hasPermission(MorpheusModule.PERM_MorpheusBypass));
    for (Player o : Plugin.INSTANCE.getServer().getOnlinePlayers()) {
      p.showPlayer(Plugin.INSTANCE, o);
      if (o.isOp())
        o.sendMessage(Util.logLine(NAME, p.getDisplayName() + " ist unsichtbar geworden"));
      if (!config.getConfigurationSection("players").getKeys(false).contains(o.getUniqueId().toString()))
        o.hidePlayer(Plugin.INSTANCE, p);
      else
        o.showPlayer(Plugin.INSTANCE, p);
    }
    PermissionsModule pm = (PermissionsModule) Plugin.INSTANCE.getModule(PermissionsModule.NAME);
    if (pm != null) {
      pm.playerAttachment(p.getUniqueId()).setPermission(MorpheusModule.PERM_MorpheusBypass, true);
    }
    p.sendMessage(Util.logLine(NAME, "Du bist jetzt unsichtbar"));
    sendFakeQuit(p);
  }

}
