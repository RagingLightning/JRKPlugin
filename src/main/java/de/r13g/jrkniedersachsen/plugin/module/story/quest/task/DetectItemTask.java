package de.r13g.jrkniedersachsen.plugin.module.story.quest.task;

import de.r13g.jrkniedersachsen.plugin.module.story.quest.QuestTask;
import de.r13g.jrkniedersachsen.plugin.module.story.util.SimpleItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class DetectItemTask extends QuestTask implements Listener {

  SimpleItem item;

  @EventHandler
  public void onInventoryOpen(InventoryOpenEvent ev) {
    Player p = (Player) ev.getPlayer();
    if (quest.story.progress.get(p).currentQuests.containsKey(quest.id)) {
      for (ItemStack s : ev.getPlayer().getInventory().getContents()) {
        if (s == null) continue;
        SimpleItem i = SimpleItem.fromItemStack(s);
        if (item.stack(i, true) && item.stackDifference(i) >= 0) {
          quest.story.progress.get(p).finishTask(quest, id);
          break;
        }
      }
    }
  }

  @Override
  public void notifyPlayer(Player p) {
    throw new NotImplementedException();
  }
}
