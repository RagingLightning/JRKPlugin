package de.r13g.jrkniedersachsen.plugin.module.story.quest.task;

import de.r13g.jrkniedersachsen.plugin.module.story.quest.QuestTask;
import de.r13g.jrkniedersachsen.plugin.module.story.util.SimpleLocation;
import de.r13g.jrkniedersachsen.plugin.util.Util;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class LocationTask extends QuestTask implements Listener {

  private static final String notification = "[{\"text\":\"Du hast \",\"italic\":true,\"color\":\"gray\"}," +
          "{\"text\":\"@name\",\"italic\":true,\"color\":\"white\"}," +
          "{\"text\":\" erreicht\",\"italic\":true,\"color\":\"gray\"}]";

  SimpleLocation location;
  float radius;

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent ev) {
    Player p = ev.getPlayer();
    if (quest.story.progress.get(p).currentQuests.containsKey(quest.id)) {
      if (ev.getTo().distanceSquared(location.getLocation()) <= radius * radius) {
        quest.story.progress.get(p).finishTask(quest, id);
      }
    }
  }

  @Override
  public void notifyPlayer(Player p) {
    if (location.name != null) {
      Util.tellRaw(p, notification.replaceAll("@name", location.name));
    }
  }
}
