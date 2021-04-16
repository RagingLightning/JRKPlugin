package de.r13g.jrkniedersachsen.plugin.module.story.quest.task;

import de.r13g.jrkniedersachsen.plugin.module.story.quest.QuestTask;
import org.bukkit.entity.Player;

public class ExternalTask extends QuestTask {

  public String notificationStart, notificationEnd;

  @Override
  public void announceStart(Player p) {
    if (notificationStart != null)
      p.sendMessage(notificationStart);
  }

  @Override
  public void announceEnd(Player p) {
    if (notificationEnd != null)
      p.sendMessage(notificationEnd);
  }
}
