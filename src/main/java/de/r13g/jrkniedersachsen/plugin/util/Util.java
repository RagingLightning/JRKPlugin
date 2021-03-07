package de.r13g.jrkniedersachsen.plugin.util;

import org.bukkit.ChatColor;

public class Util {

  public static String logLine(String module, String msg) {
    return "[" + ChatColor.RED + "JRK" + ChatColor.RESET + "/" + ChatColor.GREEN + module + ChatColor.RESET + "] " + msg;
  }

  public static String logLine(String module, String msg, ChatColor color) {
    return "[" + ChatColor.RED + "JRK" + ChatColor.RESET + "/" + ChatColor.GREEN + module + ChatColor.RESET + "] " + color + msg;
  }

  public static String configToFormatString(String configValue) {
    return configValue.replaceAll("\\$(\\d)","%$1\\$s");
  }

}
