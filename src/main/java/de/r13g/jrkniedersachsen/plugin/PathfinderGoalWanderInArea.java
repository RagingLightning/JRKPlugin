package de.r13g.jrkniedersachsen.plugin;

import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityInsentient;
import net.minecraft.server.v1_16_R3.EntityLiving;
import net.minecraft.server.v1_16_R3.PathfinderGoal;
import org.bukkit.Location;

import java.util.EnumSet;

public class PathfinderGoalWanderInArea extends PathfinderGoal {

  private final EntityInsentient a; //This Entity

  private final double f; //Speed
  private final float g; //Distance

  private double c; //X
  private double d; //Y
  private double e; //Z

  public PathfinderGoalWanderInArea(EntityInsentient a, double c, double d, double e, double f, float g) {
    this.a = a;
    this.c = c;
    this.d = d;
    this.e = e;
    this.f = f;
    this.g = g;
    this.a(EnumSet.of(Type.MOVE)); //Set type of Pathfinder
  }

  @Override //gets called every tick, decides, whether to run c() based on return value
  public boolean a() {
    if (this.a.h(c, d, e) <= (double) (this.g * this.g))
      return false;
    return true;
  }

  public void c() {
    //runs after a() returns true and if b() returns true
    this.a.getNavigation().a(c, d, e, f);
  }

  public boolean b() {
    //runs every tick after c (repeats c)
    return this.a.h(c, d, e) > (double) (this.g*this.g)/4;
  }

  public void d() {
    //runs when b() return false
  }
}
