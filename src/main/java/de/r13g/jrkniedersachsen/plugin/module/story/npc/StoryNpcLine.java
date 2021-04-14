package de.r13g.jrkniedersachsen.plugin.module.story.npc;

public class StoryNpcLine {

  public String message;
  public boolean isJson;
  public long msDelayAfter;

  public StoryNpcLine(String message, boolean isJson, long msDelayAfter) {
    this.message = message;
    this.isJson = isJson;
    this.msDelayAfter = msDelayAfter;
  }
}
