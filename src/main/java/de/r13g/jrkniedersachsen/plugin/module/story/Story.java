package de.r13g.jrkniedersachsen.plugin.module.story;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.r13g.jrkniedersachsen.plugin.module.story.npc.StoryNpc;
import de.r13g.jrkniedersachsen.plugin.module.story.quest.QuestReward;
import de.r13g.jrkniedersachsen.plugin.module.story.quest.QuestTask;
import de.r13g.jrkniedersachsen.plugin.module.story.util.SimpleBehaviour;
import de.r13g.jrkniedersachsen.plugin.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.io.FileReader;
import java.util.*;

public class Story {

  private static final String NAME = "-Story";

  public static final Gson gson = new GsonBuilder()
          .setPrettyPrinting()
          .registerTypeAdapter(StoryNpc.class, new StoryNpc.Adapter())
          .registerTypeAdapter(SimpleBehaviour.class, new SimpleBehaviour.Adapter())
          .registerTypeAdapter(QuestReward.class, new QuestReward.Adapter())
          .registerTypeAdapter(QuestTask.class, new QuestTask.Adapter())
          .create();

  public static Map<UUID, Story> registeredStories = new HashMap<>();
  private transient Map<UUID, StoryQuest> registeredQuests = new HashMap<>();
  private transient Map<UUID, StoryNpc> registeredNpcs = new HashMap<>();

  public transient StoryProgress progress;

  public transient File storyRoot;

  private transient UUID id;
  public String name;
  public String desc;
  public boolean active;

  /* --- Utility functions--- */

  private UUID generateStoryId() {
    List<Long> e = new ArrayList<>();
    registeredStories.keySet().forEach(u -> e.add(u.getMostSignificantBits()));
    Random r = new Random();
    long msb = r.nextLong();
    while (e.contains(msb)) msb = r.nextLong();
    return new UUID(msb, 0);
  }

  private UUID generateSubStoryId(Set<UUID> existing) {
    List<Long> e = new ArrayList<>();
    existing.forEach(u -> e.add(u.getLeastSignificantBits()));
    Random r = new Random();
    long lsb = r.nextLong();
    while (e.contains(lsb)) lsb = r.nextLong();
    return new UUID(id.getMostSignificantBits(), lsb);
  }

  /* ---Story Management--- */

  /**
   * create and register new Story
   *
   * @param name Name of the story
   */
  public Story(String name) {
    this.name = name;
    register(generateStoryId(), this);
  }

  /**
   * get a registered story
   *
   * @param id id of the story
   * @return Story
   */
  public static Story get(UUID id) {
    if (registeredStories.containsKey(id))
      return registeredStories.get(id);
    return null;
  }

  /**
   * register an existing Story
   *
   * @param id    id for the story to be registered to
   * @param story Story to be registered
   * @return success
   */
  public static boolean register(UUID id, Story story) {
    if (registeredStories.containsKey(story.id)) return false;
    story.id = id;
    registeredStories.put(story.id, story);
    return true;
  }

  /**
   * unregister a registered story
   *
   * @param story story to be unregistered
   */
  public static void unregister(Story story) {
    registeredStories.remove(story.id, story);
  }

  /**
   * load all registered stories
   *
   * @return success
   */
  public static boolean loadAll() {
    final boolean[] success = {true};
    List<Story> toBeUnregistered = new ArrayList<>();
    registeredStories.forEach((k, v) -> {
              if (!v.load()) {
                success[0] = false;
                toBeUnregistered.add(v);
              }});
    if (!success[0]) {
      Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Unregistering errored stories..."));
      toBeUnregistered.forEach(Story::unload);
      toBeUnregistered.forEach(Story::unregister);
    }
    return success[0];
  }

  /**
   * unload all stories
   *
   * @return success
   */
  public static boolean unloadAll() {
    final boolean[] success = {true};
    registeredStories.forEach((k, v) -> success[0] = v.unload() && success[0]);
    return success[0];
  }

  /* ---Story Logic---*/

  /**
   * load this story
   *
   * @return success
   */
  public boolean load() {
    boolean willSurvive = true;
    if (!active) return false;
    Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Loading Story " + name + " (" + id + ")..."));

    File questDir = new File(storyRoot, "quests");
    if (!questDir.exists()) {
      Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Story has no quests folder, " +
              "asssuming there are none."));
    } else {
      Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Registering story quests..."));
      for (File questConfig : questDir.listFiles()) {
        UUID questId = UUID.fromString(questConfig.getName().replaceAll("\\.json", ""));
        try {
          StoryQuest quest = gson.fromJson(new FileReader(questConfig), StoryQuest.class);
          quest.story = this;
          if (registerQuest(questId, quest)) {
            Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Quest " + quest.name + " (id:" + questId + ") registered " +
                    "successfully, loading..."));
            if (quest.load()) {
              Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Quest " + quest.name + " (id:" + questId + ") successfully " +
                      "registered and loaded"));
            } else {
              Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Quest " + quest.name + " (id:" + questId + ") failed to load, " +
                      "disabling story...", ChatColor.YELLOW));
              return false;
            }
          } else {
            Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME,
                    "Quest " + getQuest(questId).name + " (id:" + questId + ") is already registered", ChatColor.YELLOW));
          }
        } catch (Exception e) {
          Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Unable to register story quest id:" + questId + ":", ChatColor.RED));
          e.printStackTrace();
          return false;
        }
      }
    }

    File npcDir = new File(storyRoot, "npcs");
    if (!npcDir.exists()) {
      Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Story has no NPC folder, " +
              "asssuming there are none."));
    } else {
      Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Registering Story NPCs..."));
      for (File npcConfig : npcDir.listFiles()) {
        UUID npcId = UUID.fromString(npcConfig.getName().replaceAll("\\.json", ""));
        try {
          StoryNpc npc = gson.fromJson(new FileReader(npcConfig), StoryNpc.class);
          npc.story = this;
          if (registerNpc(npcId, npc)) {
            Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "NPC " + npc.name + " (id:" + npcId + ") registered " +
                    "successfully, loading..."));
            if (npc.load()) {
              Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "NPC " + npc.name + " (id:" + npcId + ") successfully " +
                      "registered and loaded"));
            } else {
              Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "NPC " + npc.name + " (id:" + npcId + ") failed to load, " +
                      "disabling story...", ChatColor.YELLOW));
              return false;
            }
          } else {
            Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME,
                    "NPC " + getQuest(npcId).name + " (id:" + npcId + ") is already registered", ChatColor.YELLOW));
          }
        } catch (Exception e) {
          Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Unable to register Story NPC id:" + npcId + ":", ChatColor.RED));
          e.printStackTrace();
          return false;
        }
      }
    }

    Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Story " + name + " (id:" + id + ") loaded successfully, reloading player saves..."));

    if (progress == null)
      progress = new StoryProgress(this);

    Bukkit.getOnlinePlayers().forEach(p -> {
      File progressFile = new File(storyRoot.getParentFile().getParentFile(), "saves/" + p.getUniqueId().toString() + "/" + id.toString() + ".json");
      if (progress.entries.containsKey(p.getUniqueId())) {
        progress.get(p).save(progressFile);
      }
      progress.load(p, progressFile);
    });
    return true;
  }

  /**
   * unload this story
   *
   * @return success
   */
  public boolean unload() {
    Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Saving player saves..."));
    progress.unload();
    Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Unloading all NPCs..."));
    registeredNpcs.forEach((k, v) -> v.unload());
    Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Unloading all Quests..."));
    registeredQuests.forEach((k, v) -> v.unload());
    Bukkit.getConsoleSender().sendMessage(Util.logLine(NAME, "Successfully unloaded story " + name + " (id:" + id + ")"));
    return true;
  }

  public UUID getId() {
    return this.id;
  }

  /* ---Quest Management--- */
  public StoryQuest createQuest(String name) {
    StoryQuest newQuest = new StoryQuest(this, name);
    if (registerQuest(generateSubStoryId(registeredQuests.keySet()), newQuest))
      return newQuest;
    return null;
  }

  public StoryQuest getQuest(UUID questId) {
    if (registeredQuests.containsKey(questId))
      return registeredQuests.get(questId);
    return null;
  }

  public List<StoryQuest> getAllQuests(List<UUID> questIds) {
    List<StoryQuest> list = new ArrayList<>();
    questIds.forEach(questId -> list.add(getQuest(questId)));
    return list;
  }

  public List<StoryQuest> getDefaultQuests() {
    List<StoryQuest> list = new ArrayList<>();
    registeredQuests.forEach((k, v) -> {
      if (v.tasks == null) list.add(v);
    });
    return list;
  }

  public boolean registerQuest(UUID questId, StoryQuest quest) {
    if (registeredQuests == null) registeredQuests = new HashMap<>();
    if (registeredQuests.containsKey(questId)) return false;
    quest.id = questId;
    registeredQuests.put(questId, quest);
    return true;
  }

  public void unregisterQuest(StoryQuest quest) {
    registeredQuests.remove(quest.id, quest);
  }

  /* ---NPC Management--- */
  public StoryNpc createNpc(StoryNpc.Type npcType, Location loc) {
    StoryNpc newNpc = StoryNpc.create(this, npcType, loc);
    if (registerNpc(generateSubStoryId(registeredNpcs.keySet()), newNpc))
      return newNpc;
    return null;
  }

  public StoryNpc getNpc(UUID npcId) {
    if (registeredNpcs.containsKey(npcId))
      return registeredNpcs.get(npcId);
    return null;
  }

  public List<StoryNpc> getAllNpcs(List<UUID> npcIds) {
    List<StoryNpc> list = new ArrayList<>();
    npcIds.forEach(npcId -> list.add(getNpc(npcId)));
    return list;
  }

  public boolean registerNpc(UUID npcId, StoryNpc npc) {
    if (registeredNpcs == null) registeredNpcs = new HashMap<>();
    if (registeredNpcs.containsKey(npcId)) return false;
    npc.id = npcId;
    registeredNpcs.put(npcId, npc);
    return true;
  }

  public void unregisterNpc(StoryNpc npc) {
    registeredNpcs.remove(npc.id, npc);
  }
}
