package de.r13g.jrkniedersachsen.plugin.module.story.util;

import com.google.gson.*;
import com.mojang.serialization.Decoder;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Type;
import java.util.List;

public abstract class SimpleItem {
  public String material;
  public int count;
  public String displayName;
  public List<String> lore;

  public abstract boolean stack(SimpleItem other, boolean test);

  public abstract ItemStack getItemStack();

  /**
   * calculates the size difference of this SimpleItem to the other SimpleItem
   * positive values indicate that other is bigger than this
   * negative values indicate that other is smaller than this
   *
   * @param other SimpleItem to be compared against
   * @return
   */
  public int stackDifference(SimpleItem other) {
    if (!this.stack(other, true))
      throw new UnsupportedOperationException("SimpleItems are not of the same type");
    return other.count - this.count;
  }

  public static SimpleItem fromItemStack(ItemStack stack) {
    if (stack == null) return null;
    if (stack.getType() == Material.PLAYER_HEAD)
      return new SimpleHead(stack);
    return new SimpleNonHead(stack);
  }

  @Override
  public String toString() {
    return material + "/" + displayName + "x" + count;
  }

  public static class Adapter implements JsonDeserializer<SimpleItem> {

    @Override
    public SimpleItem deserialize(JsonElement e, Type _t, JsonDeserializationContext c) throws JsonParseException {
      JsonObject o = e.getAsJsonObject();
      String m = o.get("material").getAsString();
      switch (Material.valueOf(m)) {
        case PLAYER_HEAD:
          return c.deserialize(e, SimpleHead.class);
        default:
          return c.deserialize(e, SimpleNonHead.class);
      }
    }
  }
}