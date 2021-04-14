package de.r13g.jrkniedersachsen.plugin.module.story.quest.reward;

import de.r13g.jrkniedersachsen.plugin.module.story.quest.QuestReward;
import de.r13g.jrkniedersachsen.plugin.module.story.quest.QuestTask;
import org.bukkit.entity.Player;

public class ExperiencePointReward extends QuestReward {

  public int amount;

  @Override
  public boolean reward(Player p) {
    p.giveExp(amount);
    return true;
  }
}
