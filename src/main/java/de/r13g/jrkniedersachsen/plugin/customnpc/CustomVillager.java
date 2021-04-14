package de.r13g.jrkniedersachsen.plugin.customnpc;

import com.mojang.serialization.Dynamic;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;

import java.util.List;

public class CustomVillager extends EntityVillager {

  public CustomVillager(Location location, VillagerProfession profession) {
    super(EntityTypes.VILLAGER, ((CraftWorld) location.getWorld()).getHandle());
    this.setPosition(location.getX(), location.getY(), location.getZ());
    this.setInvulnerable(true);
    this.setVillagerData(this.getVillagerData().withProfession(profession));

    /*List<org.bukkit.inventory.MerchantRecipe> a = new ArrayList<>();
    org.bukkit.inventory.MerchantRecipe r = new org.bukkit.inventory.MerchantRecipe(new org.bukkit.inventory.ItemStack(Material.DIAMOND), 1);
    r.addIngredient(new org.bukkit.inventory.ItemStack(Material.IRON_INGOT, 2));
    r.addIngredient(new org.bukkit.inventory.ItemStack(Material.COAL, 5));
    a.add(r);

    setTrades(a);*/
  }

  public CustomVillager(Location loc, VillagerProfession prof, PathfinderGoal mainGoal) {
    this(loc, prof);

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
  }

  @Override
  protected BehaviorController<?> a(Dynamic<?> dynamic) {
    return this.cK().a(dynamic);
  }

  @Override
  public void c(WorldServer worldserver) {
  }

  @Override
  protected void b(MerchantRecipe merchantRecipe) {
  }

  @Override
  protected void eW() {
  } //called to initialize trades, not in use, replaced by setTrades(List)
}
