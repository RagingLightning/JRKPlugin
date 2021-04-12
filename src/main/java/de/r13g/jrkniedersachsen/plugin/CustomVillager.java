package de.r13g.jrkniedersachsen.plugin;

import de.r13g.jrkniedersachsen.plugin.util.Util;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class CustomVillager extends EntityVillager {

  public CustomVillager(Location location) {
    super(EntityTypes.VILLAGER, ((CraftWorld) location.getWorld()).getHandle());
    this.setPosition(location.getX(), location.getY(), location.getZ());
    this.setInvulnerable(true);

    //TODO: Manage to suppress Villager AI

    ((Map)Util.getPrivateField("c", PathfinderGoalSelector.class, goalSelector)).clear();
    ((Map)Util.getPrivateField("c", PathfinderGoalSelector.class, targetSelector)).clear();
    ((Set)Util.getPrivateField("d", PathfinderGoalSelector.class, goalSelector)).clear();
    ((Set)Util.getPrivateField("d", PathfinderGoalSelector.class, targetSelector)).clear();

  }
}
