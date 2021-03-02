package de.r13g.jrkniedersachsen.plugin.morpheus;

import de.r13g.jrkniedersachsen.plugin.Plugin;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.TabCompleteEvent;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class MorpheusModule implements Listener {

  public static final String CFGKEY_Percentage = "modules.morpheus.percentage";
  public static final String CFGKEY_EnterBedMessage = "modules.morpheus.enterBedMessage";
  public static final String CFGKEY_LeaveBedMessage = "modules.morpheus.leaveBedMessage";
  public static final String CFGKEY_SleepSuccessMessage = "modules.morpheus.sleepSuccessMessage";

  public static final String PERM_MorpheusAdmin = "jrk.morpheus.admin";
  public static final String PERM_MorpheusBypass = "jrk.morpheus.bypass";
  public static final String PERM_MorpheusMod = "jrk.morpheus.mod";

  Instant leaveBedSuppression;

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
      Plugin.INSTANCE.getServer().getScheduler().runTaskLater(Plugin.INSTANCE, (Runnable) () -> {
        Plugin.INSTANCE.getServer().broadcastMessage(ChatColor.GOLD + Plugin.INSTANCE.getConfig().getString(CFGKEY_SleepSuccessMessage));
        ev.getPlayer().getWorld().setTime(0);
      },6*20);
      leaveBedSuppression = Instant.now().plusSeconds(60);
    }
  }

  @EventHandler
  public void onPlayerBedLeave(PlayerBedLeaveEvent ev) {
    if (ev.getPlayer().hasPermission(PERM_MorpheusBypass)) return;
    double percentage = sleepingPercentage(ev.getPlayer().getWorld(), false);
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

  @EventHandler
  public void onTabComplete(TabCompleteEvent ev) {
    if (ev.getBuffer().startsWith("/morpheus")) {
      List<String> completions = new ArrayList<>();
      switch (ev.getBuffer().trim()) {
        case "/morpheus":
          if (!(ev.getSender() instanceof Player) || ev.getSender().hasPermission(PERM_MorpheusAdmin)) {
            completions.add("percentage");
            completions.add("enterBedMessage");
            completions.add("leaveBedMessage");
            completions.add("sleepSuccessMessage");
          }
          if (!(ev.getSender() instanceof Player) || ev.getSender().hasPermission(PERM_MorpheusMod)) {
            completions.add("bypass");
          }
          break;
        case "/morpheus bypass":
          completions.add("true"); completions.add("false");
          break;
        case "/morpheus bypass true":
        case "/morpheus bypass false":
          if (!(ev.getSender() instanceof Player) || ev.getSender().hasPermission(PERM_MorpheusMod)) {
            Plugin.INSTANCE.getServer().getWorlds().get(0).getPlayers().forEach(p -> completions.add(p.getDisplayName()));
          }
          break;
      }
      ev.setCompletions(completions);
    }
  }

  @EventHandler
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    Plugin.INSTANCE.getServer().getConsoleSender().sendMessage(args[0]);
      switch (args[0]) {
        case "bypass":
          if (!(sender instanceof Player) || sender.hasPermission(PERM_MorpheusMod)) {
            Player p = null;
            if (args.length == 2 && sender instanceof Player) {
              p = (Player) sender;
            } else if (args.length == 3) {
              p = Plugin.INSTANCE.getServer().getPlayerExact(args[2]);
            }
            if (p == null) {
              sender.sendMessage("Der angegebene Spieler wurde nicht gefunden.");
              return true;
            }
            if (args[1].equals("true") || args[1].equals("false")) {
              Plugin.INSTANCE.attachments.get(p.getUniqueId()).setPermission(PERM_MorpheusBypass, args[1].equals("true"));
              if (p.hasPermission(PERM_MorpheusBypass)) {
                sender.sendMessage("Der Spieler wird vom System ignoriert");
              } else {
                sender.sendMessage("Der Spieler wird vom System berücksichtigt");
              }
            }
          }
          break;
        case "percentage":
          if (!(sender instanceof Player) || sender.hasPermission(PERM_MorpheusAdmin)) {
            if (args.length == 2) {
              double d = 50;
              try {
                d = Double.parseDouble(args[1]);
              } catch (NumberFormatException e) {
                sender.sendMessage("Keine Valide Prozentangabe!");
                return true;
              }
              Plugin.INSTANCE.getConfig().set(CFGKEY_Percentage,d);
            }
          }
          break;
        case "enterBedMessage":
          if (!(sender instanceof Player) || sender.hasPermission(PERM_MorpheusAdmin)) {
            if (args.length == 1) {
              sender.sendMessage("Fehlende Nachricht");
              Plugin.INSTANCE.getConfig().set(CFGKEY_EnterBedMessage,"");
            } else {
              StringBuilder s = new StringBuilder();
              for (int i = 1; i < args.length; i++) {
                s.append(args[i]).append(" ");
              }
              Plugin.INSTANCE.getConfig().set(CFGKEY_EnterBedMessage, s.toString().trim());
              sender.sendMessage("Neue Benachrichtigung übernommen");
            }
          }
          break;
        case "leaveBedMessage":
          if (!(sender instanceof Player) || sender.hasPermission(PERM_MorpheusAdmin)) {
            if (args.length == 1) {
              sender.sendMessage("Fehlende Nachricht");
              Plugin.INSTANCE.getConfig().set(CFGKEY_LeaveBedMessage,"");
            } else {
              StringBuilder s = new StringBuilder();
              for (int i = 1; i < args.length; i++) {
                s.append(args[i]).append(" ");
              }
              Plugin.INSTANCE.getConfig().set(CFGKEY_LeaveBedMessage, s.toString().trim());
              sender.sendMessage("Neue Benachrichtigung übernommen");
            }
          }
          break;
        case "sleepSuccessMessage":
          if (!(sender instanceof Player) || sender.hasPermission(PERM_MorpheusAdmin)) {
            if (args.length == 1) {
              sender.sendMessage("Fehlende Nachricht");
              Plugin.INSTANCE.getConfig().set(CFGKEY_SleepSuccessMessage,"");
            } else {
              StringBuilder s = new StringBuilder();
              for (int i = 1; i < args.length; i++) {
                s.append(args[i]).append(" ");
              }
              Plugin.INSTANCE.getConfig().set(CFGKEY_SleepSuccessMessage, s.toString().trim());
              sender.sendMessage("Neue Benachrichtigung übernommen");
            }
          }
          break;
    }
    return false;
  }

}
