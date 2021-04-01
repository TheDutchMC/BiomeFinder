package nl.thedutchmc.worldfinder.commands;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import nl.thedutchmc.worldfinder.WorldFinder;
import nl.thedutchmc.worldfinder.listeners.PlayerJoinEventListener;
import nl.thedutchmc.worldfinder.world.WorldAnalyser;
import nl.thedutchmc.worldfinder.world.WorldFinderLogic;

public class FindWorldCommandExecutor implements CommandExecutor {

	private WorldFinder plugin;

	public FindWorldCommandExecutor(WorldFinder plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args.length > 0) {
			if(args[0].equals("true")) {
				if(sender instanceof Player) {
					sender.sendMessage(ChatColor.RED + "This command can only be run from console when used like this. Use '/findworld' to use it in-game!");
					return true;
				}
				
				Bukkit.getOnlinePlayers().forEach(player -> {
					player.kickPlayer(ChatColor.RED + "World Finding operation starting.");
				});
				
				PlayerJoinEventListener joinEventListener = new PlayerJoinEventListener();
				Bukkit.getPluginManager().registerEvents(joinEventListener, this.plugin);
				
				new WorldFinderLogic(this.plugin, joinEventListener).start();
				
				return true;
			}
		}
		
		if(!(sender instanceof Player)) {
			sender.sendMessage("Only an in-game Player can use this command like this. Use '/findworld true' to use it from Console!");
			return true;
		}

		sender.sendMessage(ChatColor.GOLD + "Checking the world...");
		
		World w = ((Player) sender).getWorld();
		
		WorldAnalyser analyser = new WorldAnalyser(this.plugin);
		boolean worldPassed = analyser.analyseWorld(w);
		
		sender.sendMessage(ChatColor.GOLD + "This world " + ChatColor.RED + ((worldPassed) ? "passed" : "did not pass") + ChatColor.GOLD + " the biome check");
		return true;
	}
}
