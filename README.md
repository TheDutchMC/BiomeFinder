# WorldFinder
Spigot 1.16+

Find a World that matches your biome needs

**Requires Java 11+, I won't backport to Java 8**

## Usage:
For players, this will only check the current world: ``/findworld``

From console: ``/findworld true``, this will keep generating a new world until a good one is found  

When run from console the plugin will generate a world called `world_finder`, you can TP to this world with `/worldfindertp`

## Permissions
Maybe someday Ill implement them, not yet for now.

## Default config
```yaml
requiredBiomes:
- "desert"
- "plains"
- "birch_forest"

# The amount of time between each world generation
generationDelay: 15

# The maximum amount of recursions (tries) before we give up
maxRecursions: 500

# Should we search circular, or square
# Square will check if the absolute value of the coordinates falls within the searchDistance
# Circular will calculate the distance between the centerPointX and the X and Z coordinate
searchCircular: true
searchDistance: 3000

# The center point to use for calculating distances
# This is ignored when searchCircular is false!
centerPointX: 0
centerPointZ: 0

# Should we print debug output
isDebug: false
```
