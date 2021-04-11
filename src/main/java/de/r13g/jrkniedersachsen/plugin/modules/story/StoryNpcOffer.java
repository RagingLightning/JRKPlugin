package de.r13g.jrkniedersachsen.plugin.modules.story;

import de.r13g.jrkniedersachsen.plugin.modules.story.util.SimpleItem;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class StoryNpcOffer {

  private static Map<UUID, StoryNpcOffer> registeredOffers = new HashMap<>();

  public UUID id = null;
  public SimpleItem[] items = {null, null, null};
  public int uses;
  public UUID dependsOn = null;
  public List<UUID> unlocks = null;

  public static StoryNpcOffer get(UUID id) {
    if (registeredOffers.containsKey(id))
      return registeredOffers.get(id);
    return null;
  }

  public ItemStack getItemStack(int index) {
    return items[index].getItemStack();
  }

}
