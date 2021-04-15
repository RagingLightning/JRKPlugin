package de.r13g.jrkniedersachsen.plugin.module.story.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class QuestSave {

  public Map<Integer, TaskSave> tasks;

  public QuestSave(Set<Integer> taskKeySet) {
    tasks = new HashMap<>();
    taskKeySet.forEach(i -> tasks.put(i, new TaskSave()));
  }

}
