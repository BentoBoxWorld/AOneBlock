package world.bentobox.aoneblock;

import java.io.IOException;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.generator.ChunkGenerator;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.aoneblock.commands.admin.AdminCommand;
import world.bentobox.aoneblock.commands.island.PlayerCommand;
import world.bentobox.aoneblock.dataobjects.OneBlockIslands;
import world.bentobox.aoneblock.generators.ChunkGeneratorWorld;
import world.bentobox.aoneblock.listeners.BlockListener;
import world.bentobox.aoneblock.listeners.BlockProtect;
import world.bentobox.aoneblock.listeners.HoloListener;
import world.bentobox.aoneblock.listeners.InfoListener;
import world.bentobox.aoneblock.listeners.ItemsAdderListener;
import world.bentobox.aoneblock.listeners.JoinLeaveListener;
import world.bentobox.aoneblock.listeners.BossBarListener;
import world.bentobox.aoneblock.listeners.NoBlockHandler;
import world.bentobox.aoneblock.listeners.StartSafetyListener;
import world.bentobox.aoneblock.oneblocks.OneBlockCustomBlockCreator;
import world.bentobox.aoneblock.oneblocks.OneBlocksManager;
import world.bentobox.aoneblock.oneblocks.customblock.ItemsAdderCustomBlock;
import world.bentobox.aoneblock.requests.IslandStatsHandler;
import world.bentobox.aoneblock.requests.LocationStatsHandler;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.configuration.Config;
import world.bentobox.bentobox.api.configuration.WorldSettings;
import world.bentobox.bentobox.api.flags.Flag;
import world.bentobox.bentobox.api.flags.Flag.Mode;
import world.bentobox.bentobox.api.flags.Flag.Type;
import world.bentobox.bentobox.database.objects.Island;

/**
 * Main OneBlock class - provides an island minigame in the sky
 *
 * @author tastybento
 */
public class AOneBlock extends GameModeAddon {

    private static final String NETHER = "_nether";
    private static final String THE_END = "_the_end";
    private boolean hasItemsAdder = false;

    // Settings
    private Settings settings;
    private ChunkGeneratorWorld chunkGenerator;
    private final Config<Settings> configObject = new Config<>(this, Settings.class);
    private BlockListener blockListener;
    private OneBlocksManager oneBlockManager;
    private AOneBlockPlaceholders phManager;
    private HoloListener holoListener;

    // Flag
    public final Flag START_SAFETY = new Flag.Builder("START_SAFETY", Material.BAMBOO_BLOCK)
            .mode(Mode.BASIC)
            .type(Type.WORLD_SETTING)
            .listener(new StartSafetyListener(this))
            .defaultSetting(false)
            .build();
    private BossBarListener bossBar = new BossBarListener(this);
    public final Flag ONEBLOCK_BOSSBAR = new Flag.Builder("ONEBLOCK_BOSSBAR", Material.DRAGON_HEAD).mode(Mode.BASIC)
            .type(Type.SETTING).listener(bossBar).defaultSetting(true).build();

    @Override
    public void onLoad() {
        // Check if ItemsAdder exists, if yes register listener
        if (Bukkit.getPluginManager().getPlugin("ItemsAdder") != null) {
            registerListener(new ItemsAdderListener(this));
            OneBlockCustomBlockCreator.register(ItemsAdderCustomBlock::fromId);
            OneBlockCustomBlockCreator.register("itemsadder", ItemsAdderCustomBlock::fromMap);
            hasItemsAdder = true;
        }
        // Save the default config from config.yml
        saveDefaultConfig();
        // Load settings from config.yml. This will check if there are any issues with
        // it too.
        if (loadSettings()) {
            // Chunk generator
            chunkGenerator = settings.isUseOwnGenerator() ? null : new ChunkGeneratorWorld(this);
            // Register commands
            playerCommand = new PlayerCommand(this);
            adminCommand = new AdminCommand(this);
            // Register flag with BentoBox
            // Register protection flag with BentoBox
            getPlugin().getFlagsManager().registerFlag(this, START_SAFETY);
            // Bossbar
            getPlugin().getFlagsManager().registerFlag(this, this.ONEBLOCK_BOSSBAR);
        }
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
    public void onEnable() {
        oneBlockManager = new OneBlocksManager(this);
        if (loadData()) {
            // Failed to load - don't register anything
            return;
        }
        blockListener = new BlockListener(this);
        registerListener(blockListener);
        registerListener(new NoBlockHandler(this));
        registerListener(new BlockProtect(this));
        registerListener(new JoinLeaveListener(this));
        registerListener(new InfoListener(this));
        registerListener(bossBar);
        // Register placeholders
        phManager = new AOneBlockPlaceholders(this, getPlugin().getPlaceholdersManager());

        // Register request handlers
        registerRequestHandler(new IslandStatsHandler(this));
        registerRequestHandler(new LocationStatsHandler(this));

        // Register Holograms
        holoListener = new HoloListener(this);
        registerListener(holoListener);
    }

    // Load phase data
    public boolean loadData() {
        try {
            oneBlockManager.loadPhases();
        } catch (IOException e) {
            // Disable
            logError("AOneBlock settings could not load (oneblock.yml error)! Addon disabled.");
            logError(e.getMessage());
            setState(State.DISABLED);
            return true;
        }
        return false;
    }

    @Override
    public void onDisable() {
        // save cache
        if (blockListener != null) {
            blockListener.saveCache();
        }

        // Clear holograms
        if (holoListener != null) {
            holoListener.onDisable();
        }
    }

    @Override
    public void onReload() {
        // save cache
        blockListener.saveCache();
        if (loadSettings()) {
            log("Reloaded AOneBlock settings");
            loadData();
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
            netherWorld = settings.isNetherIslands() ? getWorld(worldName, World.Environment.NETHER, chunkGenerator)
                    : getWorld(worldName, World.Environment.NETHER, null);
        }
        // Make the end if it does not exist
        if (settings.isEndGenerate()) {
            if (getServer().getWorld(worldName + THE_END) == null) {
                log("Creating AOneBlock's End World...");
            }
            endWorld = settings.isEndIslands() ? getWorld(worldName, World.Environment.THE_END, chunkGenerator)
                    : getWorld(worldName, World.Environment.THE_END, null);
        }
    }

    /**
     * Gets a world or generates a new world if it does not exist
     *
     * @param worldName2      - the overworld name
     * @param env             - the environment
     * @param chunkGenerator2 - the chunk generator. If <tt>null</tt> then the
     *                        generator will not be specified
     * @return world loaded or generated
     */
    private World getWorld(String worldName2, Environment env, ChunkGeneratorWorld chunkGenerator2) {
        // Set world name
        worldName2 = env.equals(World.Environment.NETHER) ? worldName2 + NETHER : worldName2;
        worldName2 = env.equals(World.Environment.THE_END) ? worldName2 + THE_END : worldName2;
        WorldCreator wc = WorldCreator.name(worldName2).environment(env);
        World w = settings.isUseOwnGenerator() ? wc.createWorld() : wc.generator(chunkGenerator2).createWorld();
        // Set spawn rates
        if (w != null) {
            setSpawnRates(w);
        }
        return w;

    }

    private void setSpawnRates(World w) {
        if (getSettings().getSpawnLimitMonsters() > 0) {
            w.setSpawnLimit(SpawnCategory.MONSTER, getSettings().getSpawnLimitMonsters());
        }
        if (getSettings().getSpawnLimitAmbient() > 0) {
            w.setSpawnLimit(SpawnCategory.AMBIENT, getSettings().getSpawnLimitAmbient());
        }
        if (getSettings().getSpawnLimitAnimals() > 0) {
            w.setSpawnLimit(SpawnCategory.ANIMAL, getSettings().getSpawnLimitAnimals());
        }
        if (getSettings().getSpawnLimitWaterAnimals() > 0) {
            w.setSpawnLimit(SpawnCategory.WATER_ANIMAL, getSettings().getSpawnLimitWaterAnimals());
        }
        if (getSettings().getTicksPerAnimalSpawns() > 0) {
            w.setTicksPerSpawns(SpawnCategory.ANIMAL, getSettings().getTicksPerAnimalSpawns());
        }
        if (getSettings().getTicksPerMonsterSpawns() > 0) {
            w.setTicksPerSpawns(SpawnCategory.MONSTER, getSettings().getTicksPerMonsterSpawns());
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

    @Override
    public void saveDefaultConfig() {
        super.saveDefaultConfig();
        // Save default phases panel
        this.saveResource("panels/phases_panel.yml", false);
    }

    /*
     * (non-Javadoc)
     * 
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
     *
     * @return the phManager
     */
    public AOneBlockPlaceholders getPlaceholdersManager() {
        return phManager;
    }

    /**
     * @return the holoListener
     */
    public HoloListener getHoloListener() {
        return holoListener;
    }

    /**
     * @return true if ItemsAdder is on the server
     */
    public boolean hasItemsAdder() {
        return hasItemsAdder;
    }

    /**
     * Set the addon's world. Used only for testing.
     * @param world world
     */
    public void setIslandWorld(World world) {
        this.islandWorld = world;

    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    /**
     * @return the bossBar
     */
    public BossBarListener getBossBar() {
        return bossBar;
    }

}
