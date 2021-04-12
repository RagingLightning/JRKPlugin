package de.r13g.jrkniedersachsen.plugin.modules.story.checkpoint.rewards;

import de.r13g.jrkniedersachsen.plugin.modules.story.checkpoint.CheckpointReward;
import de.r13g.jrkniedersachsen.plugin.modules.story.util.SimpleItem;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

public class ItemReward extends CheckpointReward {

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
