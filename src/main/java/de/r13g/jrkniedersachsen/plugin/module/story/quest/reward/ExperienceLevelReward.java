package de.r13g.jrkniedersachsen.plugin.module.story.quest.reward;

import de.r13g.jrkniedersachsen.plugin.module.story.quest.QuestReward;
import org.bukkit.entity.Player;

public class ExperienceLevelReward extends QuestReward {

  public int amount;

  @Override
  public boolean reward(Player p) {
    p.giveExpLevels(amount);
    return true;
  }
}
