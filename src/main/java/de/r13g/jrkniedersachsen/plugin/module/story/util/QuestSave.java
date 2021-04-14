package de.r13g.jrkniedersachsen.plugin.module.story.util;

import java.util.*;

public class QuestSave {

  public Map<Integer, TaskSave> tasks;

  public QuestSave(Set<Integer> taskKeySet) {
    tasks = new HashMap<>();
    taskKeySet.forEach( i -> tasks.put(i, new TaskSave()));
  }

}
