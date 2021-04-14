package de.r13g.jrkniedersachsen.plugin.module.story.quest;

import com.google.gson.*;
import de.r13g.jrkniedersachsen.plugin.module.story.quest.task.ExternalTask;

import java.lang.reflect.Type;

public abstract class QuestTask {

  public Type type;

  public enum Type {
    EXTERNAL,
  }

  public static class Adapter implements JsonDeserializer<QuestTask> {

    @Override
    public QuestTask deserialize(JsonElement e, java.lang.reflect.Type _t, JsonDeserializationContext c) throws JsonParseException {
      JsonObject o = e.getAsJsonObject();
      String t = o.get("type").getAsString();
      switch (Type.valueOf(t)) {
        case EXTERNAL: return c.deserialize(e, ExternalTask.class);
      }
      throw new JsonParseException("QuestTask has unknown type '" + t + "'");
    }
  }

}
