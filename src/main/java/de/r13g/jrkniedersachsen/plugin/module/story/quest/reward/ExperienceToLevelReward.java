package de.r13g.jrkniedersachsen.plugin.module.story.quest.reward;

import de.r13g.jrkniedersachsen.plugin.module.story.quest.QuestReward;
import org.bukkit.entity.Player;

public class ExperienceToLevelReward extends QuestReward {

  public int level;

  @Override
  public boolean reward(Player p) {
    p.giveExpLevels(level-p.getLevel());
    return true;
  }
}
