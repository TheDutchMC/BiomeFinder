package nl.thedutchmc.worldfinder.listeners;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinEventListener implements Listener {

	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		event.getPlayer().kickPlayer(ChatColor.RED + "You are not allowed to be online during the World Finding process!");
	}
}
