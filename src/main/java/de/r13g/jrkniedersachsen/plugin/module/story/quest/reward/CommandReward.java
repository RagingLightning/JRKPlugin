package de.r13g.jrkniedersachsen.plugin.module.story.quest.reward;

import de.r13g.jrkniedersachsen.plugin.module.story.quest.QuestReward;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CommandReward extends QuestReward {

  private String command;

  public CommandReward(String command) {
    this.type = Type.COMMAND;
    this.command = command;
  }

  @Override
  public boolean reward(Player p) {
    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("@p", p.getName()));
    return true;
  }
}
