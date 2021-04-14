package de.r13g.jrkniedersachsen.plugin.module.story.util;

import de.r13g.jrkniedersachsen.plugin.module.story.*;
import de.r13g.jrkniedersachsen.plugin.module.story.npc.StoryNpcOffer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class NpcTradeEndListener implements Listener {

  private Story story;
  private Player player;
  private Map<ItemStack, StoryNpcOffer> successItems;

  public NpcTradeEndListener(Story story, Player player, Map<ItemStack, StoryNpcOffer> successItems) {
    this.story = story;
    this.player = player;
    this.successItems = successItems;
  }

  @EventHandler
  public void onInventoryClose(InventoryCloseEvent ev) {
    if (ev.getPlayer() == player) {
      for (ItemStack i : successItems.keySet()) {
        if (player.getInventory().contains(i)) {
          StoryNpcOffer offer = successItems.get(i);
          if (offer.unlocks != null)
            offer.unlocks.forEach((k ,v) -> story.progress.get(player).finishTask(story.getQuest(k), v));
        }
      }
      HandlerList.unregisterAll(this);
    }
  }

}
