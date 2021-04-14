package de.r13g.jrkniedersachsen.plugin.module.story.util;

import java.util.HashMap;
import java.util.Map;

public class TaskSave {

  public boolean finished;
  public Map<String, Object> data;

  public TaskSave() {
    finished = false;
    data = new HashMap<>();
  }

}
