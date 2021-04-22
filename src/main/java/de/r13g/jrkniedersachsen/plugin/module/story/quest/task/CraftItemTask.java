package de.r13g.jrkniedersachsen.plugin.module.story.quest.task;

import de.r13g.jrkniedersachsen.plugin.module.story.quest.QuestTask;
import de.r13g.jrkniedersachsen.plugin.module.story.util.SimpleItem;
import de.r13g.jrkniedersachsen.plugin.util.Util;
import me.pikamug.localelib.LocaleManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;

import java.util.Map;

public class CraftItemTask extends QuestTask implements Listener {

  //TODO: Fix not working

  private static final String notificationStart = "[{\"text\":\"Stelle @countx\",\"italic\":true,\"color\":\"gray\"}," +
          "{\"text\":\"[\",\"italic\":true,\"color\":\"white\"}," +
          "{\"translate\":\"@key\",\"italic\":true,\"color\":\"white\",\"hoverEvent\":{\"action\":\"show_item\",\"value\":\"@json\"}}," +
          "{\"text\":\"]\",\"italic\":true,\"color\":\"white\"}," +
          "{\"text\":\" her\",\"italic\":true,\"color\":\"gray\"}]";

  private static final String notificationEnd = "[{\"text\":\"Task vollendet; @countx\",\"italic\":true,\"color\":\"gray\"}," +
          "{\"text\":\"[\",\"italic\":true,\"color\":\"white\"}," +
          "{\"translate\":\"@key\",\"italic\":true,\"color\":\"white\",\"hoverEvent\":{\"action\":\"show_item\",\"value\":\"@json\"}}," +
          "{\"text\":\"]\",\"italic\":true,\"color\":\"white\"}," +
          "{\"text\":\" hergestellt\",\"italic\":true,\"color\":\"gray\"}]";

  SimpleItem item;

  @EventHandler
  public void onCraftItem(CraftItemEvent ev) {
    Bukkit.getConsoleSender().sendMessage("EVENT CALLED");
    OfflinePlayer p = (OfflinePlayer) ev.getWhoClicked();
    if (quest.story.progress.get(p).currentQuests.containsKey(quest.id)) {
      if (quest.story.progress.get(p).currentQuests.get(quest.id).tasks.get(id).finished) return;
      SimpleItem i = SimpleItem.fromItemStack(ev.getCurrentItem());
      Bukkit.getConsoleSender().sendMessage("HAS CURRENT, ITEM: " + i.toString());
      if (item != null && item.stack(i, true)) {
        Bukkit.getConsoleSender().sendMessage("STACKS");
        Map<String, Object> data = quest.story.progress.get(p).getTaskData(this);
        if (!data.containsKey("alreadyCrafted"))
          data.put("alreadyCrafted", 0);
        data.put("alreadyCrafted", (Integer) data.get("alreadyCrafted") + i.count);
        if (((Integer) data.get("alreadyCrafted")) >= item.count) {
          Bukkit.getConsoleSender().sendMessage("FINISHED");
          quest.story.progress.get(p).finishTask(this);
        }
      }
    }
  }

  @Override
  public void announceStart(Player p) {
    String itemJson = item.getItemJson().replace("\"", "\\\"");
    Bukkit.getConsoleSender().sendMessage(itemJson);
    Util.tellRaw(p, notificationStart
            .replace("@key", new LocaleManager().queryMaterial(item.getItemStack().getType()))
            .replace("@count", String.valueOf(item.count))
            .replace("@json", itemJson)
    );
  }

  @Override
  public void announceEnd(Player p) {
    String itemJson = item.getItemJson().replace("\"", "\\\"");
    Util.tellRaw(p, notificationEnd
            .replace("@key", new LocaleManager().queryMaterial(item.getItemStack().getType()))
            .replace("@count", String.valueOf(item.count))
            .replace("@json", itemJson)
    );
  }
}
