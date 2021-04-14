package de.r13g.jrkniedersachsen.plugin.module.story.util;

import com.google.gson.*;
import de.r13g.jrkniedersachsen.plugin.module.story.npc.behaviour.SimpleWanderBehaviour;
import net.minecraft.server.v1_16_R3.EntityCreature;

import java.lang.reflect.Type;

public abstract class SimpleBehaviour {

  public static final int maxNeededGoalSlots = 2;

  public Type type;

  public abstract void applyPathfinderGoals(EntityCreature entity, int startIndex);

  public enum Type {
    STAY,
    WANDER,
    PATH_LOOP,
    PATH_BACK
  }

  public static class Adapter implements JsonDeserializer<SimpleBehaviour> {

    @Override
    public SimpleBehaviour deserialize(JsonElement e, java.lang.reflect.Type _T, JsonDeserializationContext c) throws JsonParseException {
      JsonObject o = e.getAsJsonObject();
      String t = o.get("type").getAsString();
      switch (Type.valueOf(t)) {
        case WANDER:
          return c.deserialize(e, SimpleWanderBehaviour.class);
      }
      throw new JsonParseException("SimpleBehaviour has unknown type '" + t + "'");
    }
  }

}
