package de.r13g.jrkniedersachsen.plugin.module.story.quest.task;

import de.r13g.jrkniedersachsen.plugin.module.story.quest.QuestTask;
import net.minecraft.server.v1_16_R3.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTameEvent;

import java.util.Map;

public class TameTask extends QuestTask implements Listener {

  String entityType;
  int count;

  @EventHandler
  public void onEntityTame(EntityTameEvent ev) {
    Player p = (Player) ev.getOwner();
    if (quest.story.progress.get(p).currentQuests.containsKey(quest.id)) {
      if (ev.getEntity().getType() == EntityType.valueOf(entityType)) {
        Map<String, Object> data = quest.story.progress.get(p).getTaskData(this);
        if (!data.containsKey("alreadyTamed"))
          data.put("alreadyTamed", 0);
        data.put("alreadyTamed", (Integer) data.get("alreadyTamed") + 1);
        if ((Integer) data.get("alreadyTamed") >= count)
          quest.story.progress.get(p).finishTask(quest, id);
      }
    }
  }

  @Override
  public void notifyPlayer(Player p) {

  }
}
