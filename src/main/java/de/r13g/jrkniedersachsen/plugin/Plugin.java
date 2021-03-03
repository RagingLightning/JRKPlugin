package de.r13g.jrkniedersachsen.plugin;

import de.r13g.jrkniedersachsen.plugin.modules.ColorsModule;
import de.r13g.jrkniedersachsen.plugin.modules.Module;
import de.r13g.jrkniedersachsen.plugin.modules.MorpheusModule;
import de.r13g.jrkniedersachsen.plugin.modules.PermissionsModule;
import de.r13g.jrkniedersachsen.plugin.util.Log;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Plugin extends JavaPlugin implements Listener {

  public static final String PERM_JrkAdminCommand = "jrk.admin";

  public static Plugin INSTANCE;

  private final List<String> modules = new ArrayList<String>(){{
    add(PermissionsModule.NAME);add(MorpheusModule.NAME);add(ColorsModule.NAME);}};

  private HashMap<String, Module> loadedModules = new HashMap<>();

  @Override
  public void onEnable() {
    super.onEnable();
    INSTANCE = this;
    this.getServer().getConsoleSender().sendMessage(Log.logLine("Main","Plugin wird geladen..."));
    saveDefaultConfig();
    reloadConfig();
    getServer().getPluginManager().registerEvents(this, this);

    for (String module : modules) {
      if (getConfig().getBoolean("modules." + module + ".enabled")) {
        getServer().getConsoleSender().sendMessage(Log.logLine("Main", "Modul '" + module + "' wird geladen..."));
        if (tryStartModule(module))
          getServer().getConsoleSender().sendMessage(Log.logLine("Main", "Modul '" + module + "' erfolgreich geladen"));
        else
          getServer().getConsoleSender().sendMessage(Log.logLine("Main", "<WARN> Modul '" + module + "' konnte nicht geladen werden", ChatColor.YELLOW));
      }
    }

    getServer().getConsoleSender().sendMessage(Log.logLine("Main", "Plugin erfolgreich geladen"));
  }

  public Module getModule(String module) {
    if (moduleStatus(module)!=1) return null;
    else return loadedModules.get(module);
  }

  public int moduleStatus(String module) {
    if (!loadedModules.containsKey(module)) return -1;
    return loadedModules.get(module).isReady()?1:0;
  }

  public boolean tryStartModule(String module) {
    if (loadedModules.containsKey(module) && loadedModules.get(module).isReady()) return true;
    if (!loadedModules.containsKey(module)) {
      switch (module) {
        case MorpheusModule.NAME:
          loadedModules.put(module, new MorpheusModule());
          break;
        case PermissionsModule.NAME:
          loadedModules.put(module, new PermissionsModule());
          break;
        case ColorsModule.NAME:
          loadedModules.put(module, new ColorsModule());
          break;
        default:
          return false;
      }
    }
    for (String m : getConfig().getStringList("modules." + module + ".dependencies")) {
      if (moduleStatus(m) != 1) {
        getServer().getConsoleSender().sendMessage(Log.logLine("Main", "Dependency '" + m + "': wird geladen..."));
        if (tryStartModule(m))
          getServer().getConsoleSender().sendMessage(Log.logLine("Main", "Dependency '" + m + "' erfolgreich geladen"));
        else {
          getServer().getConsoleSender().sendMessage(Log.logLine("Main", "<WARN> Dependency '" + m + "' konnte nicht geladen werden", ChatColor.YELLOW));
          return false;
        }
      }
    }
    return loadedModules.get(module).load(this, new File(getDataFolder(), module.toLowerCase()));
  }

  public boolean tryStopModule(String module) {
    if (loadedModules.containsKey(module) && !loadedModules.get(module).isReady()) return true;
    if (loadedModules.containsKey(module)) {
      if (loadedModules.get(module).unload()) {
        loadedModules.remove(module);
        return true;
      }
    }
    return false;
  }

  public boolean tryRestartModule(String module) {
    tryStopModule(module);
    return tryStartModule(module);
  }

  @Override
  public void onDisable() {
    for(Module m : loadedModules.values()) {
      m.unload();
    }
    saveConfig();
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (command.getName().equalsIgnoreCase("jrk")) {
      //TODO: GP-Command
    } else if (command.getName().equalsIgnoreCase("jrkadmin")) {
      if (!(sender instanceof ConsoleCommandSender) && !sender.hasPermission(PERM_JrkAdminCommand)) {
        sender.sendMessage(Log.logLine("Main","DU HAST DIE BENÖTIGTEN RECHTE NICHT!", ChatColor.RED));
        return true;
      }
      if (args.length == 0) {
        sender.sendMessage(Log.logLine("Main","Liste der Module (" + ChatColor.GREEN + "enabled " + ChatColor.YELLOW + "disabled " + ChatColor.RED + "errored" + ChatColor.RESET + "):"));
        for (String module : modules) {
          ChatColor c = ChatColor.GREEN;
          if (moduleStatus(module)==-1) c = ChatColor.YELLOW;
          if (moduleStatus(module)==0) c = ChatColor.RED;
          sender.sendMessage(Log.logLine("Main"," - " + module, c));
        }
        return true;
      }
      if (args[0].equals("enable")){
        if (args.length != 2) return false;
        if (modules.contains(args[1])) {
          getConfig().set("modules." + args[1] + ".enabled", true);
          saveConfig();
          if(tryStartModule(args[1]))
            sender.sendMessage(Log.logLine("Main", "Modul " + args[1] + " erfolgreich aktiviert."));
          else
            sender.sendMessage(Log.logLine("Main", "Modul " + args[1] + " konnte nicht aktiviert werden."));
        } else {
          sender.sendMessage(Log.logLine("Main", "Modul " + args[1] + " existiert nicht."));
        }
        return true;
      } else if (args[0].equals("disable")) {
        if (args.length != 2) return false;
        if (modules.contains(args[1])) {
          getConfig().set("modules." + args[1] + ".enabled", false);
          saveConfig();
          if(tryStopModule(args[1]))
            sender.sendMessage(Log.logLine("Main", "Modul " + args[1] + " erfolgreich deaktiviert."));
          else
            sender.sendMessage(Log.logLine("Main", "Modul " + args[1] + " konnte nicht deaktiviert werden."));
        } else {
          sender.sendMessage(Log.logLine("Main", "Modul " + args[1] + " existiert nicht."));
        }
        return true;
      } else if (args[0].equals("permissions")){
        if (moduleStatus(PermissionsModule.NAME) != 1) {
          sender.sendMessage(Log.logLine("Main", "Für diesen Command muss das 'Permissions' Modul aktiv sein.", ChatColor.RED));
          return true;
        }
        if (args.length < 2) return false;
        if (args[1].equals("list")) {
          Player p = sender instanceof Player ? (Player) sender : null;
          if (args.length == 3)
            p = getServer().getPlayerExact(args[2]);
          if (p != null) {
            PermissionsModule pm = (PermissionsModule) getModule(PermissionsModule.NAME);
            List<String> perms = pm.listPermissionsOfPlayer(p);
            sender.sendMessage(Log.logLine("Main", "Berechtigungen von " + p.getDisplayName() + ":"));
            for (String s : perms) {
              sender.sendMessage(" - " + s);
            }
          }
          return true;
        } else if (args[1].equals("having") && args.length==3) {
          PermissionsModule pm = (PermissionsModule) getModule(PermissionsModule.NAME);
          List<Player> players = pm.listPlayersWithPermission(args[2]);
          sender.sendMessage(Log.logLine("Main", "Spieler mit Berechtigung " + args[2] + ":"));
          for (Player p : players) {
            sender.sendMessage(" - " + p.getDisplayName());
          }
          return true;//TODO: GIVE/TAKE
        }
      }
    } else {
      for (String module : modules) {
        if (moduleStatus(module)!=1 || !command.getName().equalsIgnoreCase(module)) continue;
        return getModule(module).onCommand(sender, command, label, args);
      }
    }
    return false;
  }

  @EventHandler
  public void onTabComplete(TabCompleteEvent ev) {
    List<String[]> commands = new ArrayList<>();
    List<String> completions = new ArrayList<>();

    if (ev.getBuffer().startsWith("/jrkadmin")) {
      if (ev.getSender() instanceof ConsoleCommandSender || ev.getSender().hasPermission(PERM_JrkAdminCommand)) {
        commands.add(new String[]{"/jrkadmin","enable","<module>"});
        commands.add(new String[]{"/jrkadmin","disable","<module>"});
        commands.add(new String[]{"/jrkadmin","permissions","list","<player>"});
        commands.add(new String[]{"/jrkadmin","permissions","having","<permission>"});
      }
    }

    for (String module : modules) {
      if (moduleStatus(module)!=1 || !ev.getBuffer().startsWith("/" + module.toLowerCase())) continue;
      commands.addAll(getModule(module).getCommands());
    }

    String[] bufferSegments = ev.getBuffer().split(" ");

    commands.forEach(cmd -> {List<String> c = getCompletionForCommand(bufferSegments, ev.getBuffer().endsWith(" "), cmd); if (c != null) completions.addAll(c);});

    ev.setCompletions(completions);
  }

  private List<String> getCompletionForCommand(String[] buffer, boolean emptyEnd, String[] command) {
    if (command.length < buffer.length) return null;
    int i;
    for (i = 0; i < buffer.length - (emptyEnd?0:1); i++) {
      if (command[i].matches("<\\w+?>")) continue;
      if (command[i].equals(buffer[i])) continue;
      return null;
    }
    if (command.length <= i) return null;
    List<String> completions = new ArrayList<>();
    switch (command[i]){
      case "<player>": getServer().getWorlds().get(0).getPlayers().forEach(p -> completions.add(p.getName())); break;
      case "<module>": completions.addAll(modules); break;
      case "<team>": if (moduleStatus(ColorsModule.NAME)==1) completions.addAll(((ColorsModule) getModule(ColorsModule.NAME)).getTeamNames()); else completions.add("<team>"); break;
      case "<color>": Arrays.asList(ChatColor.values()).forEach(c -> {if (c != ChatColor.MAGIC && c != ChatColor.RESET) completions.add(c.name());}); break;
      default: completions.add(command[i]);
    }
    return completions;
  }
}