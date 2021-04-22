package de.r13g.jrkniedersachsen.plugin.module.story.util;

import com.google.gson.Gson;
import de.r13g.jrkniedersachsen.plugin.Plugin;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;

import java.io.File;
import java.util.List;

@SuppressWarnings("deprecation")
public class SimpleFilledMapItem extends SimpleItem {

  transient ItemJsonCarrier carrier;

  int mapId;

  SimpleFilledMapItem(ItemStack stack) {
    super(stack);
    this.mcMaterial = "minecraft:filled_map";
    this.material = "FILLED_MAP";
    this.mapId = ((MapMeta) stack.getItemMeta()).getMapId();
  }

  @Override
  public boolean stack(SimpleItem other, boolean test) {
    if (!(other instanceof SimpleFilledMapItem))
      return false;
    if (!super.stack(other, test))
      return false;
    if (this.mapId != ((SimpleFilledMapItem) other).mapId)
      return false;
    if (!test)
      this.count += other.count;
    return true;
  }

  @Override
  public ItemStack getItemStack() {
    ItemStack stack = new ItemStack(Material.FILLED_MAP);
    MapMeta meta = (MapMeta) stack.getItemMeta();
    File mapData = new File(Plugin.INSTANCE.getDataFolder(), "../../" + Plugin.worldName + "/data/map_" + mapId + ".dat");
    if (!mapData.exists())
      throw new IllegalArgumentException("World folder does not contain map_" + mapId + ".dat mapData");
    meta.setMapId(mapId);
    if (displayName != null)
      meta.setDisplayName(displayName);
    if (lore != null)
      meta.setLore(lore);
    stack.setItemMeta(meta);
    return stack;
  }

  @Override
  public String getItemJson() {
    if (carrier == null)
      carrier = new ItemJsonCarrier("minecraft:filled_map", displayName, lore);
    return new Gson().toJson(carrier);
  }

  private class ItemJsonCarrier {

    ItemJsonCarrier(String id, String name, List<String> lore) {
      this.id = id;
      this.tag = new Tag();
      this.tag.display = new Display();
      this.tag.display.Name = name;
      this.tag.display.Lore = lore;
    }

    String id;
    Tag tag;

    private class Tag {
      public Display display;
    }

    private class Display {
      String Name;
      List<String> Lore;
    }

  }
}
