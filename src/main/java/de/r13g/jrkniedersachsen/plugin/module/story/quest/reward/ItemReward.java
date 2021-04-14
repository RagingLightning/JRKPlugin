package de.r13g.jrkniedersachsen.plugin.module.story.quest.reward;

import de.r13g.jrkniedersachsen.plugin.module.story.quest.QuestReward;
import de.r13g.jrkniedersachsen.plugin.module.story.util.SimpleItem;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

public class ItemReward extends QuestReward {

  SimpleItem item;

  public ItemReward(SimpleItem item) {
    this.type = Type.ITEM;
    this.item = item;
  }

  @Override
  public boolean reward(Player p) {
    Item i = (Item) p.getWorld().spawnEntity(p.getLocation(), EntityType.DROPPED_ITEM);
    i.setItemStack(item.getItemStack());
    i.setOwner(p.getUniqueId());
    return true;
  }
}
