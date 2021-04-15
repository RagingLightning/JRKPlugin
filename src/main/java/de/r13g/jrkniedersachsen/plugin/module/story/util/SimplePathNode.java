package de.r13g.jrkniedersachsen.plugin.module.story.util;

import org.bukkit.Location;

public class SimplePathNode {

  public SimpleLocation location;
  public long stayTicks;

  public SimplePathNode(Location location, long stayTicks) {
    this.location = new SimpleLocation(location);
    this.stayTicks = stayTicks;
  }

}
