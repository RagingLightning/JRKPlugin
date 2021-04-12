package de.r13g.jrkniedersachsen.plugin.modules.story.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class SimpleLocation {

  String world;
  double x,y,z;

  public Location getLocation() {
    return new Location(Bukkit.getWorld(world), x , y, z);
  }


}
