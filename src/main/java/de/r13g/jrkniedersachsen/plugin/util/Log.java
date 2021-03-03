package de.r13g.jrkniedersachsen.plugin.util;

import org.bukkit.ChatColor;

public class Log {

  public static String logLine(String module, String msg) {
    return "[" + ChatColor.RED + "JRK" + ChatColor.RESET + "/" + ChatColor.GREEN + module + ChatColor.RESET + "] " + msg;
  }

  public static String logLine(String module, String msg, ChatColor color) {
    return "[" + ChatColor.RED + "JRK" + ChatColor.RESET + "/" + ChatColor.GREEN + module + ChatColor.RESET + "] " + color + msg;
  }

}
