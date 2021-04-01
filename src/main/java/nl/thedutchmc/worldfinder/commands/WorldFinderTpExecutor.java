package nl.thedutchmc.worldfinder.commands;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class WorldFinderTpExecutor implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage("This command can only be used by in-game Players!");
			return true;
		}
		
		World finderWorld = Bukkit.getWorld("world_finder");
		if(finderWorld == null) {
			sender.sendMessage(ChatColor.GOLD + "World " + ChatColor.RED + "'world_finder'" + ChatColor.GOLD + "does not exist. Please run " + ChatColor.RED + "'/findbiome true'" + ChatColor.GOLD + "first.");
			return true;
		}
		
		sender.sendMessage(ChatColor.GOLD + "Teleporting...");
		((Player) sender).teleport(finderWorld.getSpawnLocation());
		
		return true;
	}

}
