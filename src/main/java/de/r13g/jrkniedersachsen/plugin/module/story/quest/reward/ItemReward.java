package de.r13g.jrkniedersachsen.plugin.module.story.quest.reward;

import de.r13g.jrkniedersachsen.plugin.module.story.quest.QuestReward;
import de.r13g.jrkniedersachsen.plugin.module.story.util.SimpleItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemReward extends QuestReward {

  SimpleItem item;

  public ItemReward(SimpleItem item) {
    this.type = Type.ITEM;
    this.item = item;
  }

  @Override
  public boolean reward(Player p) {
    ItemStack stack = item.getItemStack();
    p.getWorld().dropItem(p.getLocation(), stack).setOwner(p.getUniqueId());
    return true;
  }
}
