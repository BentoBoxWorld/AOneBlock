package world.bentobox.aoneblock.oneblocks;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import world.bentobox.aoneblock.oneblocks.customblock.BlockDataCustomBlock;

/**
 * A creator for {@link OneBlockCustomBlock}
 *
 * @author HSGamer
 */
public final class OneBlockCustomBlockCreator {
    private static final Map<String, Function<Map<?, ?>, Optional<? extends OneBlockCustomBlock>>> creatorMap = new LinkedHashMap<>();

    static {
        register("block-data", BlockDataCustomBlock::fromMap);
    }

    private OneBlockCustomBlockCreator() {
        // EMPTY
    }

    /**
     * Register a creator
     *
     * @param type    the type
     * @param creator the creator
     */
    public static void register(String type, Function<Map<?, ?>, Optional<? extends OneBlockCustomBlock>> creator) {
        creatorMap.put(type, creator);
    }

    /**
     * Create a custom block from the map
     *
     * @param map the map
     * @return the custom block
     */
    public static Optional<OneBlockCustomBlock> create(Map<?, ?> map) {
        String type = Objects.toString(map.get("type"), null);
        if (type == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(creatorMap.get(type)).flatMap(builder -> builder.apply(map));
    }
}
