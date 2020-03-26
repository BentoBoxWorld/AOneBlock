package world.bentobox.oneblock.listeners;

import java.util.Random;
import java.util.TreeMap;

import org.bukkit.Material;

public class BlockProbability {

    /**
     * Tree map of all materials and their probabilities as a ratio to the sum of all probabilities
     */
    private final TreeMap<Integer, Material> probMap = new TreeMap<>();
    /**
     * Sum of all probabilities
     */
    private int total = 0;


    /**
     * Adds a material and associated probability
     * @param material - Material
     * @param prob - probability
     */
    public void addBlock(Material material, int prob) {
        total += prob;
        probMap.put(total, material);
    }

    /**
     * This picks a random block with the following constraints:
     * A cactus is never chosen as the bottom block.
     * Water or lava never is placed above sugar cane or cactuses because when they grow, they will touch the
     * liquid and cause it to flow.
     * @param random - random object
     * @param bottom - if true, result will never be CACTUS
     * @param noLiquid - if true, result will never be water or lava
     * @return Material selected
     */

    public Material getBlock(Random random, boolean bottom, boolean noLiquid) {
        Material temp = probMap.get(random.nextInt(total));
        if (temp == null) {
            temp = probMap.ceilingEntry(random.nextInt(total)).getValue();
        }
        if (temp == null) {
            temp = probMap.firstEntry().getValue();
        }
        if (bottom && temp.equals(Material.CACTUS)) {
            return getBlock(random, true, noLiquid);
        } else if (noLiquid && (temp.equals(Material.WATER) || temp.equals(Material.LAVA))) {
            return getBlock(random, bottom, true);
        }
        return temp;
    }

    public int getSize() {
        return probMap.size();
    }

    public boolean isEmpty() {
        return probMap.isEmpty();
    }
}