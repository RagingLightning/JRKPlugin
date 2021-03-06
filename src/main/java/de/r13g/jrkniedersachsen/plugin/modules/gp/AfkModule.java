package de.r13g.jrkniedersachsen.plugin.modules.gp;

import de.r13g.jrkniedersachsen.plugin.Plugin;
import de.r13g.jrkniedersachsen.plugin.modules.Module;
import de.r13g.jrkniedersachsen.plugin.modules.MorpheusModule;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.permissions.Permissible;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AfkModule implements Module, Listener { //TODO: Test

  public static final String NAME = "Afk";

  public long timeout = 5;

  List<UUID> toBeAfk, isAfk;

  BukkitTask checkTask;

  @Override
  public boolean load(Plugin plugin, File moduleDataFolder) {

    toBeAfk = new ArrayList<>();
    isAfk = new ArrayList<>();

    checkTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
      for (Player p : plugin.getServer().getOnlinePlayers()) {
        if (toBeAfk.contains(p.getUniqueId()) && !isAfk.contains(p.getUniqueId())) {
          isAfk.add(p.getUniqueId());
          if (!p.hasPermission(MorpheusModule.PERM_MorpheusBypass))
            plugin.getServer().broadcastMessage(ChatColor.YELLOW + p.getName() + " ist AFK.");
          p.setDisplayName("" + ChatColor.ITALIC + ChatColor.GRAY + "[A]" + ChatColor.RESET + p.getDisplayName());
          p.setPlayerListName(p.getDisplayName());
        }
        if (!toBeAfk.contains(p.getUniqueId())) toBeAfk.add(p.getUniqueId());
      }
    },0L,timeout*60*20);

    plugin.getServer().getPluginManager().registerEvents(this, plugin);

    return true;
  }

  @Override
  public boolean unload() {

    HandlerList.unregisterAll(this);

    return true;
  }

  @Override
  public boolean isReady() {
    return true;
  }

  @Override
  public List<String[]> getCommands() {
    return new ArrayList<String[]>(){{add(new String[]{"/jrk","afk"});}};
  }

  @Override
  public List<String> getHelpText(Permissible p) {
    List<String> text = new ArrayList<>();
    text.add("--- " + NAME + "-Modul: Hilfe ---");
    text.add("");
    text.add("/jrk afk - Wechselt den AFK-Status");
    return text;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length != 1 || !(sender instanceof Player)) return false;
    toBeAfk.add(((Player) sender).getUniqueId());
    isAfk.add(((Player) sender).getUniqueId());
    if (!sender.hasPermission(MorpheusModule.PERM_MorpheusBypass))
      Plugin.INSTANCE.getServer().broadcastMessage(ChatColor.YELLOW + sender.getName() + " ist AFK.");
    ((Player) sender).setDisplayName("" + ChatColor.ITALIC + ChatColor.GRAY + "[A]" + ChatColor.RESET + ((Player) sender).getDisplayName());
    ((Player) sender).setPlayerListName(((Player) sender).getDisplayName());
    return true;
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent ev) {
    if (toBeAfk.contains(ev.getPlayer().getUniqueId())) toBeAfk.remove(ev.getPlayer().getUniqueId());
    if (isAfk.contains(ev.getPlayer().getUniqueId())) {
      isAfk.remove(ev.getPlayer().getUniqueId());
      if (!ev.getPlayer().hasPermission(MorpheusModule.PERM_MorpheusBypass))
        Plugin.INSTANCE.getServer().broadcastMessage(ChatColor.YELLOW + ev.getPlayer().getName() + " ist nicht mehr AFK.");
      ev.getPlayer().setDisplayName(ev.getPlayer().getDisplayName().replaceAll("" + ChatColor.ITALIC + ChatColor.GRAY + "\\[A]" + ChatColor.RESET,
              ""));
      ev.getPlayer().setPlayerListName(ev.getPlayer().getDisplayName());
    }
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent ev) {
    if (toBeAfk.contains(ev.getPlayer().getUniqueId())) toBeAfk.remove(ev.getPlayer().getUniqueId());
    if (isAfk.contains(ev.getPlayer().getUniqueId())) {
      isAfk.remove(ev.getPlayer().getUniqueId());
      if (!ev.getPlayer().hasPermission(MorpheusModule.PERM_MorpheusBypass))
        Plugin.INSTANCE.getServer().broadcastMessage(ChatColor.YELLOW + ev.getPlayer().getName() + " ist nicht mehr AFK.");
      ev.getPlayer().setDisplayName(ev.getPlayer().getDisplayName().replaceAll("" + ChatColor.ITALIC + ChatColor.GRAY + "\\[A]" + ChatColor.RESET,""));
      ev.getPlayer().setPlayerListName(ev.getPlayer().getDisplayName());
    }
  }
}
