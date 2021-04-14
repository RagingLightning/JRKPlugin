package de.r13g.jrkniedersachsen.plugin.module;


import de.r13g.jrkniedersachsen.plugin.Plugin;
import de.r13g.jrkniedersachsen.plugin.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.Permissible;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdminChatModule implements Module, Listener {

  public static final String NAME = "AdminChat";

  private final String PERM_ReadAdminMessages = "jrk.adminchat.read";
  private final String PERM_JoinSelf = "jrk.adminchat.join.self";
  private final String PERM_JoinOthers = "jrk.adminchat.join.others";

  private boolean ready = false;

  private List<UUID> inAdminChat = new ArrayList<>();

  @Override
  public boolean load(Plugin plugin, File moduleDataFolder) {

    plugin.getServer().getPluginManager().registerEvents(this, plugin);

    ready = true;
    return true;
  }

  @Override
  public boolean unload() {
    ready = false;

    HandlerList.unregisterAll(this);

    inAdminChat.forEach(u -> {
      Player p = Bukkit.getPlayer(u);
      if (p != null) p.sendMessage(Util.logLine(NAME, "--Du bist nicht mehr im Admin-Only Chat--", ChatColor.BOLD));
    });

    return true;
  }

  @Override
  public boolean isReady() {
    return ready;
  }

  @Override
  public List<String[]> getCommands() {
    List<String[]> commands = new ArrayList<>();
    commands.add(new String[]{"/adminchat", "<player>"});
    return commands;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

    if (!(sender instanceof Player)) {
      sender.sendMessage(Util.logLine(NAME, "Admin-Chat ist nur für Spieler verfügbar", ChatColor.YELLOW));
      return false;
    }

    if (args.length == 0 && sender.hasPermission(PERM_JoinSelf)) {
      if (inAdminChat.contains(((Player) sender).getUniqueId()))
        leaveAdminChat((Player) sender);
      else
        joinAdminChat((Player) sender);
      return true;
    }

    if (args.length == 1 && sender.hasPermission(PERM_JoinOthers)) {
      Player o = Plugin.INSTANCE.getServer().getPlayerExact(args[0]);
      if (o == null) {
        sender.sendMessage(Util.logLine(NAME, args[0] + "ist nicht online", ChatColor.YELLOW));
        return true;
      }

      if (inAdminChat.contains(o.getUniqueId()))
        leaveAdminChat(o);
      else
        joinAdminChat(o);
      return true;
    }

    return false;
  }

  @Override
  public List<String> getHelpText(Permissible p) {
    return new ArrayList<String>(){{add("Falsche Syntax, bitte ggf. an den Admin wenden");}};
  }

  @EventHandler
  public void onAsyncPlayerChat(AsyncPlayerChatEvent ev) {
    if (inAdminChat.contains(ev.getPlayer().getUniqueId())) {
      ev.setCancelled(true);
      List<UUID> received = new ArrayList<>();
      inAdminChat.forEach(u -> {
        Player r = Plugin.INSTANCE.getServer().getPlayer(u);
        if (r == null) inAdminChat.remove(u);
        else r.sendMessage("<" + ChatColor.DARK_RED + "ADMIN" + ChatColor.GRAY + "/" + ChatColor.RESET + ev.getPlayer().getName() + "> " + ev.getMessage());
        received.add(u);
      });
      Plugin.INSTANCE.getServer().getOnlinePlayers().forEach(p -> {
        if (p.hasPermission(PERM_ReadAdminMessages) && !received.contains(p.getUniqueId()))
          p.sendMessage("<" + ChatColor.DARK_RED + "ADMIN" + ChatColor.GRAY + "/" + ChatColor.RESET + ev.getPlayer().getName() + "> " + ev.getMessage());
      });
    }
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent ev) {
    leaveAdminChat(ev.getPlayer());
  }

  private void joinAdminChat(Player p) {
    if (!inAdminChat.contains(p.getUniqueId())) inAdminChat.add(p.getUniqueId());
    p.sendMessage(Util.logLine(NAME, "--Du bist jetzt im Admin-Only Chat--", ChatColor.BOLD));
  }

  private void leaveAdminChat(Player p) {
    if (inAdminChat.contains(p.getUniqueId())) inAdminChat.remove(p.getUniqueId());
    p.sendMessage(Util.logLine(NAME, "--Du bist nicht mehr im Admin-Only Chat--", ChatColor.BOLD));
  }
}
