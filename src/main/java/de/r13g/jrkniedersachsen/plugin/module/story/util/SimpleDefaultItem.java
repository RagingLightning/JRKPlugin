package de.r13g.jrkniedersachsen.plugin.module.story.util;

import com.google.gson.Gson;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class SimpleDefaultItem extends SimpleItem {

  ItemJsonCarrier carrier;

  public SimpleDefaultItem(ItemStack stack) {
    super(stack);
  }

  @Override
  public boolean stack(SimpleItem other, boolean test) {
    if (!super.stack(other, test)) return false;
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

  @Override
  public String getItemJson() {
    if (mcMaterial == null) return "";
    if (carrier == null) {
      carrier = new ItemJsonCarrier(mcMaterial, displayName, lore);
    }
    return new Gson().toJson(carrier);
  }

  private class ItemJsonCarrier {

    ItemJsonCarrier(String id, String name, List<String> lore) {
      this.id = id;
      this.tag = new Tag();
      this.tag.display = new Display(name, lore);
    }

    String id;
    Tag tag;

    private class Tag {
      Display display;
    }

    private class Display {

      Display(String name, List<String> lore) {
        this.Name = name;
        this.Lore = lore;
      }

      String Name;
      List<String> Lore;
    }

  }
}
