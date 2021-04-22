package de.r13g.jrkniedersachsen.plugin.module.story.npc;

import de.r13g.jrkniedersachsen.plugin.module.story.util.SimpleItem;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;

public class StoryNpcOffer {

  public SimpleItem[] items = {null, null, null};
  public int uses;
  public UUID dependsOn = null;
  public Map<UUID, Integer> unlocks = null;

  public ItemStack getItemStack(int index) {
    if (items[index] != null)
      return items[index].getItemStack();
    return null;
  }

}
