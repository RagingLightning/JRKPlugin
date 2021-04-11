package de.r13g.jrkniedersachsen.plugin.modules.story.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class SimpleItem {
  public String materal;
  public int count;
  public String displayName;
  public List<String> lore;

  public ItemStack getItemStack() {
    ItemStack s = new ItemStack(Material.valueOf(materal), count);
    ItemMeta m = s.getItemMeta();
    m.setDisplayName(displayName);
    m.setLore(lore);
    s.setItemMeta(m);
    return s;
  }
}