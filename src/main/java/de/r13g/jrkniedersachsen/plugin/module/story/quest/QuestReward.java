package de.r13g.jrkniedersachsen.plugin.module.story.quest;

import com.google.gson.*;
import de.r13g.jrkniedersachsen.plugin.module.story.quest.reward.CommandReward;
import de.r13g.jrkniedersachsen.plugin.module.story.quest.reward.ItemReward;
import org.bukkit.entity.Player;

public abstract class QuestReward {

  public Type type;

  public abstract boolean reward(Player p);

  public enum Type {
    NONE,
    DIALOGUE,
    ITEM,
    EXPERIENCE,
    COMMAND
  }

  public static class Adapter implements JsonDeserializer<QuestReward> {

    @Override
    public QuestReward deserialize(JsonElement e, java.lang.reflect.Type _t, JsonDeserializationContext c) throws JsonParseException {
      JsonObject o = e.getAsJsonObject();
      String t = o.get("type").getAsString();
      switch (Type.valueOf(t)) {
        case COMMAND: return c.deserialize(e, CommandReward.class);
        case ITEM: return c.deserialize(e, ItemReward.class);
      }
      throw new JsonParseException("QuestReward has unknown type '" + t + "'");
    }
  }

}
