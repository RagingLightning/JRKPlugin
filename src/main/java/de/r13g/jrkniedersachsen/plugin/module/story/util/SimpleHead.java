package de.r13g.jrkniedersachsen.plugin.module.story.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class SimpleHead extends SimpleItem {

  public String owner;

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
