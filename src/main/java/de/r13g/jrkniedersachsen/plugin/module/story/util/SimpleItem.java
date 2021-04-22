package de.r13g.jrkniedersachsen.plugin.module.story.util;

import com.google.gson.*;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;
import java.util.List;

public abstract class SimpleItem {
  public String mcMaterial, material;
  public int count;
  public String displayName;
  public List<String> lore;

  protected SimpleItem(ItemStack stack) {
    material = stack.getType().toString();
    count = stack.getAmount();
    if (!"".equals(stack.getItemMeta().getDisplayName()))
      displayName = stack.getItemMeta().getDisplayName();
    if (stack.getItemMeta().getLore() != null && stack.getItemMeta().getLore().size() > 0)
      lore = stack.getItemMeta().getLore();
  }

  public boolean stack(SimpleItem other, boolean test) {
    if (!this.material.equalsIgnoreCase(other.material))
      return false;
    if (other.displayName == null && this.displayName != null)
      return false;
    if (other.displayName != null && this.displayName == null)
      return false;
    if (this.displayName != null && !this.displayName.equalsIgnoreCase(other.displayName))
      return false;
    if (other.lore == null && this.lore != null)
      return false;
    if (other.lore != null && this.lore == null)
      return false;
    if (this.lore == null) return true;
    if (this.lore.size() != other.lore.size()) return false;
    for (int i = 0; i < this.lore.size(); i++) {
      if (!this.lore.get(i).equals(other.lore.get(i))) return false;
    }
    return true;
  }

  public abstract ItemStack getItemStack();

  public abstract String getItemJson();

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
      return Integer.MIN_VALUE;
    return other.count - this.count;
  }

  public static SimpleItem fromItemStack(ItemStack stack) {
    if (stack == null) return null;
    switch (stack.getType()) {
      case PLAYER_HEAD:
        return new SimpleHeadItem(stack);
      case FILLED_MAP:
        return new SimpleFilledMapItem(stack);
      case POTION:
      case SPLASH_POTION:
      case LINGERING_POTION:
        return new SimplePotionItem(stack);
      default:
        return new SimpleDefaultItem(stack);
    }
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
          return c.deserialize(e, SimpleHeadItem.class);
        case FILLED_MAP:
          return c.deserialize(e, SimpleFilledMapItem.class);
        case POTION:
        case SPLASH_POTION:
        case LINGERING_POTION:
          return c.deserialize(e, SimplePotionItem.class);
        default:
          return c.deserialize(e, SimpleDefaultItem.class);
      }
    }
  }
}