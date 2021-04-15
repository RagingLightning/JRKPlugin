package de.r13g.jrkniedersachsen.plugin.module.story.npc.behaviour;

import de.r13g.jrkniedersachsen.plugin.customnpc.goal.PathfinderGoalStayInArea;
import de.r13g.jrkniedersachsen.plugin.module.story.util.SimpleBehaviour;
import de.r13g.jrkniedersachsen.plugin.module.story.util.SimpleLocation;
import net.minecraft.server.v1_16_R3.EntityCreature;
import org.bukkit.Location;

public class SimpleStayBehaviour extends SimpleBehaviour {

  private SimpleLocation center;
  private double speed;

  public SimpleStayBehaviour(Location center, double speed) {
    this.type = Type.WANDER;
    this.center = new SimpleLocation(center);
    this.speed = speed;
  }

  @Override
  public void applyPathfinderGoals(EntityCreature entity, int startIndex) {
    entity.goalSelector.a(startIndex, new PathfinderGoalStayInArea(entity, center.x, center.y, center.z, speed, 1.5F));
  }
}
