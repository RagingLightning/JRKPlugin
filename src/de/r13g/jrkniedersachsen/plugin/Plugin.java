package de.r13g.jrkniedersachsen.plugin;

import de.r13g.jrkniedersachsen.plugin.morpheus.MorpheusModule;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Plugin extends JavaPlugin implements Listener {

  public static Plugin INSTANCE;

  private File permissionsFile;
  private FileConfiguration permissionsCfg;

  public HashMap<UUID, PermissionAttachment> attachments = new HashMap<>();

  private MorpheusModule morpheusModule;

  @Override
  public void onEnable() {
    super.onEnable();
    INSTANCE = this;
    this.getServer().getConsoleSender().sendMessage("Plugin des JRK-Servers wird geladen...");
    saveDefaultConfig();
    reloadConfig();
    permissionsFile = new File(getDataFolder(), "permissions.yml");
    permissionsCfg = YamlConfiguration.loadConfiguration(permissionsFile);
    this.getServer().getPluginManager().registerEvents(this, this);
    if (getConfig().getBoolean("modules.morpheus.enabled")) {
      this.getServer().getConsoleSender().sendMessage("[JRK] Modul 'Morpheus' wird geladen...");
      morpheusModule = new MorpheusModule();
      this.getServer().getPluginManager().registerEvents(morpheusModule, this);
    }
  }

  @Override
  public void onDisable() {
    super.onDisable();
    for (UUID k : attachments.keySet()) {
      savePermissions(k);
    }
    try {
      permissionsCfg.save(permissionsFile);
    }catch (IOException e) {
      this.getServer().getConsoleSender().sendMessage("[JRK] " + ChatColor.RED + "PERMISSIONS COULD NOT BE SAVED!");
    }
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (command.getName().startsWith("morpheus")) {
      return morpheusModule.onCommand(sender, command, label, args);
    } else {
      return false;
    }
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent ev) {
    loadPermissions(ev.getPlayer());
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent ev) {
    savePermissions(ev.getPlayer().getUniqueId());
    ev.getPlayer().removeAttachment(attachments.get(ev.getPlayer().getUniqueId()));
    attachments.remove(ev.getPlayer().getUniqueId());
  }

  private void loadPermissions(Player p) {
    PermissionAttachment a = p.addAttachment(this);
    for (String s : permissionsCfg.getStringList(p.getUniqueId().toString())) {
      a.setPermission(s.substring(1),s.startsWith("+"));
    }
    attachments.put(p.getUniqueId(), p.addAttachment(this));
  }

  private void savePermissions(UUID uuid) {
    PermissionAttachment a = attachments.get(uuid);
    List<String> list = new ArrayList<>();
    for (String s : a.getPermissions().keySet()) {
      list.add(a.getPermissions().get(s)?"+":"-" + s);
    }
    permissionsCfg.set(uuid.toString(), list);
  }
}
