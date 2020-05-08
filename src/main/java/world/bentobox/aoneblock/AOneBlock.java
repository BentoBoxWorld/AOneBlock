package world.bentobox.aoneblock;

import java.io.IOException;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.generator.ChunkGenerator;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.aoneblock.commands.AdminCommand;
import world.bentobox.aoneblock.commands.PlayerCommand;
import world.bentobox.aoneblock.dataobjects.OneBlockIslands;
import world.bentobox.aoneblock.generators.ChunkGeneratorWorld;
import world.bentobox.aoneblock.listeners.BlockListener;
import world.bentobox.aoneblock.listeners.NoBlockHandler;
import world.bentobox.aoneblock.oneblocks.OneBlocksManager;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.configuration.Config;
import world.bentobox.bentobox.api.configuration.WorldSettings;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;

/**
 * Main OneBlock class - provides an island minigame in the sky
 * @author tastybento
 */
public class AOneBlock extends GameModeAddon {

    private static final String NETHER = "_nether";
    private static final String THE_END = "_the_end";

    // Settings
    private Settings settings;
    private ChunkGeneratorWorld chunkGenerator;
    private final Config<Settings> configObject = new Config<>(this, Settings.class);
    private BlockListener listener;
    private OneBlocksManager oneBlockManager;

    @Override
    public void onLoad() {
        // Save the default config from config.yml
        saveDefaultConfig();
        // Load settings from config.yml. This will check if there are any issues with it too.
        loadSettings();
        // Chunk generator
        chunkGenerator = settings.isUseOwnGenerator() ? null : new ChunkGeneratorWorld(this);
        // Register commands
        playerCommand = new PlayerCommand(this);
        adminCommand = new AdminCommand(this);
    }

    private boolean loadSettings() {
        // Load settings again to get worlds
        settings = configObject.loadConfigObject();
        if (settings == null) {
            // Disable
            logError("AOneBlock settings could not load! Addon disabled.");
            setState(State.DISABLED);
            return false;
        }
        return true;
    }

    @Override
    public void onEnable(){
        try {
            oneBlockManager = new OneBlocksManager(this);
            oneBlockManager.loadPhases();
            listener = new BlockListener(this);
            registerListener(listener);
            registerListener(new NoBlockHandler(this));
            // Register placeholders
            getPlugin().getPlaceholdersManager().registerPlaceholder(this,"visited_island_phase", this::getPhaseByUser);
            getPlugin().getPlaceholdersManager().registerPlaceholder(this,"visited_island_count", this::getCountByUser);
            getPlugin().getPlaceholdersManager().registerPlaceholder(this,"my_island_phase", this::getPhaseByIsland);
            getPlugin().getPlaceholdersManager().registerPlaceholder(this,"my_island_count", this::getCountByIsland);
        } catch (IOException | InvalidConfigurationException e) {
            // Disable
            logError("AOneBlock settings could not load (oneblock.yml error)! Addon disabled.");
            logError(e.getMessage());
            e.printStackTrace();
            setState(State.DISABLED);
        }

    }

    private String getPhaseByUser(User user) {
        return getIslands().getProtectedIslandAt(user.getLocation())
                .map(this::getOneBlocksIsland)
                .map(OneBlockIslands::getPhaseName)
                .orElse("");
    }

    private String getCountByUser(User user) {
        return getIslands().getProtectedIslandAt(user.getLocation())
                .map(this::getOneBlocksIsland)
                .map(OneBlockIslands::getBlockNumber)
                .map(String::valueOf)
                .orElse("");
    }

    private String getPhaseByIsland(User user) {
        Island i = getIslands().getIsland(getOverWorld(), user);
        return i == null ? "" : getOneBlocksIsland(i).getPhaseName();
    }

    private String getCountByIsland(User user) {
        Island i = getIslands().getIsland(getOverWorld(), user);
        return i == null ? "" : String.valueOf(getOneBlocksIsland(i).getBlockNumber());
    }

    @Override
    public void onDisable() {
        // save cache
        listener.saveCache();
    }

    @Override
    public void onReload() {
        if (loadSettings()) {
            log("Reloaded AOneBlock settings");
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
            log("Creating AOneBlock world ...");
        }

        // Create the world if it does not exist
        islandWorld = getWorld(worldName, World.Environment.NORMAL, chunkGenerator);
        // Make the nether if it does not exist
        if (settings.isNetherGenerate()) {
            if (getServer().getWorld(worldName + NETHER) == null) {
                log("Creating AOneBlock's Nether...");
            }
            netherWorld = settings.isNetherIslands() ? getWorld(worldName, World.Environment.NETHER, chunkGenerator) : getWorld(worldName, World.Environment.NETHER, null);
        }
        // Make the end if it does not exist
        if (settings.isEndGenerate()) {
            if (getServer().getWorld(worldName + THE_END) == null) {
                log("Creating AOneBlock's End World...");
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
    @NonNull
    public OneBlockIslands getOneBlocksIsland(Island i) {
        return listener.getIsland(i);
    }

    public OneBlocksManager getOneBlockManager() {
        return oneBlockManager;
    }
}
