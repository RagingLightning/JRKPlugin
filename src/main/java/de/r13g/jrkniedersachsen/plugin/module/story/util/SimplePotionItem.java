package de.r13g.jrkniedersachsen.plugin.module.story.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SimplePotionItem extends SimpleItem {

  public String effect;
  public int duration, amplifier;

  SimplePotionItem(ItemStack stack) {
    super(stack);
    this.effect = ((PotionMeta) stack.getItemMeta()).getCustomEffects().get(0).getType().toString();
    this.duration = ((PotionMeta) stack.getItemMeta()).getCustomEffects().get(0).getDuration();
    this.amplifier = ((PotionMeta) stack.getItemMeta()).getCustomEffects().get(0).getAmplifier();
  }

  @Override
  public boolean stack(SimpleItem other, boolean test) {
    if (!(other instanceof SimplePotionItem))
      return false;
    if (!this.effect.equals(((SimplePotionItem) other).effect))
      return false;
    if (this.amplifier != ((SimplePotionItem) other).amplifier)
      return false;
    if (this.duration != ((SimplePotionItem) other).duration)
      return false;
    if (!test)
      this.count += other.count;
    return true;
  }

  @Override
  public ItemStack getItemStack() {
    ItemStack s = new ItemStack(Material.valueOf(material), count);
    PotionMeta m = (PotionMeta) s.getItemMeta();
    m.addCustomEffect(new PotionEffect(PotionEffectType.getByName(effect), duration, amplifier), true);
    if (displayName != null)
      m.setDisplayName(displayName);
    if (lore != null)
      m.setLore(lore);
    s.setItemMeta(m);
    return s;
  }

  @Override
  public String getItemJson() {
    return null;
  }
}
