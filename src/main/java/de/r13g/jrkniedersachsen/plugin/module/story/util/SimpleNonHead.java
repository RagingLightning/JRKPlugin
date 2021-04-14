package de.r13g.jrkniedersachsen.plugin.module.story.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SimpleNonHead extends SimpleItem {

  public SimpleNonHead(ItemStack stack) {
    this.material = stack.getType().toString();
    this.count = stack.getAmount();
    this.displayName = stack.getItemMeta().getDisplayName();
    this.lore = stack.getItemMeta().getLore();
  }

  @Override
  public boolean stack(SimpleItem other, boolean test) {
    if (!(other instanceof SimpleNonHead)) return false;
    if (!other.material.equals(this.material)) return false;
    if (this.displayName == null) {
      if (other.displayName != null) return false;
    } else {
      if (other.displayName == null) return false;
      if (!this.displayName.equals(other.displayName)) return false;
    }
    if (this.lore == null) {
      if (other.lore != null) return false;
    } else {
      if (other.lore == null) return false;
      if (!this.lore.equals(other.lore)) return false;
    }
    if (!test)
      count += other.count;
    return true;
  }

  @Override
  public ItemStack getItemStack() {
    ItemStack s = new ItemStack(Material.valueOf(material), count);
    ItemMeta m = s.getItemMeta();
    if (displayName != null)
      m.setDisplayName(displayName);
    if (lore != null)
      m.setLore(lore);
    s.setItemMeta(m);
    return s;
  }
}
