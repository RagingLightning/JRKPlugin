package de.r13g.jrkniedersachsen.plugin.customnpc.event;

import de.r13g.jrkniedersachsen.plugin.customnpc.CustomPlayer;
import de.r13g.jrkniedersachsen.plugin.customnpc.CustomPlayerPacketReader;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

public class PlayerJoinQuitListener implements Listener {

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent ev) {
    if (CustomPlayer.registeredNpcs == null) return;
    if (CustomPlayer.registeredNpcs.size() == 0) return;
    CustomPlayer.addJoinPacket(ev.getPlayer());

    CustomPlayerPacketReader r = new CustomPlayerPacketReader();
    r.inject(ev.getPlayer());

  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent ev) {
    CustomPlayerPacketReader.uninject(ev.getPlayer());
  }

}
