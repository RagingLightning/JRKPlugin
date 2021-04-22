package de.r13g.jrkniedersachsen.plugin.customnpc;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import de.r13g.jrkniedersachsen.plugin.module.story.util.SimpleBehaviour;
import de.r13g.jrkniedersachsen.plugin.util.Util;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CustomVillager extends EntityVillager {

  public CustomVillager(Location location, VillagerProfession profession) {
    super(EntityTypes.VILLAGER, ((CraftWorld) location.getWorld()).getHandle());
    this.setVillagerData(this.getVillagerData().withProfession(profession));
  }

  public void setTrades(List<org.bukkit.inventory.MerchantRecipe> trades) {
    this.trades = new MerchantRecipeList();
    if (trades != null)
      trades.forEach(r -> {
        ItemStack in1 = CraftItemStack.asNMSCopy(r.getIngredients().get(0));
        ItemStack in2 = CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR));
        if (r.getIngredients().size() == 2)
          in2 = CraftItemStack.asNMSCopy(r.getIngredients().get(1));
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
    this.goalSelector.a(1, new PathfinderGoalDoorOpen(this, true));
    this.goalSelector.a(SimpleBehaviour.maxNeededGoalSlots + 2, new PathfinderGoalInteract(this, EntityHuman.class, 3.0F, 1.0F));
    this.goalSelector.a(SimpleBehaviour.maxNeededGoalSlots + 3, new PathfinderGoalLookAtPlayer(this, EntityInsentient.class, 8.0F));
  }

  @Override
  protected BehaviorController<?> a(Dynamic<?> dynamic) {

//    Collection a = (Collection) Util.getPrivateField("a", this.cK().getClass(), this.cK());
//    Collection b = (Collection) Util.getPrivateField("b", this.cK().getClass(), this.cK());
//    Codec c = (Codec) Util.getPrivateField("c", this.cK().getClass(), this.cK());

    try {
      Method mB = BehaviorController.class.getDeclaredMethod("b", Collection.class, Collection.class);
      mB.setAccessible(true);
      Codec c = (Codec) mB.invoke(null, new ArrayList<>(), new ArrayList<>());

      return new BehaviorController(new ArrayList<>(), new ArrayList<>(), ImmutableList.of(), () -> {
        return c;
      });
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
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

  @Override
  protected void mobTick() {
    ((Map) Util.getPrivateField("e", BehaviorController.class, this.getBehaviorController())).clear();
    super.mobTick();
  }

  @Override
  public void a(MemoryModuleType<GlobalPos> memorymoduletype) {
    return;
  }
}
