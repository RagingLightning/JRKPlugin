package de.r13g.jrkniedersachsen.plugin.module.story.quest;

import com.google.gson.*;
import de.r13g.jrkniedersachsen.plugin.module.story.StoryQuest;
import de.r13g.jrkniedersachsen.plugin.module.story.quest.reward.*;
import org.bukkit.entity.Player;

public abstract class QuestReward {

  public transient StoryQuest quest;

  public Type type;

  public abstract boolean reward(Player p);

  public enum Type {
    DIALOGUE,
    ITEM,
    EXPERIENCE_POINTS,
    EXPERIENCE_LEVELS,
    EXPERIENCE_TO_LEVEL,
    COMMAND
  }

  public static class Adapter implements JsonDeserializer<QuestReward> {

    @Override
    public QuestReward deserialize(JsonElement e, java.lang.reflect.Type _t, JsonDeserializationContext c) throws JsonParseException {
      JsonObject o = e.getAsJsonObject();
      String t = o.get("type").getAsString();
      switch (Type.valueOf(t)) {
        case DIALOGUE: return c.deserialize(e, DialogueReward.class);
        case ITEM: return c.deserialize(e, ItemReward.class);
        case EXPERIENCE_POINTS: return c.deserialize(e, ExperiencePointReward.class);
        case EXPERIENCE_LEVELS: return c.deserialize(e, ExperienceLevelReward.class);
        case EXPERIENCE_TO_LEVEL: return c.deserialize(e, ExperienceToLevelReward.class);
        case COMMAND: return c.deserialize(e, CommandReward.class);
      }
      throw new JsonParseException("QuestReward has unknown type '" + t + "'");
    }
  }

}
