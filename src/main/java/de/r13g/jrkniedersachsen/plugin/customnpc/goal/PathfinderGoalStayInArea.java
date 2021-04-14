package de.r13g.jrkniedersachsen.plugin.customnpc.goal;

import net.minecraft.server.v1_16_R3.*;

import java.util.EnumSet;

public class PathfinderGoalStayInArea extends PathfinderGoal {

  private final EntityInsentient a; //This Entity

  private final double f; //Speed
  private final float g; //Distance

  private double c; //X
  private double d; //Y
  private double e; //Z

  public PathfinderGoalStayInArea(EntityInsentient a, double c, double d, double e, double f, float g) {
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

  public boolean b() { //runs every tick after e (repeats e)
    return this.a.h(c, d, e) > (double) (this.g*this.g)/4;
  }

  public void c() { //runs once when a is true
  }

  public void d() { //gets called once when b is false
  }

  public void e() { //gets called every tick if a or b are true
    this.a.getNavigation().a(c, d, e, f);
  }
}
