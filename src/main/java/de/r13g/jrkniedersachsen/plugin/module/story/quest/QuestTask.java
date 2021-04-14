package de.r13g.jrkniedersachsen.plugin.module.story.quest;

import com.google.gson.*;
import de.r13g.jrkniedersachsen.plugin.module.story.StoryQuest;
import de.r13g.jrkniedersachsen.plugin.module.story.quest.task.*;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.Type;

public abstract class QuestTask {

  public transient StoryQuest quest;
  public transient int id;

  public Type type;

  public abstract void notifyPlayer(Player p);

  public enum Type {
    EXTERNAL,
    DETECT_ITEM,
    COLLECT_ITEM,
    CRAFT_ITEM,
    LOCATION,
    BREAK_BLOCK,
    PLACE_BLOCK,
    TAME,
    KILL,
    DIE
  }

  public static class Adapter implements JsonDeserializer<QuestTask> {

    @Override
    public QuestTask deserialize(JsonElement e, java.lang.reflect.Type _t, JsonDeserializationContext c) throws JsonParseException {
      JsonObject o = e.getAsJsonObject();
      String t = o.get("type").getAsString();
      switch (Type.valueOf(t)) {
        case EXTERNAL: return c.deserialize(e, ExternalTask.class);
        case DETECT_ITEM: return c.deserialize(e, DetectItemTask.class);
        case COLLECT_ITEM: return c.deserialize(e, CollectItemTask.class);
        case CRAFT_ITEM: return c.deserialize(e, CraftItemTask.class);
        case LOCATION: return c.deserialize(e, LocationTask.class);
      }
      throw new JsonParseException("QuestTask has unknown type '" + t + "'");
    }
  }

}
