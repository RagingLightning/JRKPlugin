package de.r13g.jrkniedersachsen.plugin.module.story.quest.task;

import com.google.gson.Gson;
import de.r13g.jrkniedersachsen.plugin.module.story.quest.QuestTask;
import de.r13g.jrkniedersachsen.plugin.module.story.util.SimpleItem;
import de.r13g.jrkniedersachsen.plugin.util.Util;
import me.pikamug.localelib.LocaleManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;

import java.util.Map;

public class CraftItemTask extends QuestTask implements Listener {

  private static final String notificationStart = "[{\"text\":\"Stelle @countx\",\"italic\":true,\"color\":\"gray\"}," +
          "{\"text\":\"[\",\"italic\":true,\"color\":\"white\"}," +
          "{\"translate\":\"@key\",\"italic\":true,\"color\":\"white\",\"hoverEvent\":{\"action\":\"show_item\",\"value\":\"@json\"}}," +
          "{\"text\":\"]\",\"italic\":true,\"color\":\"white\"}," +
          " \"text\":\" her\",\"italic\":true,\"color\":gray}]";

  private static final String notificationEnd = "[{\"text\":\"Task vollendet; @countx\",\"italic\":true,\"color\":\"gray\"}," +
          "{\"text\":\"[\",\"italic\":true,\"color\":\"white\"}," +
          "{\"translate\":\"@key\",\"italic\":true,\"color\":\"white\",\"hoverEvent\":{\"action\":\"show_item\",\"value\":\"@json\"}}," +
          "{\"text\":\"]\",\"italic\":true,\"color\":\"white\"}," +
          " \"text\":\" hergestellt\",\"italic\":true,\"color\":gray}]";

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
  public void announceStart(Player p) {
    String itemJson = new Gson().toJson(item.getItemStack()).replaceAll("\"", "\\\"");
    Util.tellRaw(p, notificationStart
            .replaceAll("@key", new LocaleManager().queryMaterial(item.getItemStack().getType()))
            .replaceAll("@count", String.valueOf(item.count))
            .replaceAll("@json", itemJson)
    );
  }

  @Override
  public void announceEnd(Player p) {
    String itemJson = new Gson().toJson(item.getItemStack()).replaceAll("\"", "\\\"");
    Util.tellRaw(p, notificationEnd
            .replaceAll("@key", new LocaleManager().queryMaterial(item.getItemStack().getType()))
            .replaceAll("@count", String.valueOf(item.count))
            .replaceAll("@json", itemJson)
    );
  }
}
