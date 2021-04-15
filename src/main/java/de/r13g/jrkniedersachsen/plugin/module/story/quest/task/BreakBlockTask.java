package de.r13g.jrkniedersachsen.plugin.module.story.quest.task;

import de.r13g.jrkniedersachsen.plugin.module.story.quest.QuestTask;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Map;

public class BreakBlockTask extends QuestTask implements Listener {

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
  public void notifyPlayer(Player p) {

  }
}
