package de.r13g.jrkniedersachsen.plugin.modules.story;

import de.r13g.jrkniedersachsen.plugin.modules.story.util.SimpleItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import java.lang.reflect.Array;
import java.util.*;

public class StoryNpc {

  private static Map<UUID, StoryNpc> registeredNpcs = new HashMap<>();

  private transient StoryNpcLine defaultLine = new StoryNpcLine("Ich habe dir gerade nichts zu erzählen...", false);

  public transient UUID containingStory;

  public UUID id;
  public transient Entity base;
  public Type type;
  public List<UUID> lines, offers;

  public static StoryNpc get(UUID id) {
    if (registeredNpcs.containsKey(id))
      return registeredNpcs.get(id);
    return null;
  }

  public StoryNpcLine getLine(PlayerStoryProgress progress) {
    for (UUID lnId : lines) {
      StoryNpcLine line = StoryNpcLine.get(lnId);
      for (UUID cpId : progress.checkpoints) {
        if (line.dependsOn.equals(cpId))
          return line;
      }
    }
    return null;
  }

  /**
   * tells the player the line for the story progress and handles the last message
   *
   * @param progress
   * @param p
   */
  public void tellLine(PlayerStoryProgress progress, Player p) {
    StoryNpcLine line = getLine(progress);
    if (line == null) line = defaultLine;
    StoryNpcLine last = line.tell(p);
    if (last.unlocks != null) {
      progress.unlock(StoryCheckpoint.get(last.unlocks));
    }
  }

  /**
   * updates the trade table if applicable
   *
   * @return map from bought item to fulfilled offer
   */
  public Map<ItemStack, StoryNpcOffer> updateTrades(PlayerStoryProgress progress) {
    if (type != Type.VILLAGER) return null;
    Map<ItemStack, StoryNpcOffer> successItems = new HashMap<>();
    List<MerchantRecipe> trades = new ArrayList<>();
    for (UUID ofId : offers) {
      if (progress.checkpoints.contains(ofId)) {
        StoryNpcOffer offer = StoryNpcOffer.get(ofId);
        MerchantRecipe r = new MerchantRecipe(offer.getItemStack(2), offer.uses);
        r.addIngredient(offer.getItemStack(0));
        r.addIngredient(offer.getItemStack(1));
        r.setVillagerExperience(0);
        r.setExperienceReward(false);
        trades.add(r);
        successItems.put(r.getResult(), offer);
      }
    }
    ((Villager) base).setRecipes(trades);
    return successItems;
  }

  public enum Type {
    VILLAGER,
    ZOMBIE
  }

}
