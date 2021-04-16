package de.r13g.jrkniedersachsen.plugin.module.story.util;

import de.r13g.jrkniedersachsen.plugin.Plugin;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;

import java.io.File;

@SuppressWarnings("deprecation")
public class SimpleFilledMapItem extends SimpleItem {

  int mapId;

  @Override
  public boolean stack(SimpleItem other, boolean test) {
    return false;
  }

  @Override
  public ItemStack getItemStack() {
    ItemStack stack = new ItemStack(Material.FILLED_MAP);
    MapMeta meta = (MapMeta) stack.getItemMeta();
    File mapData = new File(Plugin.INSTANCE.getDataFolder().getParentFile().getParentFile(), Plugin.worldName + "/data/map_" + mapId + ".dat");
    if (!mapData.exists())
      throw new IllegalArgumentException("World folder does not contain map_" + mapId + ".dat mapData");
    meta.setMapId(mapId);
    return null;
  }
}
