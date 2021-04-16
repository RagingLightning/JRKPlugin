package de.r13g.jrkniedersachsen.plugin.module.story.quest.task;

import com.google.gson.Gson;
import de.r13g.jrkniedersachsen.plugin.module.story.quest.QuestTask;
import de.r13g.jrkniedersachsen.plugin.module.story.util.SimpleItem;
import de.r13g.jrkniedersachsen.plugin.util.Util;
import me.pikamug.localelib.LocaleManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

public class CollectItemTask extends QuestTask implements Listener {

  private static final String notificationStartCustom = "[{\"text\":\"Sammle @countx\",\"italic\": true,\"color\":\"gray\"}," +
          "{\"text\":\"[@name]\",\"color\":\"white\",\"hoverEvent\":" +
          "{\"action\":\"show_item\",\"value\":\"@json\"}}]";
  private static final String notificationStartDefault = "[{\"text\":\"Sammle @countx\",\"italic\":true,\"color\":\"gray\"}," +
          "{\"text\":\"[\",\"italic\":true,\"color\":\"white\"}," +
          "{\"translate\":\"@key\",\"italic\":true,\"color\":\"white\",\"hoverEvent\":{\"action\":\"show_item\",\"value\":\"@json\"}}," +
          "{\"text\":\"]\",\"italic\":true,\"color\":\"white\"}]";

  private static final String notificationEndCustom = "[{\"text\":\"Task vollendet; @countx\",\"italic\": true,\"color\":\"gray\"}," +
          "{\"text\":\"[@name]\",\"color\":\"white\",\"hoverEvent\":" +
          "{\"action\":\"show_item\",\"value\":\"@json\"}}," +
          "{\"text\":\" eingesammelt\",\"italic\":true,\"color\":gray}]";
  private static final String notificationEndDefault = "[{\"text\":\"Task vollendet; @countx\",\"italic\":true,\"color\":\"gray\"}," +
          "{\"text\":\"[\",\"italic\":true,\"color\":\"white\"}," +
          "{\"translate\":\"@key\",\"italic\":true,\"color\":\"white\",\"hoverEvent\":{\"action\":\"show_item\",\"value\":\"@json\"}}," +
          "{\"text\":\"]\",\"italic\":true,\"color\":\"white\"}," +
          "{\"text\":\" eingesammelt\",\"italic\":true,\"color\":gray}]";

  SimpleItem item;

  @EventHandler
  public void onInventoryOpen(InventoryOpenEvent ev) {
    OfflinePlayer p = (OfflinePlayer) ev.getPlayer();
    if (quest.story.progress.get(p).currentQuests.containsKey(quest.id)) {
      for (ItemStack s : ev.getPlayer().getInventory().getContents()) {
        if (s == null) continue;
        SimpleItem i = SimpleItem.fromItemStack(s);
        if (item.stack(i, true)) {
          int diff = item.stackDifference(i);
          if (diff >= 0) {
            s.setAmount(diff);
            quest.story.progress.get(p).finishTask(quest, id);
            break;
          }
        }
      }
    }
  }

  @Override
  public void announceStart(Player p) {
    String itemJson = new Gson().toJson(item.getItemStack()).replaceAll("\"", "\\\"");

    if (item.displayName != null) {
      Util.tellRaw(p, notificationStartCustom
              .replaceAll("@name", item.displayName)
              .replaceAll("@count", String.valueOf(item.count))
              .replaceAll("@json", itemJson)
      );
    } else {
      Util.tellRaw(p, notificationStartDefault
              .replaceAll("@key", new LocaleManager().queryMaterial(item.getItemStack().getType()))
              .replaceAll("@count", String.valueOf(item.count))
              .replaceAll("@json", itemJson)
      );
    }
  }

  @Override
  public void announceEnd(Player p) {
    String itemJson = new Gson().toJson(item.getItemStack()).replaceAll("\"", "\\\"");

    if (item.displayName != null) {
      Util.tellRaw(p, notificationEndCustom
              .replaceAll("@name", item.displayName)
              .replaceAll("@count", String.valueOf(item.count))
              .replaceAll("@json", itemJson)
      );
    } else {
      Util.tellRaw(p, notificationEndDefault
              .replaceAll("@key", new LocaleManager().queryMaterial(item.getItemStack().getType()))
              .replaceAll("@count", String.valueOf(item.count))
              .replaceAll("@json", itemJson)
      );
    }
  }
}
