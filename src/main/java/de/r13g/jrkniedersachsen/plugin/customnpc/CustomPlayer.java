package de.r13g.jrkniedersachsen.plugin.customnpc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import de.r13g.jrkniedersachsen.plugin.Plugin;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CustomPlayer extends EntityPlayer {

  public static List<CustomPlayer> registeredNpcs = new ArrayList<>();

  private static int tick = 0;

  static {
    Bukkit.getScheduler().runTaskTimer(Plugin.INSTANCE, () -> {
      registeredNpcs.forEach(CustomPlayer::update);
      tick++;
    }, 0, 0);
  }

  private CustomPlayerPather pather;

  private float pH = 0;
  private double pX, pY, pZ;

  private CustomPlayer(MinecraftServer s, WorldServer w, GameProfile p, PlayerInteractManager m) {
    super(s, w, p, m);
  }

  public static CustomPlayerPather create(Location location, String customName, UUID skin) {
    if (skin != null) {
      String[] skinData = getSkin(skin);
      if (skinData != null)
        return create(location, customName, skinData[0], skinData[1]);
    }
    return create(location, customName, null, null);
  }

  public static CustomPlayerPather create(Location location, String customName, String skinTex, String skinSig) {
    if (customName.length() > 16) throw new IllegalArgumentException("Npc customName can't be longer than 16 characters");
    MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
    WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
    GameProfile gameProfile = new GameProfile(UUID.randomUUID(), customName);
    PlayerInteractManager interactManager = new PlayerInteractManager(world);

    CustomPlayer npc = new CustomPlayer(server, world, gameProfile, interactManager);

    if (skinTex != null)
      if (skinSig != null)
        gameProfile.getProperties().put("textures", new Property("textures", skinTex, skinSig));
      else
        gameProfile.getProperties().put("textures", new Property("textures", skinTex));


    npc.setLocation(location.getX(), location.getY(), location.getZ(), 0, 0);
    npc.pather = new CustomPlayerPather(npc);

    npc.pX = location.getX();
    npc.pY = location.getY();
    npc.pZ = location.getZ();

    addNpcPacket(npc);

    registeredNpcs.add(npc);

    return npc.pather;
  }

  public void update() {
    if (tick % 1200 == 0)
      ((CraftLivingEntity) pather.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1500, 0, false, false, false));
    for (Player p : Bukkit.getOnlinePlayers()) {
      PlayerConnection c = ((CraftPlayer) p).getHandle().playerConnection;

      //Head Roataion
      c.sendPacket(new PacketPlayOutEntityHeadRotation(this, (byte) (pather.aC * 256 / 360)));
      c.sendPacket(new PacketPlayOutEntity.PacketPlayOutEntityLook(getId(), (byte) (pather.aC * 256 / 360),
              (byte) (pather.pitch * 256 / 360), true));
    }
    if (pather.lastX != this.pX || pather.lastY != this.pY || pather.lastZ != this.pZ) {
      double dx = pather.lastX - this.pX;
      double dy = pather.lastY - this.pY;
      double dz = pather.lastZ - this.pZ;

      move(dx, dy, dz);

      pX = pather.lastX;
      pY = pather.lastY;
      pZ = pather.lastZ;
    }

    if (getHealth() < this.pH) {
      npcTakeDamage(this);
    }
    this.pH = getHealth();

    if (dead) {
      destroy();
    }
  }


  public void move(double x, double y, double z) {
    for (Player p : Bukkit.getOnlinePlayers()) {
      PlayerConnection c = ((CraftPlayer) p).getHandle().playerConnection;

      //Movement
      c.sendPacket(new PacketPlayOutEntity.PacketPlayOutRelEntityMove(getId(), (short) (x * 4096), (short) (y * 4096),
              (short) (z * 4096), true));
    }
  }

  public void destroy() {
    removeNpcPacket(this);
    pather.killEntity();
    registeredNpcs.remove(this);
  }

  public CustomPlayerPather getPather() {
    return pather;
  }

  private static String[] getSkin(UUID skinPlayer) {
    String skin = skinPlayer.toString().replaceAll("-", "");
    try {
      URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + skin);
      JsonObject o = JsonParser.parseReader(new InputStreamReader(url.openStream())).getAsJsonObject();
      JsonObject p = o.get("properties").getAsJsonArray().get(0).getAsJsonObject();
      String tex = p.get("value").getAsString();
      String sig = p.get("signature").getAsString();
      return new String[]{tex, sig};
    } catch (Exception e) {
      return null;
    }
  }

  public static void addNpcPacket(CustomPlayer npc) {
    for (Player p : Bukkit.getOnlinePlayers()) {
      PlayerConnection c = ((CraftPlayer) p).getHandle().playerConnection;
      c.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, npc));
      c.sendPacket(new PacketPlayOutNamedEntitySpawn(npc));
      c.sendPacket(new PacketPlayOutEntityHeadRotation(npc, (byte) (npc.yaw * 256 / 360)));
    }
  }

  public static void addJoinPacket(Player p) {
    for (CustomPlayer npc : registeredNpcs) {

      npc.setLocation(npc.pather.lastX, npc.pather.lastY, npc.pather.lastZ, 0, 0);

      PlayerConnection c = ((CraftPlayer) p).getHandle().playerConnection;
      c.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, npc));
      c.sendPacket(new PacketPlayOutNamedEntitySpawn(npc));
      c.sendPacket(new PacketPlayOutEntityHeadRotation(npc, (byte) (npc.yaw * 256 / 360)));
    }
  }

  public static void removeNpcPacket(CustomPlayer npc) {
    for (Player p : Bukkit.getOnlinePlayers()) {
      PlayerConnection c = ((CraftPlayer) p).getHandle().playerConnection;
      c.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, npc));
      c.sendPacket(new PacketPlayOutEntityDestroy(npc.getId()));
    }
  }

  public static void npcTakeDamage(CustomPlayer npc) {
    for (Player p : Bukkit.getOnlinePlayers()) {
      PlayerConnection c = ((CraftPlayer) p).getHandle().playerConnection;
      c.sendPacket(new PacketPlayOutAnimation(npc, 1));
    }
  }

}
