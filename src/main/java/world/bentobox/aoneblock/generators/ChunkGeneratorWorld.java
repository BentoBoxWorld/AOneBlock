package world.bentobox.aoneblock.generators;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import world.bentobox.aoneblock.AOneBlock;

/**
 * @author tastybento
 *         Creates the world
 */
public class ChunkGeneratorWorld extends ChunkGenerator {

    /**
     * @param addon - addon
     */
    public ChunkGeneratorWorld(AOneBlock addon) {
        super();
    }

    @SuppressWarnings("deprecation")
    public ChunkData generateChunks(World world) {
        return createChunkData(world);
    }

    @Override
    @Deprecated
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biomeGrid) {
        return generateChunks(world);
    }

    // This needs to be set to return true to override minecraft's default
    // behavior
    @Override
    public boolean canSpawn(World world, int x, int z) {
        return true;
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(final World world) {
        return Collections.emptyList();
    }

}