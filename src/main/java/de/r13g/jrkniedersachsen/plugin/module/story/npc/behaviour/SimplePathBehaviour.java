package de.r13g.jrkniedersachsen.plugin.module.story.npc.behaviour;

import de.r13g.jrkniedersachsen.plugin.customnpc.goal.PathfinderGoalFollowPath;
import de.r13g.jrkniedersachsen.plugin.module.story.util.SimpleBehaviour;
import de.r13g.jrkniedersachsen.plugin.module.story.util.SimplePathNode;
import net.minecraft.server.v1_16_R3.EntityCreature;
import net.minecraft.server.v1_16_R3.PathfinderGoalRandomStrollLand;

import java.util.ArrayList;
import java.util.List;

public class SimplePathBehaviour extends SimpleBehaviour {

  public List<SimplePathNode> nodes;
  public float radius;
  public double speed;
  public boolean loop;

  public SimplePathBehaviour(float radius, double speed) {
    this(new ArrayList<>(), radius, speed, true);
  }

  public SimplePathBehaviour(List<SimplePathNode> nodes, float radius, double speed, boolean loop) {
    this.nodes = nodes;
    this.radius = radius;
    this.speed = speed;
    this.loop = loop;
  }

  @Override
  public void applyPathfinderGoals(EntityCreature entity, int startIndex) {
    entity.goalSelector.a(startIndex, new PathfinderGoalFollowPath(entity, nodes, speed, radius, loop));
    entity.goalSelector.a(startIndex + 1, new PathfinderGoalRandomStrollLand(entity, speed));
  }
}
