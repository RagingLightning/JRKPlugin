package de.r13g.jrkniedersachsen.plugin.module.story.util;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SimplePotionItem extends SimpleItem {

  public String effect;
  public int duration, amplifier;

  @Override
  public boolean stack(SimpleItem other, boolean test) {
    return false;
  }

  @Override
  public ItemStack getItemStack() {
    ItemStack s = new ItemStack(Material.valueOf(material));
    PotionMeta m = (PotionMeta) s.getItemMeta();
    m.addCustomEffect(new PotionEffect(PotionEffectType.getByName(effect), duration, amplifier), true);
    return null;
  }
}
