package de.r13g.jrkniedersachsen.plugin.module.story.npc;

import de.r13g.jrkniedersachsen.plugin.Plugin;
import de.r13g.jrkniedersachsen.plugin.customnpc.CustomVillager;
import de.r13g.jrkniedersachsen.plugin.module.story.PlayerProgressEntry;
import de.r13g.jrkniedersachsen.plugin.module.story.util.NpcTradeEndListener;
import de.r13g.jrkniedersachsen.plugin.module.story.util.SimpleItem;
import de.r13g.jrkniedersachsen.plugin.util.Util;
import net.minecraft.server.v1_16_R3.VillagerProfession;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftAbstractVillager;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StoryVillager extends StoryNpc {

  public static final String NAME = "--Villager";

  String profession;
  transient VillagerProfession prof;
  List<StoryNpcOffer> offers;

  @Override
  public boolean load() {
    Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Loading StoryVillager " + name + " (id:" + id + ")..."));
    if (prof == null)
      determineProfession();
    base = new CustomVillager(location.getLocation(), prof);
    behaviour.applyPathfinderGoals(base, 2);

    setup();

    Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "initialized, spawning..."));
    if (spawn()) {
      Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "spawned in " + location.world + " at x:" + location.x + " y:" +
              location.y + " z:" + location.z + " with entityId " + base.getUniqueID()));
      return true;
    } else {
      Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "failed to spawn", ChatColor.YELLOW));
      return true;
    }
  }

  @Override
  public boolean unload() {
    return despawn();
  }

  @Override
  public void onPlayerInteractEntity(PlayerInteractEntityEvent ev) {
    Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Processing PlayerInteract as StoryVillager"));
    PlayerProgressEntry progress = story.progress.get(ev.getPlayer());
    Map<SimpleItem, StoryNpcOffer> successItems = updateTrades(progress);
    if (successItems != null && successItems.size() > 0) {
      Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Villager has trades, opening trade inventory..."));
      ev.setCancelled(true);
      Bukkit.getPluginManager().registerEvents(new NpcTradeEndListener(story, ev.getPlayer(), successItems), Plugin.INSTANCE);
      ev.getPlayer().openMerchant((AbstractVillager) CraftAbstractVillager.getEntity((CraftServer) Bukkit.getServer(), base), false);
    } else {
      Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Villager has no trades"));
      super.onPlayerInteractEntity(ev);
    }
  }

  /**
   * updates the trade table if applicable
   *
   * @return map from bought item to fulfilled offer
   */
  public Map<SimpleItem, StoryNpcOffer> updateTrades(PlayerProgressEntry progress) {
    Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Updating trades..."));
    Map<SimpleItem, StoryNpcOffer> successItems = new HashMap<>();
    List<MerchantRecipe> trades = new ArrayList<>();
    for (StoryNpcOffer offer : offers) {
      String log = "Trade: " + offer.items[2] + " for ";
      log += offer.items[0] == null ? "null, " : offer.items[0] + ", ";
      log += offer.items[1] == null ? "null" : offer.items[1];
      log += " maxUses: " + offer.uses + " active: " + (progress.finishedQuests.contains(offer.dependsOn) ? "1" : "0");
      Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, log));
      if (progress.finishedQuests.contains(offer.dependsOn)) {
        MerchantRecipe r = new MerchantRecipe(offer.getItemStack(2), offer.uses);
        r.addIngredient(offer.getItemStack(0));
        r.addIngredient(offer.getItemStack(1));
        r.setVillagerExperience(0);
        r.setExperienceReward(false);
        trades.add(r);
        successItems.put(offer.items[2], offer);
      }
    }
    Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Trades updated successfully"));
    ((CustomVillager) base).setTrades(trades);
    return successItems;
  }

  /**
   * determines VillagerProfession from String profession
   */
  private void determineProfession() {
    switch (profession) {
      case "NONE":
        prof = VillagerProfession.NONE;
        break;
      case "ARMORER":
        prof = VillagerProfession.ARMORER;
        break;
      case "BUTCHER":
        prof = VillagerProfession.BUTCHER;
        break;
      case "CARTOGRAPHER":
        prof = VillagerProfession.CARTOGRAPHER;
        break;
      case "CLERIC":
        prof = VillagerProfession.CLERIC;
        break;
      case "FARMER":
        prof = VillagerProfession.FARMER;
        break;
      case "FISHERMAN":
        prof = VillagerProfession.FISHERMAN;
        break;
      case "FLETCHER":
        prof = VillagerProfession.FLETCHER;
        break;
      case "LEATHERWORKER":
        prof = VillagerProfession.LEATHERWORKER;
        break;
      case "LIBRARIAN":
        prof = VillagerProfession.LIBRARIAN;
        break;
      case "MASON":
        prof = VillagerProfession.MASON;
        break;
      case "NITWIT":
        prof = VillagerProfession.NITWIT;
        break;
      case "SHEPHERD":
        prof = VillagerProfession.SHEPHERD;
        break;
      case "TOOLSMITH":
        prof = VillagerProfession.TOOLSMITH;
        break;
      case "WEAPONSMITH":
        prof = VillagerProfession.WEAPONSMITH;
        break;
    }
  }
}
