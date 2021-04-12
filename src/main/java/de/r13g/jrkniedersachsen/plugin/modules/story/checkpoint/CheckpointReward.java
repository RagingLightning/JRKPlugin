package de.r13g.jrkniedersachsen.plugin.modules.story.checkpoint;

import org.bukkit.entity.Player;

public abstract class CheckpointReward {

  public Type type;

  public abstract boolean reward(Player p);

  public enum Type {
    NONE,
    DIALOGUE,
    ITEM,
    EXPERIENCE,
    COMMAND
  }

}
