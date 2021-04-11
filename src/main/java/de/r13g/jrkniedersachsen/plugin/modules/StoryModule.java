package de.r13g.jrkniedersachsen.plugin.modules;

import de.r13g.jrkniedersachsen.plugin.Plugin;
import de.r13g.jrkniedersachsen.plugin.modules.story.Story;
import de.r13g.jrkniedersachsen.plugin.modules.story.StoryNpc;
import de.r13g.jrkniedersachsen.plugin.modules.story.StoryNpcOffer;
import de.r13g.jrkniedersachsen.plugin.modules.story.StoryProgress;
import de.r13g.jrkniedersachsen.plugin.modules.story.util.NpcTradeEndListener;
import de.r13g.jrkniedersachsen.plugin.modules.story.util.SimpleItem;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class StoryModule implements Module, Listener {

  public static final String NAME = "Story";

  public static final String MDAT_StoryNpcLeast = "StoryNpcLeast";
  public static final String MDAT_StoryNpcMost = "StoryNpcMost";

  @Override
  public boolean load(Plugin plugin, File moduleDataFolder) {
    return false;
  }

  @Override
  public boolean unload() {
    return false;
  }

  @Override
  public boolean isReady() {
    return false;
  }

  @Override
  public List<String[]> getCommands() {
    return null;
  }

  @Override
  public List<String> getHelpText(Permissible p) {
    return null;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    return false;
  }

  @EventHandler
  public void onPlayerInteractEntity(PlayerInteractEntityEvent ev) { //RIGHT CLICK
    if (ev.getRightClicked().hasMetadata(MDAT_StoryNpcLeast) && ev.getRightClicked().hasMetadata(MDAT_StoryNpcMost)) {
      ev.setCancelled(true);
      long most = ev.getRightClicked().getMetadata(MDAT_StoryNpcMost).get(0).asLong();
      long least = ev.getRightClicked().getMetadata(MDAT_StoryNpcLeast).get(0).asLong();
      StoryNpc npc = StoryNpc.get(new UUID(most, least));
      Story story = Story.get(npc.containingStory);
      if (npc.type == StoryNpc.Type.VILLAGER) {
        Map<ItemStack, StoryNpcOffer> successItems = npc.updateTrades(StoryProgress.get(story).getPlayer(ev.getPlayer()));
        if (successItems != null && successItems.size() > 0) {
          InventoryView iv = ev.getPlayer().openMerchant((Villager) npc.base, false);
          Bukkit.getServer().getPluginManager().registerEvents(new NpcTradeEndListener(story, ev.getPlayer(), successItems), Plugin.INSTANCE);
        } else {
          npc.tellLine(StoryProgress.get(story).getPlayer(ev.getPlayer()), ev.getPlayer());
        }
      }
    }
  }

  @EventHandler
  public void onEntityDamage(EntityDamageEvent ev) { //LEFT CLICK
    if (ev.getEntity().hasMetadata(MDAT_StoryNpcLeast) && ev.getEntity().hasMetadata(MDAT_StoryNpcMost)) {
      ev.setCancelled(true);
      if (ev instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) ev).getDamager() instanceof Player) {
        Player p = (Player) ((EntityDamageByEntityEvent) ev).getDamager();
        long most = ev.getEntity().getMetadata(MDAT_StoryNpcMost).get(0).asLong();
        long least = ev.getEntity().getMetadata(MDAT_StoryNpcLeast).get(0).asLong();
        StoryNpc npc = StoryNpc.get(new UUID(most, least));
        Story story = Story.get(npc.containingStory);
        npc.tellLine(StoryProgress.get(story).getPlayer(p), p);
      }
    }
  }
}
