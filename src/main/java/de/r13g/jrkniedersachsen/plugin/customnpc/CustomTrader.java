package de.r13g.jrkniedersachsen.plugin.customnpc;

import de.r13g.jrkniedersachsen.plugin.module.story.util.SimpleBehaviour;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;

import java.util.List;

public class CustomTrader extends EntityVillagerTrader {

  public CustomTrader(Location location) {
    super(EntityTypes.WANDERING_TRADER, ((CraftWorld) location.getWorld()).getHandle());
  }

  public void setTrades(List<org.bukkit.inventory.MerchantRecipe> trades) {
    this.trades = new MerchantRecipeList();
    if (trades != null)
      trades.forEach(r -> {
        ItemStack in1 = CraftItemStack.asNMSCopy(r.getIngredients().get(0));
        ItemStack in2 = CraftItemStack.asNMSCopy(r.getIngredients().get(1));
        ItemStack res = CraftItemStack.asNMSCopy(r.getResult());
        this.trades.add(new MerchantRecipe(in1, in2, res, r.getUses(), r.getMaxUses(), r.getVillagerExperience(), r.getPriceMultiplier()));
      });
  }

  @Override
  protected void initPathfinder() {
    this.goalSelector.a(0, new PathfinderGoalFloat(this));
    this.goalSelector.a(1, new PathfinderGoalAvoidTarget(this, EntityZombie.class, 8.0F, 0.5D, 0.5D));
    this.goalSelector.a(1, new PathfinderGoalAvoidTarget(this, EntityEvoker.class, 12.0F, 0.5D, 0.5D));
    this.goalSelector.a(1, new PathfinderGoalAvoidTarget(this, EntityVindicator.class, 8.0F, 0.5D, 0.5D));
    this.goalSelector.a(1, new PathfinderGoalAvoidTarget(this, EntityVex.class, 8.0F, 0.5D, 0.5D));
    this.goalSelector.a(1, new PathfinderGoalAvoidTarget(this, EntityPillager.class, 15.0F, 0.5D, 0.5D));
    this.goalSelector.a(1, new PathfinderGoalAvoidTarget(this, EntityIllagerIllusioner.class, 12.0F, 0.5D, 0.5D));
    this.goalSelector.a(1, new PathfinderGoalAvoidTarget(this, EntityZoglin.class, 10.0F, 0.5D, 0.5D));
    this.goalSelector.a(1, new PathfinderGoalLookAtTradingPlayer(this));
    this.goalSelector.a(SimpleBehaviour.maxNeededGoalSlots + 2, new PathfinderGoalInteract(this, EntityHuman.class, 3.0F, 1.0F));
    this.goalSelector.a(SimpleBehaviour.maxNeededGoalSlots + 3, new PathfinderGoalLookAtPlayer(this, EntityInsentient.class, 8.0F));
  }
}
