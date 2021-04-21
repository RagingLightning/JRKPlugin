package de.r13g.jrkniedersachsen.plugin.module.story.quest.reward;

import de.r13g.jrkniedersachsen.plugin.module.story.npc.StoryNpc;
import de.r13g.jrkniedersachsen.plugin.module.story.npc.StoryNpcLineSet;
import de.r13g.jrkniedersachsen.plugin.module.story.quest.QuestReward;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.UUID;

public class DialogueReward extends QuestReward {

  UUID npc;
  StoryNpcLineSet lineSet;

  @Override
  public boolean reward(Player p) {
    try {
      Method m = StoryNpc.class.getMethod("lineSetCallback", Player.class, boolean.class);
      lineSet.tell(quest.story.getNpc(npc), p, m);
      return false;
    } catch (Exception e) {
      throw new AssertionError("Method StoryNpc::lineSetCallback doesn't exist");
    }
  }
}
