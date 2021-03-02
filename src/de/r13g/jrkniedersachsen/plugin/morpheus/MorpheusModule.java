package de.r13g.jrkniedersachsen.plugin.morpheus;

import de.r13g.jrkniedersachsen.plugin.Plugin;
import de.r13g.jrkniedersachsen.plugin.permissions.PermissionsModule;
import de.r13g.jrkniedersachsen.plugin.util.Log;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class MorpheusModule implements Listener {

  public static final String NAME = "Morpheus";

  public static final String CFGKEY_Percentage = "modules.morpheus.percentage";
  public static final String CFGKEY_EnterBedMessage = "modules.morpheus.enterBedMessage";
  public static final String CFGKEY_LeaveBedMessage = "modules.morpheus.leaveBedMessage";
  public static final String CFGKEY_SleepSuccessMessage = "modules.morpheus.sleepSuccessMessage";

  public static final String PERM_MorpheusAdmin = "jrk.morpheus.admin";
  public static final String PERM_MorpheusBypass = "jrk.morpheus.bypass";
  public static final String PERM_MorpheusMod = "jrk.morpheus.mod";

  Instant leaveBedSuppression;
  BukkitTask wakeUpTask = null;

  FileConfiguration moduleConfig;
  File moduleDataFolder;

  public MorpheusModule() {
    leaveBedSuppression = Instant.now().minusSeconds(60);

  }


  @EventHandler
  public void onPlayerBedEnter(PlayerBedEnterEvent ev) {
    if (ev.getPlayer().hasPermission(PERM_MorpheusBypass)) return;
    if (ev.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) return;
    double percentage = sleepingPercentage(ev.getPlayer().getWorld(), true);
    String message = String.format(Plugin.INSTANCE.getConfig().getString(CFGKEY_EnterBedMessage).replaceAll("\\$(\\d)","%$1\\$s"),
            ev.getPlayer().getDisplayName(), ""+Math.floor(percentage)+"%");
    Plugin.INSTANCE.getServer().broadcastMessage(ChatColor.GOLD + message);
    if (percentage >= Plugin.INSTANCE.getConfig().getDouble(CFGKEY_Percentage)) {
      wakeUpTask = Plugin.INSTANCE.getServer().getScheduler().runTaskLater(Plugin.INSTANCE, (Runnable) () -> {
        leaveBedSuppression = Instant.now().plusSeconds(10);
        Plugin.INSTANCE.getServer().broadcastMessage(ChatColor.GOLD + Plugin.INSTANCE.getConfig().getString(CFGKEY_SleepSuccessMessage));
        ev.getPlayer().getWorld().setTime(0);
      },6*20);
    }
  }

  @EventHandler
  public void onPlayerBedLeave(PlayerBedLeaveEvent ev) {
    if (ev.getPlayer().hasPermission(PERM_MorpheusBypass)) return;
    double percentage = sleepingPercentage(ev.getPlayer().getWorld(), false);
    if (wakeUpTask != null && percentage < Plugin.INSTANCE.getConfig().getDouble(CFGKEY_Percentage)) wakeUpTask.cancel();
    if (Duration.between(Instant.now(), leaveBedSuppression).isNegative()) {
      String message = String.format(Plugin.INSTANCE.getConfig().getString(CFGKEY_LeaveBedMessage).replaceAll("\\$(\\d)","%$1\\$s"),
              ev.getPlayer().getDisplayName(), ""+Math.floor(percentage)+"%");
      Plugin.INSTANCE.getServer().broadcastMessage(ChatColor.GOLD + message);
    }
  }

  private double sleepingPercentage(World w, boolean enter) {
    int total = 0;
    int sleeping = enter?1:-1;
    for (Player p : w.getPlayers()) {
      if (!p.hasPermission(PERM_MorpheusBypass)) {
        total++;
        if (p.isSleeping()) sleeping++;
      }
    }
    return 100.0 * sleeping / total;
  }

  public List<String> onTabComplete(TabCompleteEvent ev) {
      List<String> commands = new ArrayList<>();
      if (!(ev.getSender() instanceof Player) || ev.getSender().hasPermission(PERM_MorpheusAdmin)) {
        commands.add("/morpheus bypass true <player>");
        commands.add("/morpheus bypass false <player>");
        commands.add("/morpheus enterBedMessage <$1:name,$2:pct>");
        commands.add("/morpheus leaveBedMessage <$1:name,$2:pct>");
        commands.add("/morpheus percentage 50.0");
        commands.add("/morpheus sleepSuccessMessage <$1:name,$2:pct>");
      } else if (ev.getSender().hasPermission(PERM_MorpheusMod)) {
        commands.add("/morpheus bypass true <player>");
        commands.add("/morpheus bypass false <player>");
      }

      return commands;
  }

  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      switch (args[0]) {
        case "bypass": //TODO: restructure, bypass <player> [true|false] or [true|false]
          if (Plugin.INSTANCE.isModuleLoaded(PermissionsModule.NAME)) {
            if (!(sender instanceof Player) || sender.hasPermission(PERM_MorpheusMod)) {
              Player p = null;
              if (args.length == 2 && sender instanceof Player) {
                p = (Player) sender;
              } else if (args.length == 3) {
                p = Plugin.INSTANCE.getServer().getPlayerExact(args[2]);
              } else return false;
              if (p == null) {
                sender.sendMessage(Log.logLine(NAME, "Der Spieler konnte nicht gefunden werden", ChatColor.YELLOW));
                return true;
              }
              if (!(args[1].equals("true") || args[1].equals("false"))) return false;
                Plugin.permissionsModule.playerAttachment(p.getUniqueId()).setPermission(PERM_MorpheusBypass, args[1].equals("true"));
                if (p.hasPermission(PERM_MorpheusBypass)) {
                  sender.sendMessage(Log.logLine(NAME, p.getDisplayName() + " wird ignoriert."));
                } else {
                  sender.sendMessage(Log.logLine(NAME, p.getDisplayName() + " wird berücksichtigt"));
                }
            }
          } else {
            sender.sendMessage(Log.logLine(NAME, "Für diesen Command wird das '" + PermissionsModule.NAME + "' Modul benötigt, welches aktuell nicht aktiviert ist."));
          }
          return true;
        case "percentage":
          if (!(sender instanceof Player) || sender.hasPermission(PERM_MorpheusAdmin)) {
            if (args.length == 2) {
              double d = 50;
              try {
                d = Double.parseDouble(args[1]);
              } catch (NumberFormatException e) {
                sender.sendMessage(Log.logLine(NAME, "Keine valide Prozantangebe (0-100)", ChatColor.YELLOW));
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
              sender.sendMessage(Log.logLine(NAME, "Neue Nachricht übernommen, Vorschau:"));
              sender.sendMessage(ChatColor.GOLD + String.format(Plugin.INSTANCE.getConfig().getString(CFGKEY_EnterBedMessage).replaceAll("\\$(\\d)","%$1\\$s"),
                      sender.getName(), "33.0%"));
            }
          } else {
            sender.sendMessage(Log.logLine(NAME, "DU HAST NICHT DIE BENÖTIGTEN BERECHTIGUNGEN", ChatColor.RED));
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
            sender.sendMessage(Log.logLine(NAME, "DU HAST NICHT DIE BENÖTIGTEN BERECHTIGUNGEN", ChatColor.RED));
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
            sender.sendMessage(Log.logLine(NAME, "DU HAST NICHT DIE BENÖTIGTEN BERECHTIGUNGEN", ChatColor.RED));
          }
          return true;
    }
    return false;
  }

}
