package de.r13g.jrkniedersachsen.plugin.modules;

import de.r13g.jrkniedersachsen.plugin.Plugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class InvSeeModule implements Module {

  public static final String NAME = "InvSee";

  @Override
  public boolean load(Plugin plugin, File moduleDataFolder) {
    return true;
  }

  @Override
  public boolean unload() {
    return true;
  }

  @Override
  public boolean isReady() {
    return true;
  }

  @Override
  public List<String[]> getCommands() {
    List<String[]> commands = new ArrayList<>();
    commands.add(new String[]{"/invsee","chest","<player>"});
    commands.add(new String[]{"/invsee","player","<player>"});
    return commands;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length == 2 && sender instanceof Player) {
      Player p = Plugin.INSTANCE.getServer().getPlayerExact(args[1]);
      if (p != null) {
        if (args[0].equals("player")) {
          ((Player) sender).openInventory(p.getInventory());
          return true;
        } else if (args[0].equals("chest")) {
          ((Player) sender).openInventory(p.getEnderChest());
          return true;
        }
      }
    }
    return false;
  }
}
