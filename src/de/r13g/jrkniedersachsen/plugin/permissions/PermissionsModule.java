package de.r13g.jrkniedersachsen.plugin.permissions;

import de.r13g.jrkniedersachsen.plugin.Plugin;
import de.r13g.jrkniedersachsen.plugin.util.Log;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PermissionsModule implements Listener {

  public static final String NAME = "Permissions";

  private File playerFile;
  private FileConfiguration playerCfg;

  private File groupFile; //TODO: groups
  private FileConfiguration groupCfg;

  private HashMap<UUID, PermissionAttachment> attachments = new HashMap<>();

  public PermissionsModule(File baseFolder) {
    playerFile = new File(baseFolder, "players.yml");
    playerCfg = YamlConfiguration.loadConfiguration(playerFile);
    if (!playerFile.exists()) {
      InputStream s = Plugin.INSTANCE.getResource("playerPermissions.yml");
      playerCfg.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(s)));
      try {
        playerCfg.save(playerFile);
      } catch (IOException e) {
        Plugin.INSTANCE.getServer().getConsoleSender().sendMessage(Log.logLine(NAME, "<WARN> unable to save player permissions", ChatColor.YELLOW));
      }
    }

    groupFile = new File(baseFolder, "groups.yml");
    groupCfg = YamlConfiguration.loadConfiguration(groupFile);
    if (!groupFile.exists()) {
      InputStream s = Plugin.INSTANCE.getResource("groupPermissions.yml");
      groupCfg.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(s)));
      try {
        groupCfg.save(groupFile);
      } catch (IOException e) {
        Plugin.INSTANCE.getServer().getConsoleSender().sendMessage(Log.logLine(NAME, "<WARN> unable to save group permissions", ChatColor.YELLOW));
      }
    }
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
      list.add(a.getPermissions().get(s)?"+":"-" + s);
    }
    playerCfg.set(u.toString(), list);
    Plugin.INSTANCE.getServer().getPlayer(u).removeAttachment(attachments.get(u));
    attachments.remove(u);
  }

  public void saveAllPermissions() {
    for (UUID k : attachments.keySet()) {
      savePlayerPermissions(k);
    }
    try {
      playerCfg.save(playerFile);
    } catch (IOException e) {
      Plugin.INSTANCE.getServer().getConsoleSender().sendMessage(Log.logLine(NAME, "<WARN> Unable to save player permissions", ChatColor.YELLOW));
    }
  }

  public List<Player> listPlayersWithPermission(String permission) {
    return null; //TODO
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
  }

}
