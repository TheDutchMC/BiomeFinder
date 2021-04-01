package nl.thedutchmc.worldfinder.world;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import nl.thedutchmc.worldfinder.WorldFinder;

public class WorldFinderLogic {

	private final int maxRecursions;
	private final int generationDelay;

	private final Random random = new Random();
	
	private WorldFinder plugin;
	private Listener playerJoinEventListener;
	
	private int recursionCount = 0;
	
	public WorldFinderLogic(WorldFinder plugin, Listener playerJoinEventListener) {
		this.plugin = plugin;
		this.playerJoinEventListener = playerJoinEventListener;
		
		this.generationDelay = (int) plugin.getConfigHandler().getValue("generationDelay");
		this.maxRecursions = (int) plugin.getConfigHandler().getValue("maxRecursions");
	}
	
	public void start() {
		WorldFinder.logInfo("Starting World Finding process. This may take a while. No players will be allowed to be online during this process!");
		
		findWorldRecursive();
	}
	
	private void finishedWorldAnalys(boolean success) {
		if(success) {
			WorldFinder.logInfo(String.format("World found! The seed is %d. This took %d tries", Bukkit.getWorld("world").getSeed(), recursionCount-1));
		} else {
			WorldFinder.logInfo(String.format("Unable to find suitable world after %d tries", recursionCount-1));
		}
		
		HandlerList.unregisterAll(this.playerJoinEventListener);
	}
	
	private void findWorldRecursive() {
		WorldAnalyser analyser = new WorldAnalyser(this.plugin);
		
		WorldFinder.logDebug(String.format("Starting recursive world finding operation (#%d out of max %d)", recursionCount, maxRecursions));
		recursionCount++;
		
		if(recursionCount > maxRecursions) {
			WorldFinder.logDebug("Maximum amount of recursions exceeded!");
			
			finishedWorldAnalys(false);
			return;
		}

		if(Bukkit.getWorld("world_finder") != null) {
			WorldFinder.logDebug("World 'world_finder' exists. Deleting world.");
			boolean unloadSuccessful = Bukkit.unloadWorld(Bukkit.getWorld("world_finder"), false);
			if(!unloadSuccessful) {
				WorldFinder.logWarn("Unable to unload World 'world_finder'!");
				finishedWorldAnalys(false);
				return;
			}
			
			File serverFolder = Bukkit.getServer().getWorldContainer();
			WorldFinder.logDebug("Server folder: " + serverFolder.getAbsolutePath());
			
			File worldFolder = new File(serverFolder.getAbsolutePath().replaceAll(Pattern.quote("."), "") + File.separator + "world_finder");
			WorldFinder.logDebug("Deleting folder: " + worldFolder.getAbsolutePath());	
			
			try {
				FileUtils.deleteDirectory(worldFolder);
			} catch (IOException e) {
				WorldFinder.logDebug(ExceptionUtils.getStackTrace(e));
				WorldFinder.logWarn("Unable to delete World folder!");
				finishedWorldAnalys(false);
				return;
			}
		}
		
		WorldCreator worldCreator = new WorldCreator("world_finder");
		worldCreator.seed(this.random.nextLong());
		worldCreator.environment(Environment.NORMAL);
		
		World world = Bukkit.createWorld(worldCreator);
		WorldFinder.logDebug("Using seed: " + world.getSeed());
		
		boolean worldPassed = analyser.analyseWorld(world);
		
		if(worldPassed) {
			finishedWorldAnalys(true);
			return;
		} else {
			WorldFinder.logDebug(String.format("World did not pass all checks. Continueing in %d seconds.", this.generationDelay));

			new BukkitRunnable() {

				@Override
				public void run() {
					findWorldRecursive();
				}
			}.runTaskLater(this.plugin, 20 * this.generationDelay);			
		}
	}
}
