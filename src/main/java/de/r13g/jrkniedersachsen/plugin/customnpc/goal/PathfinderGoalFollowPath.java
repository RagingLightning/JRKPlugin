package de.r13g.jrkniedersachsen.plugin.customnpc.goal;

import de.r13g.jrkniedersachsen.plugin.module.story.util.SimplePathNode;
import net.minecraft.server.v1_16_R3.EntityInsentient;
import net.minecraft.server.v1_16_R3.PathfinderGoal;
import org.bukkit.Bukkit;

import java.util.EnumSet;
import java.util.List;

public class PathfinderGoalFollowPath extends PathfinderGoal {

  EntityInsentient a;

  List<SimplePathNode> nodes;
  int i = 0;
  int di = 1;
  SimplePathNode currentNode;

  boolean loop;

  double speed;
  float radius;

  long t = 0;

  public PathfinderGoalFollowPath(EntityInsentient a, List<SimplePathNode> nodes, double speed, float radius, boolean loop) {
    this.a = a;
    this.nodes = nodes;
    this.currentNode = nodes.get(0);
    this.speed = speed;
    this.radius = radius;
    this.loop = loop;
    this.a(EnumSet.of(Type.MOVE)); //Set type of Pathfinder
  }

  public boolean a() { //gets called every tick, decides, whether to run c() based on return value
    if (t >= currentNode.stayTicks) {
      i += di;
      currentNode = nodes.get(i);
    } else
      t++;
    if (this.a.h(currentNode.location.x, currentNode.location.y, currentNode.location.z) <= (double) (this.radius * this.radius))
      return false;
    return true;
  }

  public boolean b() { //runs every tick after e (repeats e)
    return this.a.h(currentNode.location.x, currentNode.location.y, currentNode.location.z) > (double) (this.radius * this.radius) / 4;
  }

  public void c() { //runs once when a is true
    t = 0;
  }

  public void d() { //gets called once when b is false
    if (i < 0) di = 1;
    if (i == nodes.size()-1)
      if (loop) i = -1;
      else di = -1;
  }

  public void e() { //gets called every tick if a or b are true
    this.a.getNavigation().a(currentNode.location.x, currentNode.location.y, currentNode.location.z, speed);
  }
}
