package de.r13g.jrkniedersachsen.plugin.customnpc;

import de.r13g.jrkniedersachsen.plugin.module.story.util.SimpleBehaviour;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class CustomPlayerPather extends EntityVillagerTrader {

  public CustomPlayer npc;

  public CustomPlayerPather(CustomPlayer npc) {
    super(EntityTypes.WANDERING_TRADER, npc.getWorld());
    this.npc = npc;
    npc.getWorld().addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
    setPosition(npc.lastX, npc.lastY, npc.lastZ);
    setInvisible(true);
    setInvulnerable(true);
    setSilent(true);
  }

  @Override
  protected void initPathfinder() {
    goalSelector.a(0, new PathfinderGoalFloat(this));
    this.goalSelector.a(1, new PathfinderGoalAvoidTarget(this, EntityZombie.class, 8.0F, 0.5D, 0.5D));
    this.goalSelector.a(1, new PathfinderGoalAvoidTarget(this, EntityEvoker.class, 12.0F, 0.5D, 0.5D));
    this.goalSelector.a(1, new PathfinderGoalAvoidTarget(this, EntityVindicator.class, 8.0F, 0.5D, 0.5D));
    this.goalSelector.a(1, new PathfinderGoalAvoidTarget(this, EntityVex.class, 8.0F, 0.5D, 0.5D));
    this.goalSelector.a(1, new PathfinderGoalAvoidTarget(this, EntityPillager.class, 15.0F, 0.5D, 0.5D));
    this.goalSelector.a(1, new PathfinderGoalAvoidTarget(this, EntityIllagerIllusioner.class, 12.0F, 0.5D, 0.5D));
    this.goalSelector.a(1, new PathfinderGoalAvoidTarget(this, EntityZoglin.class, 10.0F, 0.5D, 0.5D));
    this.goalSelector.a(SimpleBehaviour.maxNeededGoalSlots + 2, new PathfinderGoalInteract(this, EntityHuman.class, 3.0F, 1.0F));
    this.goalSelector.a(SimpleBehaviour.maxNeededGoalSlots + 3, new PathfinderGoalLookAtPlayer(this, EntityInsentient.class, 8.0F));
  }
}
