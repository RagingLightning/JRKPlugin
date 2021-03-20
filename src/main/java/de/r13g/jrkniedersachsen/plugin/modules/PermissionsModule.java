package de.r13g.jrkniedersachsen.plugin.modules;

import de.r13g.jrkniedersachsen.plugin.Plugin;
import de.r13g.jrkniedersachsen.plugin.util.Util;
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
import org.bukkit.permissions.PermissionAttachment;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class PermissionsModule implements Module, Listener {

  public static final String NAME = "Permissions";

  private File playerFile;
  private FileConfiguration playerCfg;

  private File groupFile; //TODO: groups
  private FileConfiguration groupCfg;

  private HashMap<UUID, PermissionAttachment> attachments = new HashMap<>();

  boolean ready = false;

  public PermissionsModule() {}

  @Override
  public boolean load(Plugin plugin, File moduleDataFolder) {
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
    playerFile = new File(moduleDataFolder, "players.yml");
    playerCfg = YamlConfiguration.loadConfiguration(playerFile);
    if (!playerFile.exists()) {
      try {
        YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("playerPermissions.yml"))).save(playerFile);
      } catch (IOException e) {
        plugin.getServer().getConsoleSender().sendMessage(Util.logLine(NAME, "<WARN> unable to save player permissions", ChatColor.YELLOW));
      }
    }

    groupFile = new File(moduleDataFolder, "groups.yml");
    groupCfg = YamlConfiguration.loadConfiguration(groupFile);
    if (!groupFile.exists()) {
      try {
        YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("groupPermissions.yml"))).save(groupFile);
      } catch (IOException e) {
        plugin.getServer().getConsoleSender().sendMessage(Util.logLine(NAME, "<WARN> unable to save group permissions", ChatColor.YELLOW));
      }
    }
    ready = true;
    return true;
  }

  @Override
  public boolean isReady() {
    return ready;
  }

  @Override
  public boolean unload() {
    saveAllPermissions();

    ready = false;

    HandlerList.unregisterAll(this);
    attachments = new HashMap<>();
    playerFile = null;
    groupFile = null;
    playerCfg = null;
    groupCfg = null;
    return true;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    return false;
  }

  @Override
  public List<String[]> getCommands() {
    return new ArrayList<>();
  }

  @Override
  public List<String> getHelpText(Permissible p) {
    return new ArrayList<>();
  }

  public void loadPlayerPermissions(UUID u) {
    PermissionAttachment a = Plugin.INSTANCE.getServer().getPlayer(u).addAttachment(Plugin.INSTANCE);
    for (String s : playerCfg.getStringList(u.toString())) {
      a.setPermission(s.substring(1),s.startsWith("+"));
    }
    attachments.put(u, a);
  }

  public void savePlayerPermissions(UUID u) {
    PermissionAttachment a = attachments.get(u);
    List<String> list = new ArrayList<>();
    for (String s : a.getPermissions().keySet()) {
      if (s.equals("")) continue;
      list.add((a.getPermissions().get(s)?"+":"-") + s);
    }
    playerCfg.set(u.toString(), list);
  }

  public void saveAllPermissions() {
    for (UUID k : attachments.keySet()) {
      savePlayerPermissions(k);
    }
    try {
      playerCfg.save(playerFile);
    } catch (IOException e) {
      Plugin.INSTANCE.getServer().getConsoleSender().sendMessage(Util.logLine(NAME, "<WARN> Unable to save player permissions", ChatColor.YELLOW));
    }
  }

  public List<Player> listPlayersWithPermission(String permission) {
    List<Player> players = new ArrayList<>();
    boolean value = true;
    if (permission.startsWith("-")){
      permission = permission.substring(1);
      value = false;
    } else if (permission.startsWith("+"))
      permission = permission.substring(1);
    for (Player p : Plugin.INSTANCE.getServer().getOnlinePlayers()) {
      if (p.hasPermission(permission) == value)
        players.add(p);
    }
    return players;
  }

  public List<String> listPermissionsOfPlayer(Player p) {
    List<String> perms = new ArrayList<>();
    for (Map.Entry<String, Boolean> e: attachments.get(p.getUniqueId()).getPermissions().entrySet()) {
      perms.add(e.getKey() + ": " + (e.getValue()?"+":"-"));
    }
    return perms;
  }

  public PermissionAttachment playerAttachment(UUID u) {
    return attachments.get(u);
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent ev) {
    loadPlayerPermissions(ev.getPlayer().getUniqueId());
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent ev) {
    savePlayerPermissions(ev.getPlayer().getUniqueId());
    Plugin.INSTANCE.getServer().getPlayer(ev.getPlayer().getUniqueId()).removeAttachment(attachments.get(ev.getPlayer().getUniqueId()));
    attachments.remove(ev.getPlayer().getUniqueId());
  }

}
