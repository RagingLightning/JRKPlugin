package de.r13g.jrkniedersachsen.plugin.module.story.quest.task;

import com.mysql.jdbc.NotImplemented;
import de.r13g.jrkniedersachsen.plugin.module.story.quest.QuestTask;
import de.r13g.jrkniedersachsen.plugin.module.story.util.SimpleItem;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Map;

public class CraftItemTask extends QuestTask implements Listener {

  SimpleItem item;

  @EventHandler
  public void onCraftItem(CraftItemEvent ev) {
    OfflinePlayer p = (OfflinePlayer) ev.getWhoClicked();
    if (quest.story.progress.get(p).currentQuests.containsKey(quest.id)) {
      SimpleItem i = SimpleItem.fromItemStack(ev.getCurrentItem());
      if (item != null && item.stack(i, true)) {
        Map<String, Object> data = quest.story.progress.get(p).getTaskData(this);
        if (!data.containsKey("alreadyCrafted"))
          data.put("alreadyCrafted", 0);
        data.put("alreadyCrafted", (Integer) data.get("alreadyCrafted") + i.count);
        if (((Integer) data.get("alreadyCrafted")) >= item.count) {
          quest.story.progress.get(p).finishTask(quest, id);
        }
      }
    }
  }

  @Override
  public void notifyPlayer(Player p) {
    throw new NotImplementedException();
  }
}
