package de.r13g.jrkniedersachsen.plugin.module.story.util;

import de.r13g.jrkniedersachsen.plugin.module.story.Story;
import de.r13g.jrkniedersachsen.plugin.module.story.npc.StoryNpcOffer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NpcTradeEndListener implements Listener {

  private Story story;
  private Player player;
  private Map<SimpleItem, StoryNpcOffer> successItems;
  private List<SimpleItem> snapshotItems;

  public NpcTradeEndListener(Story story, Player player, Map<SimpleItem, StoryNpcOffer> successItems) {
    this.story = story;
    this.player = player;
    this.successItems = successItems;

    // Get Snapshot of inventory items, as negative
    this.snapshotItems = listItems(true);
  }

  @EventHandler
  public void onInventoryClose(InventoryCloseEvent ev) {
    if (ev.getPlayer() == player) {
      //Get state of inventory items
      List<SimpleItem> after = listItems(false);

      //merge with snapshot
      for (SimpleItem a : after) {
        for (SimpleItem b : snapshotItems) {
          if (a.stack(b, false)) break;
        }
      }

      //only consider items which increased in count
      after.removeIf(a -> a.count <= 0);

      //check for success items
      for (SimpleItem i : after) {
        for (SimpleItem s : successItems.keySet()) {
          if (s.stack(i, true) && i.count >= s.count) {
            StoryNpcOffer offer = successItems.get(s);
            if (offer.unlocks != null)
              offer.unlocks.forEach((k, v) -> story.progress.get(player).finishTask(story.getQuest(k).getTask(v)));
            break;
          }
        }
      }

      HandlerList.unregisterAll(this);
    }
  }

  private List<SimpleItem> listItems(boolean negate) {
    List<SimpleItem> list = new ArrayList<>();

    for (ItemStack s : player.getInventory().getContents()) {
      if (s == null) continue;
      SimpleItem i = SimpleItem.fromItemStack(s);
      if (negate)
        i.count *= -1;
      boolean[] merged = {false};
      list.forEach(r -> {
        if (r.stack(i, false)) merged[0] = true;
      });
      if (!merged[0])
        list.add(i);
    }

    return list;
  }

}
