package de.r13g.jrkniedersachsen.plugin.module.story;

import de.r13g.jrkniedersachsen.plugin.module.story.quest.QuestReward;
import de.r13g.jrkniedersachsen.plugin.module.story.quest.QuestTask;

import java.util.*;

public class StoryQuest {

  public static final String NAME = "--Quest";

  public transient Story story;

  public transient UUID id;
  protected String name;
  protected String desc;
  protected List<UUID> children;
  protected boolean announceStart;
  protected transient UUID parent;

  Map<Integer, QuestTask> tasks;
  List<QuestReward> rewards;

  transient List<UUID> activePlayers;

  StoryQuest(Story story, String name) {
    this.story = story;
    this.name = name;
    this.tasks = new HashMap<>();
    this.rewards = new ArrayList<>();
    this.activePlayers = new ArrayList<>();
  }

  /* ---Quest Logic--- */

  /**
   * load this checkpoint
   *
   * @return success
   */
  boolean load() {
    try {
      if (tasks != null) {
        tasks.forEach((k, v) -> {
          v.quest = this;
          v.id = k;
        });
      }
      if (rewards != null) {
        rewards.forEach(v -> v.quest = this);
      }
      activePlayers = new ArrayList<>();
      if (children == null) return true;
      children.forEach(id -> story.getQuest(id).parent = this.id);
      return true;
    } catch (NullPointerException e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * unload this checkpoint
   *
   * @return success
   */
  boolean unload() {
    return true;
  }

  List<StoryQuest> getChildren() {
    return story.getAllQuests(children);
  }

  StoryQuest getParent() {
    return story.getQuest(parent);
  }

  public QuestTask getTask(int id) {
    if (tasks.keySet().contains(id))
      return tasks.get(id);
    return null;
  }
}
