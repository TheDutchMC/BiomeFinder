package dev.array21.worldfinder;

import org.bukkit.plugin.java.JavaPlugin;

import dev.array21.worldfinder.commands.FindWorldCommandExecutor;
import dev.array21.worldfinder.commands.WorldFinderTpExecutor;

public class WorldFinder extends JavaPlugin {

	public static volatile boolean IS_DEBUG = false;

	private static WorldFinder INSTANCE;
	private ConfigurationHandler configHandler;

	@Override
	public void onEnable() {
		INSTANCE = this;

		this.configHandler = new ConfigurationHandler(this);
		this.configHandler.loadConfig();

		if ((boolean) this.configHandler.getValue("isDebug")) {
			IS_DEBUG = true;
		}

		this.getCommand("findworld").setExecutor(new FindWorldCommandExecutor(this));
		this.getCommand("worldfindertp").setExecutor(new WorldFinderTpExecutor());
	}

	@Override
	public void onDisable() {

	}

	public ConfigurationHandler getConfigHandler() {
		return this.configHandler;
	}

	public static void logInfo(String log) {
		INSTANCE.getLogger().info(log);
	}

	public static void logWarn(String log) {
		INSTANCE.getLogger().warning(log);
	}
	
	public static void logDebug(String log) {
		if(IS_DEBUG) {
			INSTANCE.getLogger().info("[DEBUG] " + log);
		}
	}
}
