package de.r13g.jrkniedersachsen.plugin.modules.story;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerStoryProgress {

  List<UUID> checkpoints = new ArrayList<>();

  transient Player player;

  public void unlock(StoryCheckpoint cp) {
    checkpoints.add(cp.id);
    if (checkpoints.contains(cp.parentCheckpoint)) {
      checkpoints.remove(cp.parentCheckpoint);
    }
    if (cp.itemDrop != null) {
      Item i = (Item) player.getWorld().spawnEntity(player.getLocation(), EntityType.DROPPED_ITEM);
      i.setItemStack(cp.itemDrop.getItemStack());
      i.setOwner(player.getUniqueId());
    }
  }

  public void unlock(List<StoryCheckpoint> cps) {
    cps.forEach(this::unlock);
  }

}
