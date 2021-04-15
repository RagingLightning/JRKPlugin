package de.r13g.jrkniedersachsen.plugin.module.gp;

import de.r13g.jrkniedersachsen.plugin.Plugin;
import de.r13g.jrkniedersachsen.plugin.module.Module;
import de.r13g.jrkniedersachsen.plugin.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PigSeatModule implements Module {

  public static final String NAME = "PigSeat";

  private File configFile;
  private FileConfiguration config;

  private boolean ready = false;

  private BukkitTask effectTask;

  @Override
  public boolean load(Plugin plugin, File moduleDataFolder) {
    /*configFile = new File(moduleDataFolder, "pigseat.yml");
    config = YamlConfiguration.loadConfiguration(configFile);
    config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("pigseat.yml"))));
    if (!configFile.exists()) {
      try {
        config.save(configFile);
      } catch (IOException e) {
        plugin.getServer().getConsoleSender().sendMessage(Util.logLine(NAME, "<WARN> unable to save config", ChatColor.YELLOW));
      }
    }*/

    effectTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
      for (World w : plugin.getServer().getWorlds()) {
        for (Pig p : w.getEntitiesByClass(Pig.class)) {
          if (!p.getScoreboardTags().contains("seat")) continue;
          p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 30 * 60 * 20, 0, true, false, false));
        }
      }
    }, 0L, 15 * 60 * 20);

    ready = true;
    return true;
  }

  @Override
  public boolean unload() {
    ready = false;

    try {
      config.save(configFile);
    } catch (IOException e) {
      Plugin.INSTANCE.getServer().getConsoleSender().sendMessage(Util.logLine(NAME, "<WARN> unable to save config", ChatColor.YELLOW));
    }

    effectTask.cancel();

    return true;
  }

  @Override
  public boolean isReady() {
    return ready;
  }

  @Override
  public List<String[]> getCommands() {
    List<String[]> commands = new ArrayList<>();
    commands.add(new String[]{"/jrk", NAME.toLowerCase()});
    return commands;
  }

  @Override
  public List<String> getHelpText(Permissible p) {
    List<String> text = new ArrayList<>();
    text.add("--- " + NAME + "-Modul: Hilfe ---");
    text.add("");
    text.add("/jrk pigseat - Fügt der aktuell fokussierten Treppe einen Sitz hinzu, oder entfernt einen bestehenden");
    return text;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length != 1) return false;
    if (!(sender instanceof Player)) return true;
    Block b = ((Player) sender).getTargetBlockExact(10);
    if (!(b.getState().getBlockData() instanceof Stairs)) return true;
    Stairs data = (Stairs) b.getState().getBlockData();
    if (data.getHalf() != Bisected.Half.BOTTOM) return true;

    Collection<Entity> pigs = b.getWorld().getNearbyEntities(b.getLocation().add(0.5, -1 + 0.61, 0.5), 0.5, 0.5, 0.5, e -> e.getScoreboardTags().contains("seat"));
    if (pigs.size() > 0) { //Remove Seat
      for (Entity p : pigs) p.remove();
    } else { //Add Seat
      Pig p = (Pig) b.getWorld().spawnEntity(new Location(b.getWorld(), b.getX() + 0.5, b.getY() - 1 + 0.61, b.getZ() + 0.5), EntityType.PIG);

      p.setSilent(true);
      p.setAI(false);
      p.getScoreboardTags().add("seat");
      p.setSaddle(true);
      p.setInvulnerable(true);
      p.setPersistent(true);
      p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 30 * 60 * 20, 0, true, false, false));

      float yaw = 0;
      if (data.getShape() == Stairs.Shape.INNER_LEFT || data.getShape() == Stairs.Shape.OUTER_LEFT) yaw = -45;
      else if (data.getShape() == Stairs.Shape.INNER_RIGHT || data.getShape() == Stairs.Shape.OUTER_RIGHT) yaw = 45;

      switch (data.getFacing()) {
        case WEST:
          yaw += 90;
        case SOUTH:
          yaw += 90;
        case EAST:
          yaw += 90;
      }

      p.teleport(new Location(b.getWorld(), b.getX() + 0.5, b.getY() - 1 + 0.61, b.getZ() + 0.5, yaw, 0));
    }
    return true;
  }
}
