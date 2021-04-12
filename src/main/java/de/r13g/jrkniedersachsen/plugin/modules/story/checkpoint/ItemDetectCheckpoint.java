package de.r13g.jrkniedersachsen.plugin.modules.story.checkpoint;

import de.r13g.jrkniedersachsen.plugin.modules.story.StoryCheckpoint;
import de.r13g.jrkniedersachsen.plugin.modules.story.StoryProgress;
import de.r13g.jrkniedersachsen.plugin.modules.story.util.SimpleItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ItemDetectCheckpoint extends StoryCheckpoint implements Listener {

  SimpleItem detectItem;
  boolean consume;
  transient ItemStack itemStack;
  List<UUID> unlocks;

  @EventHandler
  public void onInventoryClose(InventoryCloseEvent ev) {
    if (itemStack == null)
      itemStack = detectItem.getItemStack();
    Player p = (Player) ev.getPlayer();
    if (StoryProgress.get(p, containingStory).checkpoints.contains(id) && p.getInventory().contains(itemStack.getType())) {
      AtomicInteger i = new AtomicInteger();
      p.getInventory().all(itemStack.getType()).forEach((k, v) -> {
        if (!v.getItemMeta().getDisplayName().equals(itemStack.getItemMeta().getDisplayName())) return;
        if (v.getItemMeta().getLore() != itemStack.getItemMeta().getLore()) return;
        i.addAndGet(v.getAmount());
      });
      if (i.get() >= itemStack.getAmount()) {
        if (consume) {
          AtomicInteger j = new AtomicInteger(itemStack.getAmount());
          p.getInventory().all(itemStack.getType()).forEach((k, v) -> {
            if (!v.getItemMeta().getDisplayName().equals(itemStack.getItemMeta().getDisplayName())) return;
            if (v.getItemMeta().getLore() != itemStack.getItemMeta().getLore()) return;
            if (v.getAmount() <= j.get()) {
              j.getAndAdd(-v.getAmount());
              p.getInventory().remove(v);
            } else {
              v.setAmount(v.getAmount()-j.get());
            }
          });
        }
        StoryProgress.get(p, containingStory).unlock(StoryCheckpoint.get(unlocks));
      }
    }
  }

}
