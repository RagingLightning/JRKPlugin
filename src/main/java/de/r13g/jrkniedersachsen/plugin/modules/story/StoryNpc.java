package de.r13g.jrkniedersachsen.plugin.modules.story;

import com.sun.prism.shader.AlphaOne_Color_AlphaTest_Loader;
import de.r13g.jrkniedersachsen.plugin.Plugin;
import de.r13g.jrkniedersachsen.plugin.modules.StoryModule;
import de.r13g.jrkniedersachsen.plugin.modules.story.util.NpcTradeEndListener;
import de.r13g.jrkniedersachsen.plugin.modules.story.util.SimpleItem;
import de.r13g.jrkniedersachsen.plugin.modules.story.util.SimpleLocation;
import net.minecraft.server.v1_16_R3.Slot;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.*;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;

public class StoryNpc {

  private static Map<UUID, StoryNpc> registeredNpcs = new HashMap<>();

  private transient StoryNpcLine defaultLine = new StoryNpcLine(null, "Ich habe dir gerade nichts zu erzählen...", false);

  transient Story containingStory;

  public UUID id;
  UUID baseId;
  transient Entity base;
  public String name;
  Type type;
  boolean invulnerable;
  SimpleLocation location;
  SimpleItem head, chest, legs, feet, mainHand, offHand;
  List<UUID> lines, offers;


  public static StoryNpc get(UUID id) {
    if (registeredNpcs.containsKey(id))
      return registeredNpcs.get(id);
    return null;
  }

  public static boolean register(UUID id, StoryNpc npc) {
    if (registeredNpcs.containsKey(id)) return false;
    registeredNpcs.put(id, npc);
    return true;
  }

  public static void loadAll() {
    registeredNpcs.forEach((k,v) -> v.load());
  }

  public void load() {
    base = Bukkit.getEntity(baseId);
    if (base == null) {
      base = location.getLocation().getWorld().spawnEntity(location.getLocation(), type.entityType);
    }
    if (base.getType() != type.entityType) {
      throw new AssertionError("Story NPC id:" + id + " has in world base with EntityType " + base.getType() + " expecting EntityType " + type.entityType);
    }
    base.setPersistent(true);
    base.setCustomName(name);
    base.setCustomNameVisible(true);
    base.setMetadata(StoryModule.MDAT_StoryNpcMost, new FixedMetadataValue(Plugin.INSTANCE, id.getMostSignificantBits()));
    base.setMetadata(StoryModule.MDAT_StoryNpcLeast, new FixedMetadataValue(Plugin.INSTANCE, id.getLeastSignificantBits()));
    if (base instanceof LivingEntity) {
      EntityEquipment e = ((LivingEntity) base).getEquipment();
      if (head != null)
        e.setHelmet(head.getItemStack());
      if (chest != null)
        e.setChestplate(chest.getItemStack());
      if (legs != null)
        e.setLeggings(legs.getItemStack());
      if (feet != null)
        e.setBoots(feet.getItemStack());
      if (mainHand != null)
        e.setItemInMainHand(mainHand.getItemStack());
      if (offHand != null)
        e.setItemInOffHand(offHand.getItemStack());
    }
  }

  public void unload() {
    base.remove();
    base = null;
  }

  public void unregister() {
    registeredNpcs.remove(id);
  }

  public void onPlayerInteractEntity(PlayerInteractEntityEvent ev) {
    ev.setCancelled(true);
    tellLine(StoryProgress.get(ev.getPlayer(), containingStory), ev.getPlayer());
  }

  public void onEntityDamage(EntityDamageEvent ev) {
    ev.setCancelled(invulnerable);
    if (ev instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) ev).getDamager() instanceof Player) {
      Player p = (Player) ((EntityDamageByEntityEvent) ev).getDamager();
      tellLine(StoryProgress.get(p, containingStory), p);
    }
  }

  public StoryNpcLine getLine(StoryProgress progress) {
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
  public void tellLine(StoryProgress progress, Player p) {
    StoryNpcLine line = getLine(progress);
    if (line == null) line = defaultLine;
    StoryNpcLine last = line.tell(this, p);
    if (last.unlocks != null) {
      progress.unlock(StoryCheckpoint.get(last.unlocks));
    }
  }


  public class StoryVillager extends StoryNpc {
    List<UUID> offers;

    @Override
    public void onPlayerInteractEntity(PlayerInteractEntityEvent ev) {
      StoryProgress progress = StoryProgress.get(ev.getPlayer(), containingStory);
      Map<ItemStack, StoryNpcOffer> successItems = updateTrades(progress);
      if (successItems != null && successItems.size() > 0) {
        ev.setCancelled(true);
        ev.getPlayer().openMerchant((Villager) base, false);
        Bukkit.getPluginManager().registerEvents(new NpcTradeEndListener(containingStory, ev.getPlayer(), successItems), Plugin.INSTANCE);
      } else {
        super.onPlayerInteractEntity(ev);
      }
    }

    /**
     * updates the trade table if applicable
     *
     * @return map from bought item to fulfilled offer
     */
    public Map<ItemStack, StoryNpcOffer> updateTrades(StoryProgress progress) {
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
  }

  public enum Type {
    VILLAGER(EntityType.VILLAGER),
    ZOMBIE(EntityType.ZOMBIE);

    public transient final EntityType entityType;

    Type(EntityType entityType) {
      this.entityType = entityType;
    }
  }

}
