package dev.array21.worldfinder.world;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.World;

import dev.array21.worldfinder.ReflectionUtil;
import dev.array21.worldfinder.WorldFinder;

public class WorldAnalyser {

	private WorldFinder plugin;
	
	public WorldAnalyser(WorldFinder plugin) {
		this.plugin = plugin;
	}
	
	private static Class<?> minecraftKeyClass, iRegistryClass, biomeBaseClass, vec3dClass, blockPositionClass, baseBlockPositionClass;
	private static Object customRegistryWritable;

	static {
		try {
			Class<?> minecraftServerClass = ReflectionUtil.getNmsClass("MinecraftServer");
			Class<?> iRegistryCustomClass = ReflectionUtil.getNmsClass("IRegistryCustom");

			vec3dClass = ReflectionUtil.getNmsClass("Vec3D");
			minecraftKeyClass = ReflectionUtil.getNmsClass("MinecraftKey");
			blockPositionClass = ReflectionUtil.getNmsClass("BlockPosition");
			baseBlockPositionClass = ReflectionUtil.getNmsClass("BaseBlockPosition");
			biomeBaseClass = ReflectionUtil.getNmsClass("BiomeBase");
			iRegistryClass = ReflectionUtil.getNmsClass("IRegistry");

			Object minecraftServer = ReflectionUtil.invokeMethod(minecraftServerClass, null, "getServer", null, null);
			Object customRegistry = ReflectionUtil.invokeMethod(minecraftServerClass, minecraftServer, "getCustomRegistry");
			Object ayResourceKey = ReflectionUtil.getObject(null, iRegistryClass, "ay");

			customRegistryWritable = ReflectionUtil.invokeMethod(iRegistryCustomClass, customRegistry, "b", new Object[] { ayResourceKey });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean analyseWorld(World world) {
		@SuppressWarnings("unchecked")
		List<String> requiredBiomes = (List<String>) this.plugin.getConfigHandler().getValue("requiredBiomes");

		HashMap<String, Boolean> biomesPassing = new HashMap<>();
		requiredBiomes.forEach(biome -> {
			biomesPassing.put(biome, false);
		});

		int searchDistance = (int) this.plugin.getConfigHandler().getValue("searchDistance");
		double centerPointX = Double.valueOf((int) this.plugin.getConfigHandler().getValue("centerPointX"));
		double centerPointZ = Double.valueOf((int) this.plugin.getConfigHandler().getValue("centerPointZ"));
		boolean searchCircular = (boolean) this.plugin.getConfigHandler().getValue("searchCircular");
		
		WorldFinder.logDebug("Beginning analyses for World: " + world.getName());
		
		try {
			Object worldServer = ReflectionUtil.invokeMethod(world, "getHandle");
			// Construct a Vec3d from the centerPointX, 0 and the centerPointZ
			Object centerPointVec3D = ReflectionUtil.invokeConstructor(vec3dClass, new Class<?>[] { double.class, double.class, double.class }, new Object[] { centerPointX, 0, centerPointZ });
			Object centerPointBlockPosition = ReflectionUtil.invokeConstructor(blockPositionClass, centerPointVec3D);

			for (String biome : requiredBiomes) {
				// Get the MinecraftKey for the Biome we're looking for
				Object minecraftKey = ReflectionUtil.invokeConstructor(minecraftKeyClass, biome);

				// Get the Option<BiomeBase> from IRegistryWritable for our MinecraftKey
				Object optional = ReflectionUtil.invokeMethod(iRegistryClass, customRegistryWritable, "getOptional", new Object[] { minecraftKey });

				// 'Unwrap' the optional
				Object biomeBaseGeneric = ReflectionUtil.invokeMethod(optional, "get");

				// Cast the object to BiomeBase
				// Object biomeBase = biomeBaseClass.cast(biomeBaseGeneric);

				// Get the BlockPosition for the Biome
				Object biomeBlockPosition = ReflectionUtil.invokeMethod(worldServer, "a", new Class<?>[] { biomeBaseClass, blockPositionClass, int.class, int.class }, new Object[] { /* biomeBase */biomeBaseGeneric, centerPointBlockPosition, 6400, 8 });

				// Get the X and Z of the Block Position
				int biomePosX = (int) ReflectionUtil.invokeMethod(baseBlockPositionClass, biomeBlockPosition, "getX");
				int biomePosZ = (int) ReflectionUtil.invokeMethod(baseBlockPositionClass, biomeBlockPosition, "getZ");

				if(searchCircular) {
					double distance = Math.sqrt(Math.pow((biomePosX - centerPointX), 2) + Math.pow((biomePosZ - centerPointZ), 2));
					if(distance > searchDistance) {
						WorldFinder.logDebug(String.format("Biome %s did not pass distance check (X: %d, Z: %d, distance: %d)", biome, biomePosX, biomePosZ, (int) distance));
						continue;
					} else {
						WorldFinder.logDebug(String.format("Biome %s passwed distance check (X: %d, Z: %d, distance: %d)", biome, biomePosX, biomePosZ, (int) distance));
					}
				} else {
					if (biomePosX > searchDistance || biomePosX < -searchDistance) {
						WorldFinder.logDebug(String.format("Biome %s did not pass X check (at X: %d", biome, biomePosX));
						continue;
					} else {
						WorldFinder.logDebug(String.format("Biome %s passed X check (at X: %d)", biome, biomePosX));
					}

					if (biomePosZ > searchDistance || biomePosZ < -searchDistance) {
						WorldFinder.logDebug(String.format("Biome %s did not pass Z check (at Z: %d", biome, biomePosX));
						continue;
					} else {
						WorldFinder.logDebug(String.format("Biome %s passed Z check (at Z: %d)", biome, biomePosZ));
					}

					biomesPassing.put(biome, true);
				}
			}
		} catch (Exception e) {
			WorldFinder.logDebug(ExceptionUtils.getStackTrace(e));
			WorldFinder.logWarn("An error occurred: " + e.getMessage());
			return false;
		}
		
		WorldFinder.logDebug(String.format("World analysis of world '%s' finished", world.getName()));
		boolean allPassed = true;
		for (Map.Entry<String, Boolean> entry : biomesPassing.entrySet()) {
			if (!entry.getValue()) {
				allPassed = false;
				WorldFinder.logDebug("Failed biome check for: " + entry.getKey());
			}
		}

		if (allPassed) {
			WorldFinder.logDebug(String.format("All biome checks passed for world '%s'", world.getName()));
		}
		
		return allPassed;
	}
}
