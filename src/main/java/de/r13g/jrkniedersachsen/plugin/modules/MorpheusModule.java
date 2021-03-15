package de.r13g.jrkniedersachsen.plugin.modules;

import de.r13g.jrkniedersachsen.plugin.Plugin;
import de.r13g.jrkniedersachsen.plugin.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class MorpheusModule implements Module, Listener {

  public static final String NAME = "Morpheus";

  public static final String CFGKEY_Percentage = "modules.Morpheus.percentage";
  public static final String CFGKEY_EnterBedMessage = "modules.Morpheus.enterBedMessage";
  public static final String CFGKEY_LeaveBedMessage = "modules.Morpheus.leaveBedMessage";
  public static final String CFGKEY_SleepSuccessMessage = "modules.Morpheus.sleepSuccessMessage";

  public static final String PERM_MorpheusAdmin = "jrk.morpheus.admin";
  public static final String PERM_MorpheusBypass = "jrk.morpheus.bypass";

  Instant leaveBedSuppression;
  BukkitTask wakeUpTask = null;

  FileConfiguration moduleConfig;
  File moduleDataFolder;

  private boolean ready = false;

  public MorpheusModule() { }

  @Override
  public boolean load(Plugin plugin, File moduleDataFolder) {
    leaveBedSuppression = Instant.now().minusSeconds(60);
    plugin.getServer().getPluginManager().registerEvents(this, plugin);

    ready = true;
    return true;
  }

  @Override
  public boolean unload() {
    ready = false;
    HandlerList.unregisterAll(this);
    return true;
  }

  @Override
  public boolean isReady() {
    return ready;
  }

  @EventHandler
  public void onPlayerBedEnter(PlayerBedEnterEvent ev) {
    if (ev.getPlayer().hasPermission(PERM_MorpheusBypass)) return;
    if (ev.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) return;
    Plugin.INSTANCE.getServer().getScheduler().runTaskLater(Plugin.INSTANCE, () -> {
      double percentage = sleepingPercentage(ev.getPlayer().getWorld());
      checkAndDoNightSkip(percentage, ev.getPlayer().getWorld());
      String message = String.format(Plugin.INSTANCE.getConfig().getString(CFGKEY_EnterBedMessage).replaceAll("\\$(\\d)","%$1\\$s"),
              ev.getPlayer().getName(), ""+(int)Math.floor(percentage)+"%");
      ev.getPlayer().getWorld().getPlayers().forEach(p -> p.sendMessage(ChatColor.GOLD + message));
      //Plugin.INSTANCE.getServer().broadcastMessage(ChatColor.GOLD + message);
    }, 10);
    /*double percentage = sleepingPercentage(ev.getPlayer().getWorld(), true);
    String message = String.format(Plugin.INSTANCE.getConfig().getString(CFGKEY_EnterBedMessage).replaceAll("\\$(\\d)","%$1\\$s"),
            ev.getPlayer().getName(), ""+Math.floor(percentage)+"%");
    Plugin.INSTANCE.getServer().broadcastMessage(ChatColor.GOLD + message);
    if (percentage >= Plugin.INSTANCE.getConfig().getDouble(CFGKEY_Percentage)) {
      wakeUpTask = Plugin.INSTANCE.getServer().getScheduler().runTaskLater(Plugin.INSTANCE, () -> {
        leaveBedSuppression = Instant.now().plusSeconds(10);
        Plugin.INSTANCE.getServer().broadcastMessage(ChatColor.GOLD + Plugin.INSTANCE.getConfig().getString(CFGKEY_SleepSuccessMessage));
        ev.getPlayer().getWorld().setTime(0);
      },6*20);
    }*/
  }

  @EventHandler
  public void onPlayerBedLeave(PlayerBedLeaveEvent ev) {
    if (ev.getPlayer().hasPermission(PERM_MorpheusBypass)) return;
    if (Duration.between(Instant.now(), leaveBedSuppression).isNegative()) {
      Plugin.INSTANCE.getServer().getScheduler().runTaskLater(Plugin.INSTANCE, () -> {
        double percentage = sleepingPercentage(ev.getPlayer().getWorld());
        checkAndDoNightSkip(percentage, ev.getPlayer().getWorld());
        String message = String.format(Plugin.INSTANCE.getConfig().getString(CFGKEY_LeaveBedMessage).replaceAll("\\$(\\d)","%$1\\$s"),
                ev.getPlayer().getName(), ""+(int)Math.floor(percentage)+"%");
        ev.getPlayer().getWorld().getPlayers().forEach(p -> p.sendMessage(ChatColor.GOLD + message));
        //Plugin.INSTANCE.getServer().broadcastMessage(ChatColor.GOLD + message);
      }, 10);
    }
    /*double percentage = sleepingPercentage(ev.getPlayer().getWorld(), false);
    if (wakeUpTask != null && percentage < Plugin.INSTANCE.getConfig().getDouble(CFGKEY_Percentage)) wakeUpTask.cancel();
    if (Duration.between(Instant.now(), leaveBedSuppression).isNegative()) {
      String message = String.format(Plugin.INSTANCE.getConfig().getString(CFGKEY_LeaveBedMessage).replaceAll("\\$(\\d)","%$1\\$s"),
              ev.getPlayer().getName(), ""+Math.floor(percentage)+"%");
      Plugin.INSTANCE.getServer().broadcastMessage(ChatColor.GOLD + message);
    }*/
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent ev) {
    checkAndDoNightSkip(sleepingPercentage(ev.getPlayer().getWorld()),ev.getPlayer().getWorld());
  }

  private void checkAndDoNightSkip(double pct, World w) {
    if (pct >= Plugin.INSTANCE.getConfig().getDouble(CFGKEY_Percentage)) {
      wakeUpTask = Plugin.INSTANCE.getServer().getScheduler().runTaskLater(Plugin.INSTANCE, () -> {
        leaveBedSuppression = Instant.now().plusSeconds(60);
        w.getPlayers().forEach(p -> p.sendMessage(ChatColor.GOLD + Plugin.INSTANCE.getConfig().getString(CFGKEY_SleepSuccessMessage)));
        //Plugin.INSTANCE.getServer().broadcastMessage(ChatColor.GOLD + Plugin.INSTANCE.getConfig().getString(CFGKEY_SleepSuccessMessage));
        w.setTime(0);
        w.setClearWeatherDuration((int)(Math.random()*(168000-12000)+12000));
        wakeUpTask = null;
      },4*20);
    } else if (wakeUpTask != null) {
      wakeUpTask.cancel();
      wakeUpTask = null;
    }
  }

  private double sleepingPercentage(World w) {
    double total = 0, sleeping = 0;
    for (Player p : w.getPlayers()) {
      if (!p.hasPermission(PERM_MorpheusBypass) && p.getGameMode() != GameMode.SPECTATOR) {
        total += 1;
        if (p.isSleeping()) sleeping += 1;
      }
    }
    return (sleeping/total) * 100;
  }

  @Override
  public List<String[]> getCommands() {
      List<String[]> commands = new ArrayList<>();
      commands.add(new String[]{"/morpheus","bypass","true","<player>"});
      commands.add(new String[]{"/morpheus","bypass","false","<player>"});
      commands.add(new String[]{"/morpheus","enterBedMessage","<$1:name,$2:pct>"});
      commands.add(new String[]{"/morpheus","leaveBedMessage","<$1:name,$2:pct>"});
      commands.add(new String[]{"/morpheus","percentage","50.0"});
      commands.add(new String[]{"/morpheus","sleepSuccessMessage","<$1:name,$2:pct>"});
      return commands;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      switch (args[0]) {
        case "bypass":
          if (Plugin.INSTANCE.moduleStatus(PermissionsModule.NAME)==1) {
            PermissionsModule pm = (PermissionsModule) Plugin.INSTANCE.getModule(PermissionsModule.NAME);
            if (!(sender instanceof Player) || sender.hasPermission(PERM_MorpheusAdmin)) {
              Player p;
              if (args.length == 2 && sender instanceof Player) {
                p = (Player) sender;
              } else if (args.length == 3) {
                p = Plugin.INSTANCE.getServer().getPlayerExact(args[2]);
              } else return false;
              if (p == null) {
                sender.sendMessage(Util.logLine(NAME, "Der Spieler konnte nicht gefunden werden", ChatColor.YELLOW));
                return true;
              }
              if (!(args[1].equals("true") || args[1].equals("false"))) return false;
                pm.playerAttachment(p.getUniqueId()).setPermission(PERM_MorpheusBypass, args[1].equals("true"));
                if (p.hasPermission(PERM_MorpheusBypass)) {
                  p.sendMessage(Util.logLine(NAME, "Du wirst ignoriert, " + sender.getName() + " wollte es so"));
                  sender.sendMessage(Util.logLine(NAME, p.getDisplayName() + " wird ignoriert."));
                } else {
                  p.sendMessage(Util.logLine(NAME, "Du wirst berücksichtigt, " + sender.getName() + " wollte es so"));
                  sender.sendMessage(Util.logLine(NAME, p.getDisplayName() + " wird berücksichtigt"));
                }
            }
          } else {
            sender.sendMessage(Util.logLine(NAME, "Für diesen Command wird das '" + PermissionsModule.NAME + "' Modul benötigt, welches aktuell nicht aktiviert ist."));
          }
          return true;
        case "percentage":
          if (!(sender instanceof Player) || sender.hasPermission(PERM_MorpheusAdmin)) {
            if (args.length == 2) {
              double d = 50;
              try {
                d = Double.parseDouble(args[1]);
              } catch (NumberFormatException e) {
                sender.sendMessage(Util.logLine(NAME, "Keine valide Prozantangebe (0-100)", ChatColor.YELLOW));
                return true;
              }
              Plugin.INSTANCE.getConfig().set(CFGKEY_Percentage,d);
              return true;
            }
          }
          return false;
        case "enterBedMessage":
          if (!(sender instanceof Player) || sender.hasPermission(PERM_MorpheusAdmin)) {
            if (args.length == 1) {
              sender.sendMessage("Fehlende Nachricht");
              //Plugin.INSTANCE.getConfig().set(CFGKEY_EnterBedMessage,"");
            } else {
              StringBuilder s = new StringBuilder();
              for (int i = 1; i < args.length; i++) {
                s.append(args[i]).append(" ");
              }
              Plugin.INSTANCE.getConfig().set(CFGKEY_EnterBedMessage, s.toString().trim());
              sender.sendMessage(Util.logLine(NAME, "Neue Nachricht übernommen, Vorschau:"));
              sender.sendMessage(ChatColor.GOLD + String.format(Plugin.INSTANCE.getConfig().getString(CFGKEY_EnterBedMessage).replaceAll("\\$(\\d)","%$1\\$s"),
                      sender.getName(), "33.0%"));
            }
          } else {
            sender.sendMessage(Util.logLine(NAME, "DU HAST NICHT DIE BENÖTIGTEN BERECHTIGUNGEN", ChatColor.RED));
          }
          return true;
        case "leaveBedMessage":
          if (!(sender instanceof Player) || sender.hasPermission(PERM_MorpheusAdmin)) {
            if (args.length == 1) {
              sender.sendMessage("Fehlende Nachricht");
              //Plugin.INSTANCE.getConfig().set(CFGKEY_LeaveBedMessage,"");
            } else {
              StringBuilder s = new StringBuilder();
              for (int i = 1; i < args.length; i++) {
                s.append(args[i]).append(" ");
              }
              Plugin.INSTANCE.getConfig().set(CFGKEY_LeaveBedMessage, s.toString().trim());
              sender.sendMessage("Neue Nachricht übernommen, Vorschau:");
              sender.sendMessage(ChatColor.GOLD + String.format(Plugin.INSTANCE.getConfig().getString(CFGKEY_LeaveBedMessage).replaceAll("\\$(\\d)","%$1\\$s"),
                      sender.getName(), "33.0%"));
            }
          } else {
            sender.sendMessage(Util.logLine(NAME, "DU HAST NICHT DIE BENÖTIGTEN BERECHTIGUNGEN", ChatColor.RED));
          }
          return true;
        case "sleepSuccessMessage":
          if (!(sender instanceof Player) || sender.hasPermission(PERM_MorpheusAdmin)) {
            if (args.length == 1) {
              sender.sendMessage("Fehlende Nachricht");
              //Plugin.INSTANCE.getConfig().set(CFGKEY_SleepSuccessMessage,"");
            } else {
              StringBuilder s = new StringBuilder();
              for (int i = 1; i < args.length; i++) {
                s.append(args[i]).append(" ");
              }
              Plugin.INSTANCE.getConfig().set(CFGKEY_SleepSuccessMessage, s.toString().trim());
              sender.sendMessage("Neue Nachricht übernommen, Vorschau:");
              sender.sendMessage(ChatColor.GOLD + Plugin.INSTANCE.getConfig().getString(CFGKEY_SleepSuccessMessage));
            }
          } else {
            sender.sendMessage(Util.logLine(NAME, "DU HAST NICHT DIE BENÖTIGTEN BERECHTIGUNGEN", ChatColor.RED));
          }
          return true;
    }
    return false;
  }

}
