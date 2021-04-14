package de.r13g.jrkniedersachsen.plugin;

import de.r13g.jrkniedersachsen.plugin.customnpc.CustomPlayer;
import de.r13g.jrkniedersachsen.plugin.customnpc.CustomPlayerPather;
import de.r13g.jrkniedersachsen.plugin.customnpc.CustomVillager;
import de.r13g.jrkniedersachsen.plugin.module.*;
import de.r13g.jrkniedersachsen.plugin.module.gp.AfkModule;
import de.r13g.jrkniedersachsen.plugin.module.gp.PigSeatModule;
import de.r13g.jrkniedersachsen.plugin.module.story.npc.behaviour.SimpleWanderBehaviour;
import de.r13g.jrkniedersachsen.plugin.util.Util;
import net.minecraft.server.v1_16_R3.VillagerProfession;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftVillager;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public class Plugin extends JavaPlugin implements Listener {

  public static final String PERM_JrkAdminCommand = "jrk.admin";

  public static final String PERM_GPCommand = "jrk.gp";

  public static Plugin INSTANCE;

  private final List<String> gpModules = new ArrayList<String>(){{
    add("TPS");add(AfkModule.NAME);add(PigSeatModule.NAME);
  }};

  private HashMap<String, Module> loadedGpModules = new HashMap<>();

  private final List<String> modules = new ArrayList<String>(){{
    add(PermissionsModule.NAME);add(MorpheusModule.NAME);add(ColorsModule.NAME);add(LockModule.NAME);add(VanishModule.NAME);
    add(InvSeeModule.NAME);add(TempBanModule.NAME);add(AdminChatModule.NAME);add(StoryModule.NAME);
  }};

  private HashMap<String, Module> loadedModules = new HashMap<>();

  /*@EventHandler
  public void onPlayerJoin(PlayerJoinEvent ev) {
    CustomVillager villager = new CustomVillager(ev.getPlayer().getLocation(), VillagerProfession.CARTOGRAPHER);
    ((CraftWorld)ev.getPlayer().getWorld()).addEntity(villager, CreatureSpawnEvent.SpawnReason.CUSTOM);
    Villager bukkitVillager = (CraftVillager) villager.getBukkitEntity();
  }*/

  @Override
  public void onEnable() {
    super.onEnable();
    INSTANCE = this;
    this.getServer().getConsoleSender().sendMessage(Util.logLine("Main","Plugin wird geladen..."));
    saveDefaultConfig();
    reloadConfig();
    getServer().getPluginManager().registerEvents(this, this);

    for (String module : modules) {
      if (getConfig().getBoolean("modules." + module + ".enabled")) {
        getServer().getConsoleSender().sendMessage(Util.logLine("Main", "Modul '" + module + "' wird geladen..."));
        if (tryStartModule(module))
          getServer().getConsoleSender().sendMessage(Util.logLine("Main", "Modul '" + module + "' erfolgreich geladen"));
        else
          getServer().getConsoleSender().sendMessage(Util.logLine("Main", "<WARN> Modul '" + module + "' konnte nicht geladen werden", ChatColor.YELLOW));
      }
    }

    for (String module : gpModules) {
      if (getConfig().getBoolean("modules.gp." + module + ".enabled")) {
        getServer().getConsoleSender().sendMessage(Util.logLine("Main", "GP-Modul '" + module + "' wird geladen..."));
        if (tryStartModule(module))
          getServer().getConsoleSender().sendMessage(Util.logLine("Main", "GP-Modul '" + module + "' erfolgreich geladen"));
        else
          getServer().getConsoleSender().sendMessage(Util.logLine("Main", "<WARN> GP-Modul '" + module + "' konnte nicht geladen werden", ChatColor.YELLOW));
      }
    }

    getServer().getConsoleSender().sendMessage(Util.logLine("Main", "Plugin erfolgreich geladen"));
  }

  public Module getModule(String module) {
    if (moduleStatus(module)!=1) return null;
    else return loadedModules.get(module);
  }

  public int moduleStatus(String module) {
    if (!loadedModules.containsKey(module) && !loadedGpModules.containsKey(module)) return -1;
    if (loadedModules.containsKey(module))
      return loadedModules.get(module).isReady()?1:0;
    return loadedGpModules.get(module).isReady()?1:0;
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
        case LockModule.NAME:
          loadedModules.put(module, new LockModule());
          break;
        case VanishModule.NAME:
          loadedModules.put(module, new VanishModule());
          break;
        case InvSeeModule.NAME:
          loadedModules.put(module, new InvSeeModule());
          break;
        case TempBanModule.NAME:
          loadedModules.put(module, new TempBanModule());
          break;
        case AdminChatModule.NAME:
          loadedModules.put(module, new AdminChatModule());
          break;
        case StoryModule.NAME:
          loadedModules.put(module, new StoryModule());
          break;
        case AfkModule.NAME:
          loadedGpModules.put(module, new AfkModule());
          break;
        case PigSeatModule.NAME:
          loadedGpModules.put(module, new PigSeatModule());
          break;
        default:
          return false;
      }
    }
    for (String m : getConfig().getStringList("modules." + module + ".dependencies")) {
      if (moduleStatus(m) != 1) {
        getServer().getConsoleSender().sendMessage(Util.logLine("Main", "Dependency '" + m + "': wird geladen..."));
        if (tryStartModule(m))
          getServer().getConsoleSender().sendMessage(Util.logLine("Main", "Dependency '" + m + "' erfolgreich geladen"));
        else {
          getServer().getConsoleSender().sendMessage(Util.logLine("Main", "<WARN> Dependency '" + m + "' konnte nicht geladen werden", ChatColor.YELLOW));
          return false;
        }
      }
    }
    try {
      if (loadedModules.containsKey(module))
        return loadedModules.get(module).load(this, new File(getDataFolder(), module.toLowerCase()));
      return loadedGpModules.get(module).load(this, new File(getDataFolder(), module.toLowerCase()));
    } catch (Exception e) {
      getServer().getConsoleSender().sendMessage(Util.logLine("Main","<ERR> Error while loading module " + module + ": "));
      e.printStackTrace();
      return false;
    }
  }

  public boolean tryStopModule(String module) {
    if (loadedModules.containsKey(module) && !loadedModules.get(module).isReady()) return true;
    if (loadedModules.containsKey(module)) {
      if (loadedModules.get(module).unload()) {
        loadedModules.remove(module);
        return true;
      }
    } else if(loadedGpModules.containsKey(module)) {
      if(loadedGpModules.get(module).unload()) {
        loadedGpModules.remove(module);
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

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent ev) {
    CustomPlayer.addJoinPacket(ev.getPlayer());
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (command.getName().equalsIgnoreCase("test")) {
      Location l = ((Player) sender).getLocation();
      CustomPlayerPather custom = CustomPlayer.create(l, "TEST");
      return true;
    }
    if (command.getName().equalsIgnoreCase("jrk")) {
      if (args.length == 0) return false;
      if (args[0].equals("tps") &&(sender instanceof ConsoleCommandSender || sender.hasPermission(PERM_GPCommand + ".tps"))) return gpTpsCommand(sender); //TODO: test
      for (String module : loadedGpModules.keySet()) {
        if (args[0].equals(module.toLowerCase()) && sender.hasPermission(PERM_GPCommand + "." + args[0])) {
          if(!loadedGpModules.get(module).onCommand(sender, command, label, args))
            loadedGpModules.get(module).getHelpText(sender).forEach(sender::sendMessage);
          return true;
        }
      }
      //TODO: GP-Command
    } else if (command.getName().equalsIgnoreCase("jrkadmin")) {
      if (!(sender instanceof ConsoleCommandSender) && !sender.hasPermission(PERM_JrkAdminCommand)) {
        sender.sendMessage(Util.logLine("Main","DU HAST DIE BENÖTIGTEN RECHTE NICHT!", ChatColor.RED));
        return true;
      }
      if (args.length == 0) {
        sender.sendMessage(Util.logLine("Main","Liste der Module (" + ChatColor.GREEN + "enabled " + ChatColor.YELLOW + "disabled " + ChatColor.RED + "errored" + ChatColor.RESET + "):"));
        for (String module : modules) {
          ChatColor c = ChatColor.GREEN;
          if (moduleStatus(module)==-1) c = ChatColor.YELLOW;
          if (moduleStatus(module)==0) c = ChatColor.RED;
          sender.sendMessage(Util.logLine("Main"," - " + module, c));
        }
        for (String module : gpModules) {
          ChatColor c = ChatColor.GREEN;
          if (moduleStatus(module)==-1) c = ChatColor.YELLOW;
          if (moduleStatus(module)==0) c = ChatColor.RED;
          sender.sendMessage(Util.logLine("Main"," - " + module, c));
        }
        return true;
      }
      if (args[0].equals("enable")){
        if (args.length != 2) return false;
        if (modules.contains(args[1])) {
          getConfig().set("modules." + args[1] + ".enabled", true);
          saveConfig();
          if(tryStartModule(args[1]))
            sender.sendMessage(Util.logLine("Main", "Modul " + args[1] + " erfolgreich aktiviert."));
          else
            sender.sendMessage(Util.logLine("Main", "Modul " + args[1] + " konnte nicht aktiviert werden."));
        } else if (gpModules.contains(args[1])) {
          getConfig().set("modules.gp." + args[1] + ".enabled", true);
          saveConfig();
          if(tryStartModule(args[1]))
            sender.sendMessage(Util.logLine("Main", "Modul GP-" + args[1] + " erfolgreich aktiviert."));
          else
            sender.sendMessage(Util.logLine("Main", "Modul GP-" + args[1] + " konnte nicht aktiviert werden."));
        } else {
          sender.sendMessage(Util.logLine("Main", "Modul " + args[1] + " existiert nicht."));
        }
        return true;
      } else if (args[0].equals("disable")) {
        if (args.length != 2) return false;
        if (modules.contains(args[1])) {
          getConfig().set("modules." + args[1] + ".enabled", false);
          saveConfig();
          if(tryStopModule(args[1]))
            sender.sendMessage(Util.logLine("Main", "Modul " + args[1] + " erfolgreich deaktiviert."));
          else
            sender.sendMessage(Util.logLine("Main", "Modul " + args[1] + " konnte nicht deaktiviert werden."));
        } else if (gpModules.contains(args[1])) {
          getConfig().set("modules.gp." + args[1] + ".enabled", false);
          saveConfig();
          if(tryStopModule(args[1]))
            sender.sendMessage(Util.logLine("Main", "Modul GP-" + args[1] + " erfolgreich deaktiviert."));
          else
            sender.sendMessage(Util.logLine("Main", "Modul GP-" + args[1] + " konnte nicht deaktiviert werden."));
        } else {
          sender.sendMessage(Util.logLine("Main", "Modul " + args[1] + " existiert nicht."));
        }
        return true;
      } else if (args[0].equals("permissions")) {
        if (moduleStatus(PermissionsModule.NAME) != 1) {
          sender.sendMessage(Util.logLine("Main", "Für diesen Command muss das 'Permissions' Modul aktiv sein.", ChatColor.RED));
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
            sender.sendMessage(Util.logLine("Main", "Berechtigungen von " + p.getDisplayName() + ":"));
            for (String s : perms) {
              sender.sendMessage(" - " + s);
            }
          }
          return true;
        } else if (args[1].equals("having") && args.length==3) {
          PermissionsModule pm = (PermissionsModule) getModule(PermissionsModule.NAME);
          List<Player> players = pm.listPlayersWithPermission(args[2]);
          sender.sendMessage(Util.logLine("Main", "Spieler mit Berechtigung " + args[2] + ":"));
          for (Player p : players) {
            sender.sendMessage(" - " + p.getDisplayName());
          }
          return true;
        } else if (args[1].equals("give") && args.length==4) {
          PermissionsModule pm = (PermissionsModule) getModule(PermissionsModule.NAME);
          pm.playerAttachment(getServer().getPlayerExact(args[2]).getUniqueId()).setPermission(args[3], true);
          return true;
        } else if (args[1].equals("take") && args.length==4) {
          PermissionsModule pm = (PermissionsModule) getModule(PermissionsModule.NAME);
          pm.playerAttachment(getServer().getPlayerExact(args[2]).getUniqueId()).setPermission(args[3], false);
          return true;
        }
      } else if (args[0].equals("admin")) {
        if (args.length == 1) return false;
        if (moduleStatus(PermissionsModule.NAME) != 1) {
          sender.sendMessage(Util.logLine("Main", "Für diesen Command muss das 'Permissions' Modul aktiv sein.", ChatColor.RED));
          return true;
        }
        PermissionsModule pm = (PermissionsModule) getModule(PermissionsModule.NAME);
        if (args.length == 2) { //jrkadmin admin <module>
          if (!modules.contains(args[1])) {
            sender.sendMessage(Util.logLine("Main",args[1] + " ist kein Modul")); return true;
          }
          sender.sendMessage(Util.logLine("Main","Spieler mit Adminrechten für Modul " + args[1]));
          for (Player p : pm.listPlayersWithPermission("jrk." + args[1].toLowerCase() + ".admin")) {
            sender.sendMessage(Util.logLine("Main"," - " + p.getDisplayName()));
          }
        } else if (args.length == 3) { //jrkadmin admin <module> <player>
          if (!modules.contains(args[1])) {
            sender.sendMessage(Util.logLine("Main",args[1] + " ist kein Modul")); return true;
          }
          Player p = getServer().getPlayerExact(args[2]);
          if (p == null) {
            sender.sendMessage(Util.logLine("Main",args[2] + " ist nicht online")); return true;
          }
          if (p.hasPermission("jrk." + args[1].toLowerCase() + ".admin")) {
            sender.sendMessage(Util.logLine("Main", p.getDisplayName() + " hat Adminrechte für das Modul " + args[1]));
          } else {
            sender.sendMessage(Util.logLine("Main", p.getDisplayName() + " hat keine Adminrechte für das Modul " + args[1]));
          }
        } else if (args.length == 4) { //jrkadmin admin <module> <player> <true/false>
          if (!modules.contains(args[1])) {
            sender.sendMessage(Util.logLine("Main",args[1] + " ist kein Modul")); return true;
          }
          Player p = getServer().getPlayerExact(args[2]);
          if (p == null) {
            sender.sendMessage(Util.logLine("Main",args[2] + " ist nicht online")); return true;
          }
          pm.playerAttachment(p.getUniqueId()).setPermission("jrk." + args[1].toLowerCase() + ".admin", args[3].equals("true"));
          if (args[3].equals("true")) {
            sender.sendMessage(Util.logLine("Main",p.getDisplayName() + " ist jetzt ein Admin des Moduls " + args[1]));
          } else {
            sender.sendMessage(Util.logLine("Main",p.getDisplayName() + " ist jetzt kein Admin des Moduls " + args[1]));
          }
        } else return false;
        return true;
      }
    } else {
      for (String module : modules) {
        if (moduleStatus(module)!=1 || !command.getName().equalsIgnoreCase(module)) continue;
        if (!getModule(module).onCommand(sender, command, label, args))
          loadedModules.get(module).getHelpText(sender).forEach(sender::sendMessage);
        return true;
      }
    }
    return false;
  }

  private boolean gpTpsCommand(CommandSender sender) {
    return getServer().dispatchCommand(sender, "tps");
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
        commands.add(new String[]{"/jrkadmin","permissions","give","<player>","<permission>"});
        commands.add(new String[]{"/jrkadmin","permissions","take","<player>","<permission>"});
        commands.add(new String[]{"/jrkadmin","admin","<module>","<player>","true"});
        commands.add(new String[]{"/jrkadmin","admin","<module>","<player>","false"});
        commands.add(new String[]{"/jrkadmin","gp","<gpmodule>","<player>","true"});
        commands.add(new String[]{"/jrkadmin","gp","<gpmodule>","<player>","false"});
      }
    } else if (ev.getBuffer().startsWith("/jrk")) {
      if (ev.getSender() instanceof ConsoleCommandSender || ev.getSender().hasPermission(PERM_GPCommand)) {
        if(ev.getSender() instanceof ConsoleCommandSender || ev.getSender().hasPermission(PERM_GPCommand + ".tps")) commands.add(new String[]{"/jrk","tps"});
        for (String module : loadedGpModules.keySet()) {
          if(ev.getSender() instanceof ConsoleCommandSender || ev.getSender().hasPermission(PERM_GPCommand + "." + module.toLowerCase())) commands.addAll(loadedGpModules.get(module).getCommands());
        }
      }
    } else {
      for (String module : loadedModules.keySet()) {
        if (moduleStatus(module) != 1 || !ev.getBuffer().startsWith("/" + module.toLowerCase())) continue;
        commands.addAll(getModule(module).getCommands());
      }
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
      case "<gpmodule>": completions.addAll(gpModules); break;
      case "<team>": if (moduleStatus(ColorsModule.NAME)==1) completions.addAll(((ColorsModule) getModule(ColorsModule.NAME)).getTeamNames()); else completions.add("<team>"); break;
      case "<color>": Arrays.asList(ChatColor.values()).forEach(c -> {if (c != ChatColor.MAGIC && c != ChatColor.RESET) completions.add(c.name());}); break;
      default: completions.add(command[i]);
    }
    return completions;
  }

  private List<String> getHelpTextForJrkCommand() {
    List<String> text = new ArrayList<>();
    text.add("--- Serverbefehle: Hilfe ---");
    text.add("");
    text.add("/jrk tps - zeigt die aktuellen Server-TPS an");
    text.add("- alias: /tps");
    if (moduleStatus(AfkModule.NAME) == 1) {
      text.add("/jrk afk - setzt den Afk-Status");
      text.add("- Nach " + ((AfkModule) getModule(AfkModule.NAME)).timeout + " Minuten Inaktivität wird ein Spieler automatisch Afk gesetzt");
      text.add("- Bewegung oder Interaktion mit Blöcken beendet den Afk-Status");
    }
    return text;
  }

  private List<String> getHelpTextForAdminCommand() {
    List<String> text = new ArrayList<>();
    return text;
  }
}
