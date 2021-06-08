package nl.thedutchmc.worldfinder;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigurationHandler {

    private HashMap<String, Object> configValues = new HashMap<>();

    private File file;
    private FileConfiguration config;

    private WorldFinder plugin;
    
    public ConfigurationHandler(WorldFinder plugin) {
		this.plugin = plugin;
    }
    
    public FileConfiguration getConfig() {
		return config;
    }

    public void loadConfig() {
		file = new File(plugin.getDataFolder(), "config.yml");
	
		if (!file.exists()) {
		    file.getParentFile().mkdirs();
		    plugin.saveResource("config.yml", false);
		}
	
		config = new YamlConfiguration();
	
		try {
		    config.load(file);
		    readConfig();
		} catch (InvalidConfigurationException e) {
		    WorldFinder.logWarn("Invalid config.yml!");
		} catch (IOException e) {
		    e.printStackTrace();
		}
    }

    public void readConfig() {
		for (String key : this.config.getKeys(true)) {
		    Object value = this.config.get(key);
		    this.configValues.put(key, value);
		}
    }

    public Object getValue(String key) {
    	return this.configValues.get(key);
    }
}
