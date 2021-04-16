package de.r13g.jrkniedersachsen.plugin.module.story.npc;

import de.r13g.jrkniedersachsen.plugin.Plugin;
import de.r13g.jrkniedersachsen.plugin.customnpc.CustomPlayer;
import de.r13g.jrkniedersachsen.plugin.customnpc.CustomPlayerPather;
import de.r13g.jrkniedersachsen.plugin.module.StoryModule;
import de.r13g.jrkniedersachsen.plugin.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftLivingEntity;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.UUID;

public class StoryPlayer extends StoryNpc {

  //TODO: Test

  public static final String NAME = "--Player";

  UUID skinPlayer;
  String skinTexture;

  @Override
  public boolean load() {
    Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Loading StoryPlayer " + name + " (id:" + id + ")..."));
    if (skinPlayer != null)
      base = CustomPlayer.create(location.getLocation(), name, skinPlayer);
    else
      base = CustomPlayer.create(location.getLocation(), name, skinTexture, null);
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
    ((CustomPlayerPather) base).npc.destroy();
    return true;
  }

  @Override
  protected void setup() {
    CraftLivingEntity cle = ((CustomPlayerPather) base).npc.getBukkitEntity();

    cle.setMetadata(StoryModule.MDAT_StoryNpcLeast, new FixedMetadataValue(Plugin.INSTANCE, id.getLeastSignificantBits()));
    cle.setMetadata(StoryModule.MDAT_StoryNpcMost, new FixedMetadataValue(Plugin.INSTANCE, id.getMostSignificantBits()));

    base.getBukkitEntity().setMetadata(StoryModule.MDAT_StoryNpcLeast, new FixedMetadataValue(Plugin.INSTANCE, id.getLeastSignificantBits()));
    base.getBukkitEntity().setMetadata(StoryModule.MDAT_StoryNpcMost, new FixedMetadataValue(Plugin.INSTANCE, id.getMostSignificantBits()));

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
}
