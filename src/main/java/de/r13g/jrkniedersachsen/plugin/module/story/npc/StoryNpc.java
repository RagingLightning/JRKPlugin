package de.r13g.jrkniedersachsen.plugin.module.story.npc;

import com.google.gson.*;
import de.r13g.jrkniedersachsen.plugin.Plugin;
import de.r13g.jrkniedersachsen.plugin.module.StoryModule;
import de.r13g.jrkniedersachsen.plugin.module.story.Story;
import de.r13g.jrkniedersachsen.plugin.module.story.StoryProgress;
import de.r13g.jrkniedersachsen.plugin.module.story.util.SimpleBehaviour;
import de.r13g.jrkniedersachsen.plugin.module.story.util.SimpleItem;
import de.r13g.jrkniedersachsen.plugin.module.story.util.SimpleLocation;
import de.r13g.jrkniedersachsen.plugin.util.Util;
import net.minecraft.server.v1_16_R3.EntityCreature;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftLivingEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;
import java.util.UUID;

public abstract class StoryNpc {

  public static final String NAME = "--Npc";

  private static StoryNpcLine defaultLine = new StoryNpcLine( "Ich habe dir gerade nichts zu erzählen...", false, 0L);

  public transient Story story;

  public transient UUID id;
  protected transient EntityCreature base;
  public String name;
  Type type;
  boolean invulnerable;
  SimpleLocation location;
  SimpleBehaviour behaviour;
  SimpleItem head, chest, legs, feet, mainHand, offHand;
  List<StoryNpcLineSet> lineSets;

  public static StoryNpc create(Story story, Type type, Location location) {
    throw new NotImplementedException();
  }

  public abstract boolean load();

  public abstract boolean unload();

  protected void setup() {
    CraftLivingEntity cle = (CraftLivingEntity) base.getBukkitEntity();

    cle.setMetadata(StoryModule.MDAT_StoryNpcLeast, new FixedMetadataValue(Plugin.INSTANCE, id.getLeastSignificantBits()));
    cle.setMetadata(StoryModule.MDAT_StoryNpcMost, new FixedMetadataValue(Plugin.INSTANCE, id.getMostSignificantBits()));

    cle.setCustomName(name);
    cle.setCustomNameVisible(true);
    cle.setInvulnerable(invulnerable);

    if (head != null)
      cle.getEquipment().setHelmet(head.getItemStack());
    if (chest != null)
      cle.getEquipment().setChestplate(chest.getItemStack());
    if (legs != null)
      cle.getEquipment().setLeggings(legs.getItemStack());
    if (feet != null)
      cle.getEquipment().setBoots(feet.getItemStack());
  }

  public boolean spawn() {
    base.setLocation(location.x, location.y, location.z, 0, 0);
    ((CraftWorld)location.getLocation().getWorld()).addEntity(base, CreatureSpawnEvent.SpawnReason.CUSTOM);
    return Bukkit.getEntity(base.getUniqueID()) != null;
  }

  public boolean despawn() {
    base.killEntity();
    return true;
  }

  public void onPlayerInteractEntity(PlayerInteractEntityEvent ev) {
    Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Processing PlayerInteract as StoryNpc"));
    ev.setCancelled(true);
    tellLineSet(story.progress.get(ev.getPlayer()), ev.getPlayer());
  }

  public void onEntityDamage(EntityDamageEvent ev) {

    if (ev.getCause() == EntityDamageEvent.DamageCause.VOID) return;
    Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Processing EntityDamage as StoryNpc"));
    ev.setCancelled(invulnerable);
    if (ev instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) ev).getDamager() instanceof Player) {
      Player p = (Player) ((EntityDamageByEntityEvent) ev).getDamager();
      tellLineSet(story.progress.get(p), p);
    }
  }

  public StoryNpcLineSet getLineSet(StoryProgress.PlayerEntry progress) {
    for (StoryNpcLineSet set : lineSets) {
      for (UUID cpId : progress.finishedQuests) {
        if (set.dependsOn.equals(cpId))
          return set;
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
  public void tellLineSet(StoryProgress.PlayerEntry progress, Player p) {
    StoryNpcLineSet set = getLineSet(progress);
    if (set == null) {
      set = new StoryNpcLineSet(defaultLine);
      Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Npc has no applicable Line, telling default line"));
    } else {
      Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "telling line set for player checkpoint"));
    }
    if(set.tell(this, p))
      for (UUID questId : set.unlocks.keySet()) {
        int taskId = set.unlocks.get(questId);
        progress.finishTask(story.getQuest(questId), taskId);
      }
    else
      throw new AssertionError("StoryNpcLineSet::tell returned false");
  }

  public enum Type {
    VILLAGER(EntityType.VILLAGER),
    WANDERING_TRADER(EntityType.WANDERING_TRADER),
    PLAYER(EntityType.CREEPER),
    ZOMBIE(EntityType.ZOMBIE);

    public transient final EntityType entityType;

    Type(EntityType entityType) {
      this.entityType = entityType;
    }
  }

  public static class Adapter implements JsonDeserializer<StoryNpc> {
    @Override
    public StoryNpc deserialize(JsonElement e, java.lang.reflect.Type _t, JsonDeserializationContext c) throws JsonParseException {
      JsonObject o = e.getAsJsonObject();
      String t = o.get("type").getAsString();
      switch (Type.valueOf(t)) {
        case VILLAGER: return c.deserialize(e, StoryVillager.class);
        case WANDERING_TRADER: return c.deserialize(e, StoryTrader.class);
        case PLAYER: return c.deserialize(e, StoryPlayer.class);
      }
      throw new JsonParseException("StoryNpc has unknown type '" + t + "'");
    }
  }

}
