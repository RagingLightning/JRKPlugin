package de.r13g.jrkniedersachsen.plugin.modules.story.util;

import de.r13g.jrkniedersachsen.plugin.modules.story.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class NpcTradeEndListener implements Listener {

  private Story s;
  private Player p;
  private Map<ItemStack, StoryNpcOffer> offerItems;

  public NpcTradeEndListener(Story s, Player p, Map<ItemStack, StoryNpcOffer> offerItems) {
    this.s = s;
    this.p = p;
    this.offerItems = offerItems;
  }

  @EventHandler
  public void onInventoryClose(InventoryCloseEvent ev) {
    if (ev.getPlayer() == p) {
      for (ItemStack i : offerItems.keySet()) {
        if (p.getInventory().contains(i)) {
          StoryNpcOffer offer = offerItems.get(i);
          if (offer.unlocks != null)
            StoryProgress.get(s).getPlayer(p).unlock(StoryCheckpoint.get(offer.unlocks));
        }
      }
      HandlerList.unregisterAll(this);
    }
  }

}
