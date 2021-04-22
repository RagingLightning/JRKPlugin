package de.r13g.jrkniedersachsen.plugin.module.story.quest.task;

import de.r13g.jrkniedersachsen.plugin.module.story.quest.QuestTask;
import de.r13g.jrkniedersachsen.plugin.module.story.util.SimpleItem;
import de.r13g.jrkniedersachsen.plugin.util.Util;
import me.pikamug.localelib.LocaleManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class DetectItemTask extends QuestTask implements Listener {

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
          "{\"text\":\" erkannt\",\"italic\":true,\"color\":\"gray\"}]";
  private static final String notificationEndDefault = "[{\"text\":\"Task vollendet; @countx\",\"italic\":true,\"color\":\"gray\"}," +
          "{\"text\":\"[\",\"italic\":true,\"color\":\"white\"}," +
          "{\"translate\":\"@key\",\"italic\":true,\"color\":\"white\",\"hoverEvent\":{\"action\":\"show_item\",\"value\":\"@json\"}}," +
          "{\"text\":\"]\",\"italic\":true,\"color\":\"white\"}," +
          "{\"text\":\" erkannt\",\"italic\":true,\"color\":\"gray\"}]";

  SimpleItem item;

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent ev) {
    check(ev.getPlayer());
  }

  private void check(Player p) {
    if (quest.story.progress.get(p).currentQuests.containsKey(quest.id)) {
      if (quest.story.progress.get(p).currentQuests.get(quest.id).tasks.get(id).finished) return;
      for (ItemStack s : p.getInventory().getContents()) {
        if (s == null) continue;
        SimpleItem i = SimpleItem.fromItemStack(s);
        if (item.stack(i, true) && item.stackDifference(i) >= 0) {
          quest.story.progress.get(p).finishTask(this);
          break;
        }
      }
    }
  }

  @Override
  public void announceStart(Player p) {
    String itemJson = item.getItemJson().replace("\"", "\\\"");

    if (item.displayName != null) {
      Util.tellRaw(p, notificationStartCustom
              .replace("@name", item.displayName)
              .replace("@count", String.valueOf(item.count))
              .replace("@json", itemJson)
      );
    } else {
      Util.tellRaw(p, notificationStartDefault
              .replace("@key", new LocaleManager().queryMaterial(item.getItemStack().getType()))
              .replace("@count", String.valueOf(item.count))
              .replace("@json", itemJson)
      );
    }
  }

  @Override
  public void announceEnd(Player p) {
    String itemJson = item.getItemJson().replace("\"", "\\\"");

    if (item.displayName != null) {
      Util.tellRaw(p, notificationEndCustom
              .replace("@name", item.displayName)
              .replace("@count", String.valueOf(item.count))
              .replace("@json", itemJson)
      );
    } else {
      Util.tellRaw(p, notificationEndDefault
              .replace("@key", new LocaleManager().queryMaterial(item.getItemStack().getType()))
              .replace("@count", String.valueOf(item.count))
              .replace("@json", itemJson)
      );
    }
  }
}
