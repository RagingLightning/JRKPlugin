package de.r13g.jrkniedersachsen.plugin;

import de.r13g.jrkniedersachsen.plugin.morpheus.MorpheusModule;
import de.r13g.jrkniedersachsen.plugin.permissions.PermissionsModule;
import de.r13g.jrkniedersachsen.plugin.util.Log;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Plugin extends JavaPlugin implements Listener {

  public static Plugin INSTANCE;

  public static MorpheusModule morpheusModule = null;
  public static PermissionsModule permissionsModule = null;

  @Override
  public void onEnable() {
    super.onEnable();
    INSTANCE = this;
    this.getServer().getConsoleSender().sendMessage("Plugin des JRK-Servers wird geladen...");
    saveDefaultConfig();
    reloadConfig();
    getServer().getPluginManager().registerEvents(this, this);
    if (getConfig().getBoolean("modules.permissions.enabled")) {
      this.getServer().getConsoleSender().sendMessage(Log.logLine("Main", "Modul 'Permissions' wird geladen..."));
      permissionsModule = new PermissionsModule(new File(getDataFolder(), "permissions"));
      this.getServer().getPluginManager().registerEvents(permissionsModule, this);
    }
    if (getConfig().getBoolean("modules.morpheus.enabled")) {
      this.getServer().getConsoleSender().sendMessage(Log.logLine("Main", "Modul 'Morpheus' wird geladen..."));
      morpheusModule = new MorpheusModule();
      this.getServer().getPluginManager().registerEvents(morpheusModule, this);
    }
  }

  public boolean isModuleLoaded(String module) {
    switch (module) {
      case PermissionsModule.NAME:
        return permissionsModule != null;
      case MorpheusModule.NAME:
        return morpheusModule != null;
      default:
        return false;
    }
  }

  @Override
  public void onDisable() {
    super.onDisable();
    if(isModuleLoaded(PermissionsModule.NAME))
      permissionsModule.saveAllPermissions();
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (command.getName().equals("morpheus")) {
      return morpheusModule.onCommand(sender, command, label, args);
    } else { //TODO: help
      return false;
    }
  }

  @EventHandler
  public void onTabComplete(TabCompleteEvent ev) {
    if (ev.getBuffer().startsWith("/morpheus") && morpheusModule != null) {
      List<String> completions = new ArrayList<>();
      List<String> commands = morpheusModule.onTabComplete(ev);

      commands.forEach(i -> {
        if (i.contains(ev.getBuffer())) {
          String b = ev.getBuffer().replaceAll("\\s\\w+$", "");
          String val = i.replaceFirst(b, "").trim().split(" ")[0];
          if (val.equals("<player>"))
            Plugin.INSTANCE.getServer().getWorlds().get(0).getPlayers().forEach(p -> commands.add(p.getDisplayName()));
          else
            completions.add(val);
        }
      });

      ev.setCompletions(completions);
    }
  }
}
