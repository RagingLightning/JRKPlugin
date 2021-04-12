package de.r13g.jrkniedersachsen.plugin.util;

import net.minecraft.server.v1_16_R3.ChatMessageType;
import net.minecraft.server.v1_16_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PacketPlayOutChat;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.UUID;

public class Util {

  public static String logLine(String module, String msg) {
    ChatColor color = ChatColor.RESET;
    if (msg.startsWith("<WARN>"))
      color = ChatColor.YELLOW;
    return "[" + ChatColor.RED + "JRK" + ChatColor.RESET + "/" + ChatColor.GREEN + module + ChatColor.RESET + "] " + color + msg;
  }

  public static String logLine(String module, String msg, ChatColor color) {
    return "[" + ChatColor.RED + "JRK" + ChatColor.RESET + "/" + ChatColor.GREEN + module + ChatColor.RESET + "] " + color + msg;
  }

  public static void tellRaw(Player p, String json) {
    Packet packet = new PacketPlayOutChat(ChatSerializer.a(json), ChatMessageType.CHAT, new UUID(0,0));
    ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
  }

  public static String configToFormatString(String configValue) {
    return configValue.replaceAll("\\$(\\d)","%$1\\$s");
  }

  public static Object getPrivateField(String fieldName, Class clazz, Object object)
  {
    Field field;
    Object o = null;

    try {
      field = clazz.getDeclaredField(fieldName);

      field.setAccessible(true);

      o = field.get(object);
    } catch(NoSuchFieldException e) {
      e.printStackTrace();
    } catch(IllegalAccessException e) {
      e.printStackTrace();
    }
    return o;
  }

}
