package world.bentobox.oneblock;

import java.io.IOException;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.generator.ChunkGenerator;
import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.configuration.Config;
import world.bentobox.bentobox.api.configuration.WorldSettings;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.oneblock.commands.AdminCommand;
import world.bentobox.oneblock.commands.IslandCommand;
import world.bentobox.oneblock.dataobjects.OneBlockIslands;
import world.bentobox.oneblock.generators.ChunkGeneratorWorld;
import world.bentobox.oneblock.listeners.BlockListener;
import world.bentobox.oneblock.listeners.OneBlocksManager;

/**
 * Main OneBlock class - provides an island minigame in the sky
 * @author tastybento
 */
public class OneBlock extends GameModeAddon {

    private static final String NETHER = "_nether";
    private static final String THE_END = "_the_end";

    // Settings
    private Settings settings;
    private ChunkGeneratorWorld chunkGenerator;
    private final Config<Settings> configObject = new Config<>(this, Settings.class);
    private BlockListener listener;

    @Override
    public void onLoad() {
        // Save the default config from config.yml
        saveDefaultConfig();

        // Load settings from config.yml. This will check if there are any issues with it too.
        loadSettings();
        // Chunk generator
        chunkGenerator = settings.isUseOwnGenerator() ? null : new ChunkGeneratorWorld(this);
        // Register commands
        playerCommand = new IslandCommand(this);
        adminCommand = new AdminCommand(this);
    }

    private boolean loadSettings() {
        // Load settings again to get worlds
        settings = configObject.loadConfigObject();
        if (settings == null) {
            // Disable
            logError("OneBlock settings could not load! Addon disabled.");
            setState(State.DISABLED);
            return false;
        }
        // Load the oneblocks

        return true;
    }

    @Override
    public void onEnable(){
        try {
            listener = new BlockListener(this);
            registerListener(listener);
        } catch (IOException | InvalidConfigurationException e) {
            // Disable
            logError("OneBlock settings could not load (oneblock.yml error)! Addon disabled.");
            logError(e.getMessage());
            e.printStackTrace();
            setState(State.DISABLED);
        }

    }

    @Override
    public void onDisable() {
        // save cache
        listener.saveCache();
    }

    @Override
    public void onReload() {
        if (loadSettings()) {
            log("Reloaded OneBlock settings");
        }
    }

    /**
     * @return the settings
     */
    public Settings getSettings() {
        return settings;
    }

    @Override
    public void createWorlds() {
        String worldName = settings.getWorldName().toLowerCase();
        if (getServer().getWorld(worldName) == null) {
            log("Creating OneBlock world ...");
        }

        // Create the world if it does not exist
        islandWorld = getWorld(worldName, World.Environment.NORMAL, chunkGenerator);
        // Make the nether if it does not exist
        if (settings.isNetherGenerate()) {
            if (getServer().getWorld(worldName + NETHER) == null) {
                log("Creating OneBlock's Nether...");
            }
            netherWorld = settings.isNetherIslands() ? getWorld(worldName, World.Environment.NETHER, chunkGenerator) : getWorld(worldName, World.Environment.NETHER, null);
        }
        // Make the end if it does not exist
        if (settings.isEndGenerate()) {
            if (getServer().getWorld(worldName + THE_END) == null) {
                log("Creating OneBlock's End World...");
            }
            endWorld = settings.isEndIslands() ? getWorld(worldName, World.Environment.THE_END, chunkGenerator) : getWorld(worldName, World.Environment.THE_END, null);
        }
    }

    /**
     * Gets a world or generates a new world if it does not exist
     * @param worldName2 - the overworld name
     * @param env - the environment
     * @param chunkGenerator2 - the chunk generator. If <tt>null</tt> then the generator will not be specified
     * @return world loaded or generated
     */
    private World getWorld(String worldName2, Environment env, ChunkGeneratorWorld chunkGenerator2) {
        // Set world name
        worldName2 = env.equals(World.Environment.NETHER) ? worldName2 + NETHER : worldName2;
        worldName2 = env.equals(World.Environment.THE_END) ? worldName2 + THE_END : worldName2;
        WorldCreator wc = WorldCreator.name(worldName2).type(WorldType.FLAT).environment(env);
        World w = settings.isUseOwnGenerator() ? wc.createWorld() : wc.generator(chunkGenerator2).createWorld();
        // Set spawn rates
        if (w != null) {
            w.setMonsterSpawnLimit(getSettings().getSpawnLimitMonsters());
            w.setAmbientSpawnLimit(getSettings().getSpawnLimitAmbient());
            w.setAnimalSpawnLimit(getSettings().getSpawnLimitAnimals());
            w.setWaterAnimalSpawnLimit(getSettings().getSpawnLimitWaterAnimals());
            w.setTicksPerAnimalSpawns(getSettings().getTicksPerAnimalSpawns());
            w.setTicksPerMonsterSpawns(getSettings().getTicksPerMonsterSpawns());
        }
        return w;

    }

    @Override
    public WorldSettings getWorldSettings() {
        return getSettings();
    }

    @Override
    public @Nullable ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return chunkGenerator;
    }

    @Override
    public void saveWorldSettings() {
        if (settings != null) {
            configObject.saveConfigObject(settings);
        }
    }

    /* (non-Javadoc)
     * @see world.bentobox.bentobox.api.addons.Addon#allLoaded()
     */
    @Override
    public void allLoaded() {
        // Reload settings and save them. This will occur after all addons have loaded
        this.loadSettings();
        this.saveWorldSettings();
    }

    /**
     * @param i - island
     * @return one block island data
     */
    public OneBlockIslands getOneBlocksIsland(Island i) {
        return listener.getIsland(i);
    }
}
