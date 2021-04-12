package de.r13g.jrkniedersachsen.plugin.modules.story.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.UUID;

public class SimpleItem {
  public String materal;
  public int count;
  public String displayName;
  public List<String> lore;

  public ItemStack getItemStack() {
    ItemStack s = new ItemStack(Material.valueOf(materal), count);
    ItemMeta m = s.getItemMeta();
    if (displayName != null)
      m.setDisplayName(displayName);
    if (lore != null)
      m.setLore(lore);
    s.setItemMeta(m);
    return s;
  }

  public class SimpleHead extends SimpleItem {
    public UUID owner;

    @Override
    public ItemStack getItemStack() {
      ItemStack s = new ItemStack(Material.PLAYER_HEAD, count);
      SkullMeta m = (SkullMeta) s.getItemMeta();
      m.setOwningPlayer(Bukkit.getOfflinePlayer(owner));
      if (displayName != null)
        m.setDisplayName(displayName);
      if (lore != null)
        m.setLore(lore);
      s.setItemMeta(m);
      return s;
    }
  }
}