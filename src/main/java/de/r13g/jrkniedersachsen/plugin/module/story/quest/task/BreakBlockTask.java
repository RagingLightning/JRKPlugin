package de.r13g.jrkniedersachsen.plugin.module.story.quest.task;

import de.r13g.jrkniedersachsen.plugin.module.story.quest.QuestTask;
import de.r13g.jrkniedersachsen.plugin.util.Util;
import me.pikamug.localelib.LocaleManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Map;

public class BreakBlockTask extends QuestTask implements Listener {

  private static final String notificationStart = "[{\"text\":\"Baue @countx\",\"italic\":true,\"color\":\"gray\"}," +
          "{\"text\":\"[\",\"italic\":true,\"color\":\"white\"}," +
          "{\"translate\":\"@key\",\"italic\":true,\"color\":\"white\"}," +
          "{\"text\":\"]\",\"italic\":true,\"color\":\"white\"}," +
          "{\"text\":\" ab\",\"italic\":true,\"color\":gray}]";

  private static final String notificationEnd = "[{\"text\":\"Task vollendet; @countx\",\"italic\":true,\"color\":\"gray\"}," +
          "{\"text\":\"[\",\"italic\":true,\"color\":\"white\"}," +
          "{\"translate\":\"@key\",\"italic\":true,\"color\":\"white\"}," +
          "{\"text\":\"]\",\"italic\":true,\"color\":\"white\"}," +
          "{\"text\":\" abgebaut\",\"italic\":true,\"color\":gray}]";

  String block;
  int count;

  @EventHandler
  public void onBlockBreak(BlockBreakEvent ev) {
    Player p = ev.getPlayer();
    if (quest.story.progress.get(p).currentQuests.containsKey(quest.id)) {
      if (Material.valueOf(block) == ev.getBlock().getType()) {
        Map<String, Object> data = quest.story.progress.get(p).getTaskData(this);
        if (!data.containsKey("alreadyBroken"))
          data.put("alreadyBroken", 0);
        data.put("alreadyBroken", (Integer) data.get("alreadyBroken") + 1);
        if ((Integer) data.get("alreadyBroken") >= count) {
          quest.story.progress.get(p).finishTask(quest, id);
        }
      }
    }
  }

  @Override
  public void announceStart(Player p) {
    Util.tellRaw(p, notificationStart
            .replaceAll("@key", new LocaleManager().queryMaterial(Material.valueOf(block)))
            .replaceAll("@count", String.valueOf(count))
    );
  }

  @Override
  public void announceEnd(Player p) {
    Util.tellRaw(p, notificationEnd
            .replaceAll("@key", new LocaleManager().queryMaterial(Material.valueOf(block)))
            .replaceAll("@count", String.valueOf(count))
    );
  }
}
