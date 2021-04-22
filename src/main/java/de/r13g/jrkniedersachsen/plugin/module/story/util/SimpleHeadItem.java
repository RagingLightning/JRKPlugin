package de.r13g.jrkniedersachsen.plugin.module.story.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class SimpleHeadItem extends SimpleItem {

  public String owner;

  public SimpleHeadItem(ItemStack stack) {
    super(stack);
    if (((SkullMeta) stack.getItemMeta()).getOwningPlayer() == null)
      this.owner = new UUID(0, 0).toString();
    else
      this.owner = ((SkullMeta) stack.getItemMeta()).getOwningPlayer().getUniqueId().toString();
  }

  @Override
  public boolean stack(SimpleItem other, boolean test) {
    if (!(other instanceof SimpleHeadItem)) return false;
    if (!super.stack(other, test))
      return false;
    if (this.owner != ((SimpleHeadItem) other).owner)
      return false;
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

  @Override
  public String getItemJson() {
    return null;
  }
}
