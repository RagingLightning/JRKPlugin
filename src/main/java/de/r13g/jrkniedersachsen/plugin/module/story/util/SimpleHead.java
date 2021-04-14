package de.r13g.jrkniedersachsen.plugin.module.story.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class SimpleHead extends SimpleItem {

  public String owner;

  public SimpleHead(ItemStack stack) {
    this.material = stack.getType().toString();
    this.count = stack.getAmount();
    this.displayName = stack.getItemMeta().getDisplayName();
    this.lore = stack.getItemMeta().getLore();
    if (((SkullMeta) stack.getItemMeta()).getOwningPlayer() == null)
      this.owner = new UUID(0, 0).toString();
    else
      this.owner = ((SkullMeta) stack.getItemMeta()).getOwningPlayer().getUniqueId().toString();
  }

  @Override
  public boolean stack(SimpleItem other, boolean test) {
    if (!(other instanceof SimpleHead)) return false;
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
    if (this.owner == null) {
      if (((SimpleHead) other).owner != null) return false;
    } else {
      if (((SimpleHead) other).owner == null) return false;
      if (!this.owner.equals(((SimpleHead) other).owner)) return false;
    }
    if (!test)
      count += other.count;
    return true;
  }

  @Override
  public int stackDifference(SimpleItem other) {
    if (!this.stack(other, true))
      throw new UnsupportedOperationException("The SimpleItems are not of the same type");
    return this.count - other.count;
  }

  @Override
  public ItemStack getItemStack() {
    ItemStack s = new ItemStack(Material.PLAYER_HEAD, count);
    SkullMeta m = (SkullMeta) s.getItemMeta();
    UUID id = UUID.fromString(owner);
    m.setOwningPlayer(Bukkit.getOfflinePlayer(id));
    if (displayName != null)
      m.setDisplayName(displayName);
    if (lore != null)
      m.setLore(lore);
    s.setItemMeta(m);
    return s;
  }
}
