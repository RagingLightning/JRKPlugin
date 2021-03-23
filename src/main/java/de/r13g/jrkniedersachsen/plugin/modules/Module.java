package de.r13g.jrkniedersachsen.plugin.modules;

import de.r13g.jrkniedersachsen.plugin.Plugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

import java.io.File;
import java.util.List;

public interface Module {

  boolean load(Plugin plugin, File moduleDataFolder);

  boolean unload();

  //String getName();

  boolean isReady();

  List<String[]> getCommands();

  List<String> getHelpText(Permissible p);

  boolean onCommand(CommandSender sender, Command command, String label, String[] args);

}
