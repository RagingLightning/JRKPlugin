package de.r13g.jrkniedersachsen.plugin.module.story.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class SimpleLocation {

  public String name, world;
  public double x, y, z;

  public SimpleLocation(Location loc, String name) {
    this(loc);
    this.name = name;
  }

  public SimpleLocation(Location loc) {
    this.world = loc.getWorld().getName();
    this.x = loc.getX();
    this.y = loc.getY();
    this.z = loc.getZ();
  }

  public Location getLocation() {
    return new Location(Bukkit.getWorld(world), x, y, z);
  }


}
