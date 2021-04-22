package de.r13g.jrkniedersachsen.plugin.module.story.quest.task;

import de.r13g.jrkniedersachsen.plugin.module.story.quest.QuestTask;
import de.r13g.jrkniedersachsen.plugin.util.Util;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;

import java.util.Map;

public class KillTask extends QuestTask implements Listener {

  private static final String notificationStart = "[{\"text\":\"Töte @countx\",\"italic\":true,\"color\":\"gray\"}," +
          "{\"text\":\"[\",\"italic\":true,\"color\":\"white\"}," +
          "{\"translate\":\"@key\",\"italic\":true,\"color\":\"white\"}," +
          "{\"text\":\"]\",\"italic\":true,\"color\":\"white\"}]";

  private static final String notificationEnd = "[{\"text\":\"Task vollendet; @countx\",\"italic\":true,\"color\":\"gray\"}," +
          "{\"text\":\"[\",\"italic\":true,\"color\":\"white\"}," +
          "{\"translate\":\"@key\",\"italic\":true,\"color\":\"white\"}," +
          "{\"text\":\"]\",\"italic\":true,\"color\":\"white\"}," +
          "{\"text\":\" getötet\",\"italic\":true,\"color\":\"gray\"}]";

  String entityType;
  int count;

  @EventHandler
  public void onEntityKill(PlayerStatisticIncrementEvent ev) {
    if (ev.getStatistic().getKey() != Statistic.KILL_ENTITY.getKey()) return;

    Player p = ev.getPlayer();
    if (quest.story.progress.get(p).currentQuests.containsKey(quest.id)) {
      if (quest.story.progress.get(p).currentQuests.get(quest.id).tasks.get(id).finished) return;
      if (ev.getEntityType() == EntityType.valueOf(entityType)) {
        Map<String, Object> data = quest.story.progress.get(p).getTaskData(this);
        if (!data.containsKey("alreadyKilled"))
          data.put("alreadyKilled", 0);
        data.put("alreadyKilled", (double) data.get("alreadyKilled") + 1);
        if ((double) data.get("alreadyKilled") >= count)
          quest.story.progress.get(p).finishTask(this);
      }
    }
  }

  @Override
  public void announceStart(Player p) {

    Util.tellRaw(p, notificationStart
            .replace("@key", "entity.minecraft." + entityType.toLowerCase())
            .replace("@count", String.valueOf(count))
    );
  }

  @Override
  public void announceEnd(Player p) {

    Util.tellRaw(p, notificationEnd
            .replace("@key", "entity.minecraft." + entityType.toLowerCase())
            .replace("@count", String.valueOf(count))
    );
  }
}
