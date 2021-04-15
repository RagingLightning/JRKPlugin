package de.r13g.jrkniedersachsen.plugin.customnpc;

import de.r13g.jrkniedersachsen.plugin.Plugin;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.minecraft.server.v1_16_R3.EnumHand;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PacketPlayInUseEntity;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CustomPlayerPacketReader {

  public static Map<UUID, Channel> channels = new HashMap<>();

  public void inject(Player player) {
    CraftPlayer p = (CraftPlayer) player;
    Channel channel = p.getHandle().playerConnection.networkManager.channel;
    channels.put(p.getUniqueId(), channel);

    if (channel.pipeline().get("PacketInjector") != null)
      return;

    channel.pipeline().addAfter("decoder", "PacketInjector", new MessageToMessageDecoder<Packet<?>>() {

      @Override
      protected void decode(ChannelHandlerContext c, Packet<?> p, List<Object> l) throws Exception {
        l.add(p);

        readPacket(player, p);

      }

    });
  }

  public static void uninject(Player player) {
    Channel channel = channels.get(player.getUniqueId());

    if (channel.pipeline().get("PacketInjector") == null)
      return;

    channel.pipeline().remove("PacketInjector");
  }

  public void readPacket(Player player, Packet<?> p) {

    if (p instanceof PacketPlayInUseEntity) {
      PacketPlayInUseEntity packet = (PacketPlayInUseEntity) p;

      if (packet.c() == EnumHand.OFF_HAND)
        return;

      int id = (int) getValue(packet, "a");

      for (CustomPlayer npc : CustomPlayer.registeredNpcs) {
        if (npc.getId() == id)
          if (getValue(packet, "action").toString().equals("ATTACK"))
            Bukkit.getScheduler().scheduleSyncDelayedTask(Plugin.INSTANCE,
                    () -> Bukkit.getPluginManager().callEvent(new EntityDamageByEntityEvent(player,
                            npc.getPather().getBukkitEntity(),
                            EntityDamageEvent.DamageCause.ENTITY_ATTACK,
                            0)),
                    0);
          else if (getValue(packet, "action").toString().equals("INTERACT_AT"))
            Bukkit.getScheduler().scheduleSyncDelayedTask(Plugin.INSTANCE,
                    () -> Bukkit.getPluginManager().callEvent(new PlayerInteractAtEntityEvent(player,
                            npc.getPather().getBukkitEntity(),
                            new Vector(packet.d().x, packet.d().y, packet.d().z))),
                    0);
          else if (getValue(packet, "action").toString().equals("INTERACT"))
            Bukkit.getScheduler().scheduleSyncDelayedTask(Plugin.INSTANCE,
                    () -> Bukkit.getPluginManager().callEvent(new PlayerInteractEntityEvent(player,
                            npc.getPather().getBukkitEntity())),
                    0);
      }
    }

}

  private Object getValue(Object instance, String name) {
    Object r = null;

    try {
      Field f = instance.getClass().getDeclaredField("name");
      f.setAccessible(true);
      r = f.get(instance);
      f.setAccessible(false);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return r;
  }
}
