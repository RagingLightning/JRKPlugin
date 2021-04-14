package de.r13g.jrkniedersachsen.plugin.module.story;

import de.r13g.jrkniedersachsen.plugin.module.story.quest.QuestReward;
import de.r13g.jrkniedersachsen.plugin.module.story.quest.QuestTask;

import java.util.*;

public class StoryQuest {

  public static final String NAME = "--Quest";

  transient Story story;

  protected transient UUID id = null;
  protected String name = null;
  protected String desc = null;
  protected List<UUID> children = null;

  Map<Integer, QuestTask> tasks = new HashMap<>();
  List<QuestReward> rewards = new ArrayList<>();

  transient List<UUID> activePlayers = new ArrayList<>();

  StoryQuest(Story story, String name) {
    this.story = story;
    this.name = name;
  }

  /* ---Quest Logic--- */

  /**
   * load this checkpoint
   *
   * @return success
   */
  boolean load() {
    return true;
  }

  /**
   * unload this checkpoint
   * @return success
   */
  boolean unload() {
    return true;
  }

  List<StoryQuest> getChildren() {
    return story.getAllQuests(children);
  }

  public QuestTask getTask(int id) {
    if (tasks.keySet().contains(id))
      return tasks.get(id);
    return null;
  }
}
