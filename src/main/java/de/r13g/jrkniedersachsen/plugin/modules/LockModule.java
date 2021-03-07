package de.r13g.jrkniedersachsen.plugin.modules;

import de.r13g.jrkniedersachsen.plugin.Plugin;
import de.r13g.jrkniedersachsen.plugin.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.*;

@SuppressWarnings("deprecation")
public class LockModule implements Module, Listener {

  public static final String NAME = "Lock";

  private static final String MDAT_LockOwner = "jrkLockOwner";
  private static final String MDAT_LockPassword = "jrkLockPassword";
  private static final String MDAT_LockOrigin = "jrkLockOrigin";

  private static final String CFGKEY_OwnerNoPw = "ownerNoPw"; //boolean
  private static final String CFGKEY_PasswordTimeout = "passwordTimeout";
  private static final String CFGKEY_TilesWithLocks = "tiles";
  private static final String CFGKEY_MaxLockCount = "maxLockCount";
  private static final String CFGKEY_PlayerLocks = "players";
  private static final String CFGKEY_BlockHasLockMessage = "messages.blockHasLock";
  private static final String CFGKEY_BlockHasNoLockMessage = "messages.blockHasNoLock";
  private static final String CFGKEY_WrongPasswordMessage = "messages.wrongPassword";
  private static final String CFGKEY_PasswordExpiredMessage = "messages.passwordExpired";
  private static final String CFGKEY_NotLockOwner = "messages.notLockOwner";
  private static final String CFGKEY_LockAddedMessage = "messages.lockAdded";
  private static final String CFGKEY_LockRemovedMessage = "messages.lockRemoved";
  private static final String CFGKEY_InvalidTypeMessage = "messages.invalidType";
  private static final String CFGKEY_LockDataMessage = "messages.lockData";
  private static final String CFGKEY_TooManyLocksMessage = "messages.tooManyLocks";
  private static final String CFGKEY_NoCreatePermissionMessage = "messages.noCreatePermission";

  private static final String PERM_BypassLock = "jrk.lock.bypass";
  private static final String PERM_CreateLock = "jrk.lock.create";
  private static final String PERM_LockAdmin = "jrk.lock.admin";

  private File configFile;
  private FileConfiguration config;

  private boolean ready = false;

  private HashMap<UUID, String> passwordCache;
  private HashMap<UUID, List<String>> playerLocks;

  @Override
  public boolean load(Plugin plugin, File moduleDataFolder) {
    configFile = new File(moduleDataFolder, "config.yml");

    if (!configFile.exists()) {
      try {
        YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("lock.yml"))).save(configFile);
      } catch (IOException e) {
        plugin.getServer().getConsoleSender().sendMessage(Util.logLine(NAME, "<WARN> default config could not be loaded"));
        return false;
      }
    }

    config = YamlConfiguration.loadConfiguration(configFile);
    plugin.getServer().getPluginManager().registerEvents(this, plugin);

    passwordCache = new HashMap<>();

    for (String k : config.getConfigurationSection(CFGKEY_PlayerLocks).getKeys(false)) {
      for (String v : config.getConfigurationSection(CFGKEY_PlayerLocks + "." + k).getKeys(false)) {
        String w = v.split("\\[W]")[1].split("\\[X]")[0];
        int x = Integer.parseInt(v.split("\\[X]")[1].split("\\[Y]")[0]);
        int y = Integer.parseInt(v.split("\\[Y]")[1].split("\\[Z]")[0]);
        int z = Integer.parseInt(v.split("\\[Z]")[1]);
        String p = config.getString(CFGKEY_PlayerLocks + "." + k + "." + v);
        Block b = plugin.getServer().getWorld(w).getBlockAt(x,y,z);
        makeLock(b, k, p);
      }
    }

    ready = true;
    return true;
  }

  @Override
  public boolean unload() {
    ready = false;
    try {
      config.save(configFile);
    } catch (IOException e) {
      Plugin.INSTANCE.getServer().getConsoleSender().sendMessage(Util.logLine(NAME, "<WARN> unable to save config.yml", ChatColor.YELLOW));
    }
    HandlerList.unregisterAll(this);
    return true;
  }

  @Override
  public boolean isReady() {
    return ready;
  }

  @Override
  public List<String[]> getCommands() {
    List<String[]> commands = new ArrayList<>();
    commands.add(new String[]{"/"+NAME.toLowerCase(),"pw","<password>"});
    commands.add(new String[]{"/"+NAME.toLowerCase(),"add","<password>"});
    commands.add(new String[]{"/"+NAME.toLowerCase(),"remove"});
    commands.add(new String[]{"/"+NAME.toLowerCase(),"get"});
    commands.add(new String[]{"/"+NAME.toLowerCase(),"list"});
    return commands;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length == 0) return false;
    if (!(sender instanceof Player)) return false;
    if (args[0].equals("pw") && args.length == 2) {
      passwordCache.put(((Player) sender).getUniqueId(), args[1]);
      Plugin.INSTANCE.getServer().getScheduler().runTaskLater(Plugin.INSTANCE, () -> {
        passwordCache.remove(((Player) sender).getUniqueId());
        sender.sendMessage(ChatColor.ITALIC + config.getString(CFGKEY_PasswordExpiredMessage));
      }, config.getInt(CFGKEY_PasswordTimeout));
      return true;
    }
    Block b = ((Player) sender).getTargetBlockExact(10);
    if (args[0].equals("add") && args.length == 2) {
      if (!sender.hasPermission(PERM_CreateLock)) {
        sender.sendMessage(ChatColor.ITALIC + config.getString(CFGKEY_NoCreatePermissionMessage));
        return true;
      }
      if (!config.getStringList(CFGKEY_TilesWithLocks).contains(b.getType().name())) {
        sender.sendMessage(ChatColor.ITALIC + config.getString(CFGKEY_InvalidTypeMessage));
        return true;
      }
      if (config.getConfigurationSection(CFGKEY_PlayerLocks + "." + ((Player) sender).getUniqueId()).getKeys(false).size() >= config.getInt(CFGKEY_MaxLockCount)) {
        sender.sendMessage(ChatColor.ITALIC + config.getString(CFGKEY_TooManyLocksMessage));
        return true;
      }
      if (b.hasMetadata(MDAT_LockPassword) && b.hasMetadata(MDAT_LockOwner) && !b.getMetadata(MDAT_LockOwner).get(0).asString().equals(((Player) sender).getUniqueId().toString())) {
        sender.sendMessage(ChatColor.ITALIC + config.getString(CFGKEY_BlockHasLockMessage));
        return true;
      }
      makeLock(b, ((Player) sender).getUniqueId().toString(), args[1]);
      sender.sendMessage(ChatColor.ITALIC + config.getString(CFGKEY_LockAddedMessage));
      return true;
    }
    if (args[0].equals("remove")) {
      if (!b.hasMetadata(MDAT_LockOwner) && !b.hasMetadata(MDAT_LockPassword)) {
        sender.sendMessage(ChatColor.ITALIC + config.getString(CFGKEY_BlockHasNoLockMessage));
        return true;
      }
      if (!b.getMetadata(MDAT_LockOwner).get(0).asString().equals(((Player) sender).getUniqueId().toString()) && !sender.hasPermission((PERM_LockAdmin))) {
        sender.sendMessage(ChatColor.ITALIC + config.getString(CFGKEY_NotLockOwner));
        return true;
      }
      removeLock(b);
      sender.sendMessage(ChatColor.ITALIC + config.getString(CFGKEY_LockRemovedMessage));
    }
    if (args[0].equals("get")) {
      if (!b.hasMetadata(MDAT_LockPassword) && !b.hasMetadata(MDAT_LockOwner)) {
        sender.sendMessage(ChatColor.ITALIC + config.getString(CFGKEY_BlockHasNoLockMessage));
        return true;
      }
      if (!b.getMetadata(MDAT_LockOwner).get(0).asString().equals(((Player) sender).getUniqueId().toString()) && !sender.hasPermission(PERM_BypassLock)) {
        sender.sendMessage(ChatColor.ITALIC + config.getString(CFGKEY_NotLockOwner));
        return true;
      }
      sender.sendMessage(ChatColor.ITALIC + String.format(Util.configToFormatString(config.getString(CFGKEY_LockDataMessage)),
              Plugin.INSTANCE.getServer().getOfflinePlayer(UUID.fromString(b.getMetadata(MDAT_LockOwner).get(0).asString())), b.getMetadata(MDAT_LockPassword).get(0).asString()));
      return true;
    }
    if (args[0].equals("list")) {
      OfflinePlayer p = (OfflinePlayer) sender;
      if (sender.hasPermission(PERM_LockAdmin) && args.length == 2) p = Plugin.INSTANCE.getServer().getOfflinePlayer(args[1]);
      if (config.isSet(CFGKEY_PlayerLocks + "." + p.getUniqueId()))
        for (String k : config.getConfigurationSection(CFGKEY_PlayerLocks + "." + p.getUniqueId()).getKeys(false))
        sender.sendMessage(ChatColor.ITALIC + " - " + k + " / " + config.getString(CFGKEY_PlayerLocks + "." + p.getUniqueId() + "." + k));
      return true;
    }
    return false;
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent ev) {
    // Block has no lock
    if (ev.getClickedBlock() == null || !ev.getClickedBlock().hasMetadata(MDAT_LockPassword) || !ev.getClickedBlock().hasMetadata(MDAT_LockOwner)) return;
    // Player is lock owner and CFGKEY_ownerNoPw is set
    if (config.getBoolean(CFGKEY_OwnerNoPw) && ev.getClickedBlock().getMetadata(MDAT_LockOwner).get(0).asString().equals(ev.getPlayer().getUniqueId().toString())) return;
    // Player has correctly named item in hand
    if (ev.hasItem() && ev.getItem().getItemMeta().hasDisplayName() && ev.getClickedBlock().getMetadata(MDAT_LockPassword).get(0).asString().equals(ev.getItem().getItemMeta().getDisplayName())) return;
    // Player has correct password in Buffer
    if (passwordCache.containsKey(ev.getPlayer().getUniqueId()) && ev.getClickedBlock().getMetadata(MDAT_LockPassword).get(0).asString().equals(passwordCache.get(ev.getPlayer().getUniqueId()))) return;
    // Player can bypass all locks
    if (ev.getPlayer().hasPermission(PERM_BypassLock)) return;
    ev.getPlayer().sendMessage(ChatColor.ITALIC + config.getString(CFGKEY_BlockHasLockMessage));
    ev.setCancelled(true);
  }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent ev) {
    Block b = ev.getBlock();
    if (!b.hasMetadata(MDAT_LockOwner) && !b.hasMetadata(MDAT_LockOrigin) && !b.hasMetadata(MDAT_LockPassword)) return;

    removeLock(b);

  }

  @EventHandler
  public void onBlockPhysics(BlockPhysicsEvent ev) {
    if (ev.getBlock().hasMetadata(MDAT_LockPassword) && !ev.getSourceBlock().hasMetadata(MDAT_LockPassword))
      ev.setCancelled(true);
  }

  private void makeLock(Block b, String ownerUid, String password) {
    b.setMetadata(MDAT_LockPassword, new FixedMetadataValue(Plugin.INSTANCE, password));
    b.setMetadata(MDAT_LockOwner, new FixedMetadataValue(Plugin.INSTANCE, ownerUid));
    Location loc = b.getLocation();
    if (b.getState().getBlockData() instanceof Door) {
      if (((Door) b.getState().getBlockData()).getHalf() == Bisected.Half.TOP) {
        Block b1 = b.getRelative(BlockFace.DOWN);
        b1.setMetadata(MDAT_LockPassword, new FixedMetadataValue(Plugin.INSTANCE, password));
        b1.setMetadata(MDAT_LockOwner, new FixedMetadataValue(Plugin.INSTANCE, ownerUid));
        b1.setMetadata(MDAT_LockOrigin, new FixedMetadataValue(Plugin.INSTANCE, true));
        loc = b1.getLocation();
      } else {
        Block b1 = b.getRelative(BlockFace.UP);
        b1.setMetadata(MDAT_LockPassword, new FixedMetadataValue(Plugin.INSTANCE, password));
        b1.setMetadata(MDAT_LockOwner, new FixedMetadataValue(Plugin.INSTANCE, ownerUid));
        b.setMetadata(MDAT_LockOrigin, new FixedMetadataValue(Plugin.INSTANCE, true));
      }
    } else {
      b.setMetadata(MDAT_LockOrigin, new FixedMetadataValue(Plugin.INSTANCE, true));
    }

    String lock = "[W]" + loc.getWorld().getName() + "[X]" + loc.getBlockX() + "[Y]" + loc.getBlockY() + "[Z]" + loc.getBlockZ();
    config.set(CFGKEY_PlayerLocks + "." + ownerUid + "." + lock, password);

    try {
      config.save(configFile);
    } catch (IOException e) {
      Plugin.INSTANCE.getServer().getConsoleSender().sendMessage(Util.logLine(NAME, "<WARN> unable to save new lock"));
    }
  }

  private void removeLock(Block b) {
    String ownerUid = b.getMetadata(MDAT_LockOwner).get(0).asString();
    b.removeMetadata(MDAT_LockOwner, Plugin.INSTANCE);
    b.removeMetadata(MDAT_LockPassword, Plugin.INSTANCE);
    b.removeMetadata(MDAT_LockOrigin, Plugin.INSTANCE);
    Location loc = b.getLocation();
    if (b.getState().getBlockData() instanceof Door) {
      if (((Door) b.getState().getBlockData()).getHalf() == Bisected.Half.TOP) {
        Block b1 = b.getRelative(BlockFace.DOWN);
        b1.removeMetadata(MDAT_LockPassword, Plugin.INSTANCE);
        b1.removeMetadata(MDAT_LockOwner, Plugin.INSTANCE);
        b1.removeMetadata(MDAT_LockOrigin, Plugin.INSTANCE);
        loc = b1.getLocation();
      } else {
        Block b1 = b.getRelative(BlockFace.UP);
        b1.removeMetadata(MDAT_LockPassword, Plugin.INSTANCE);
        b1.removeMetadata(MDAT_LockOwner, Plugin.INSTANCE);
        b1.removeMetadata(MDAT_LockOrigin, Plugin.INSTANCE);
      }
    }

    String lock = "[W]" + loc.getWorld().getName() + "[X]" + loc.getBlockX() + "[Y]" + loc.getBlockY() + "[Z]" + loc.getBlockZ();
    config.set(CFGKEY_PlayerLocks + "." + ownerUid + "." + lock, null);

    try {
      config.save(configFile);
    } catch (IOException e) {
      Plugin.INSTANCE.getServer().getConsoleSender().sendMessage(Util.logLine(NAME, "<WARN> unable to deletion of lock"));
    }
  }
}
