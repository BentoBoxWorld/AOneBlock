package world.bentobox.aoneblock;

import java.io.IOException;
import java.util.Objects;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.generator.ChunkGenerator;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.aoneblock.commands.AdminCommand;
import world.bentobox.aoneblock.commands.PlayerCommand;
import world.bentobox.aoneblock.dataobjects.OneBlockIslands;
import world.bentobox.aoneblock.generators.ChunkGeneratorWorld;
import world.bentobox.aoneblock.listeners.BlockListener;
import world.bentobox.aoneblock.listeners.BlockProtect;
import world.bentobox.aoneblock.listeners.JoinLeaveListener;
import world.bentobox.aoneblock.listeners.NoBlockHandler;
import world.bentobox.aoneblock.oneblocks.OneBlocksManager;
import world.bentobox.aoneblock.requests.IslandStatsHandler;
import world.bentobox.aoneblock.requests.LocationStatsHandler;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.configuration.Config;
import world.bentobox.bentobox.api.configuration.WorldSettings;
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
    private BlockListener blockListener;
    private OneBlocksManager oneBlockManager;
    private PlaceholdersManager phManager;

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
        } else {
            // Save the settings
            configObject.saveConfigObject(settings);
        }
        return true;
    }

    @Override
    public void onEnable(){
        try {
            oneBlockManager = new OneBlocksManager(this);
            oneBlockManager.loadPhases();
            blockListener = new BlockListener(this);
        } catch (IOException e) {
            // Disable
            logError("AOneBlock settings could not load (oneblock.yml error)! Addon disabled.");
            logError(e.getMessage());
            setState(State.DISABLED);
            return;
        }

        registerListener(blockListener);
        registerListener(new NoBlockHandler(this));
        registerListener(new BlockProtect(this));
        registerListener(new JoinLeaveListener(this));
        // Register placeholders
        phManager = new PlaceholdersManager(this);
        getPlugin().getPlaceholdersManager().registerPlaceholder(this,"visited_island_phase", phManager::getPhaseByLocation);
        getPlugin().getPlaceholdersManager().registerPlaceholder(this,"visited_island_count", phManager::getCountByLocation);
        getPlugin().getPlaceholdersManager().registerPlaceholder(this,"my_island_phase", phManager::getPhase);
        getPlugin().getPlaceholdersManager().registerPlaceholder(this,"my_island_count", phManager::getCount);
        getPlugin().getPlaceholdersManager().registerPlaceholder(this,"visited_island_next_phase", phManager::getNextPhaseByLocation);
        getPlugin().getPlaceholdersManager().registerPlaceholder(this,"my_island_next_phase", phManager::getNextPhase);
        getPlugin().getPlaceholdersManager().registerPlaceholder(this,"my_island_blocks_to_next_phase", phManager::getNextPhaseBlocks);
        getPlugin().getPlaceholdersManager().registerPlaceholder(this,"visited_island_blocks_to_next_phase", phManager::getNextPhaseBlocksByLocation);
        getPlugin().getPlaceholdersManager().registerPlaceholder(this,"my_island_percent_done", phManager::getPercentDone);
        getPlugin().getPlaceholdersManager().registerPlaceholder(this,"visited_island_percent_done", phManager::getPercentDoneByLocation);
        getPlugin().getPlaceholdersManager().registerPlaceholder(this,"my_island_done_scale", phManager::getDoneScale);
        getPlugin().getPlaceholdersManager().registerPlaceholder(this,"visited_island_done_scale", phManager::getDoneScaleByLocation);

        // Register request handlers
        registerRequestHandler(new IslandStatsHandler(this));
        registerRequestHandler(new LocationStatsHandler(this));
    }


    @Override
    public void onDisable() {
        // save cache
        blockListener.saveCache();
    }

    @Override
    public void onReload() {
        // save cache
        blockListener.saveCache();
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
            setSpawnRates(w);
        }
        return w;

    }

    private void setSpawnRates(World w) {
        if (getSettings().getSpawnLimitMonsters() > 0) {
            w.setMonsterSpawnLimit(getSettings().getSpawnLimitMonsters());
        }
        if (getSettings().getSpawnLimitAmbient() > 0) {
            w.setAmbientSpawnLimit(getSettings().getSpawnLimitAmbient());
        }
        if (getSettings().getSpawnLimitAnimals() > 0) {
            w.setAnimalSpawnLimit(getSettings().getSpawnLimitAnimals());
        }
        if (getSettings().getSpawnLimitWaterAnimals() > 0) {
            w.setWaterAnimalSpawnLimit(getSettings().getSpawnLimitWaterAnimals());
        }
        if (getSettings().getTicksPerAnimalSpawns() > 0) {
            w.setTicksPerAnimalSpawns(getSettings().getTicksPerAnimalSpawns());
        }
        if (getSettings().getTicksPerMonsterSpawns() > 0) {
            w.setTicksPerMonsterSpawns(getSettings().getTicksPerMonsterSpawns());
        }

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
        // save settings. This will occur after all addons have loaded
        this.saveWorldSettings();
    }

    /**
     * @param i - island
     * @return one block island data
     */
    @NonNull
    public OneBlockIslands getOneBlocksIsland(@NonNull Island i) {
        return blockListener.getIsland(Objects.requireNonNull(i));
    }

    public OneBlocksManager getOneBlockManager() {
        return oneBlockManager;
    }

    /**
     * @return the blockListener
     */
    public BlockListener getBlockListener() {
        return blockListener;
    }

    /**
     * Get the placeholder manager
     * @return the phManager
     */
    public PlaceholdersManager getPlaceholdersManager() {
        return phManager;
    }


}
