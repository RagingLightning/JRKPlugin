package de.r13g.jrkniedersachsen.plugin.module.story.quest.reward;

import de.r13g.jrkniedersachsen.plugin.module.story.npc.StoryNpcLineSet;
import de.r13g.jrkniedersachsen.plugin.module.story.quest.QuestReward;
import org.bukkit.entity.Player;

import java.util.UUID;

public class DialogueReward extends QuestReward {

  UUID npc;
  StoryNpcLineSet lineSet;

  @Override
  public boolean reward(Player p) {
    lineSet.tell(quest.story.getNpc(npc), p);
    return false;
  }
}
