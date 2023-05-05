package world.bentobox.aoneblock.oneblocks;

import world.bentobox.aoneblock.oneblocks.customblock.BlockDataCustomBlock;

import java.util.*;
import java.util.function.Function;

/**
 * A creator for {@link OneBlockCustomBlock}
 *
 * @author HSGamer
 */
public final class OneBlockCustomBlockCreator {
    private static final Map<String, Function<Map<?, ?>, Optional<? extends OneBlockCustomBlock>>> creatorMap = new LinkedHashMap<>();
    private static final List<Function<String, Optional<? extends OneBlockCustomBlock>>> shortCreatorList = new ArrayList<>();

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
     * Register a short creator
     *
     * @param creator the creator
     */
    public static void register(Function<String, Optional<? extends OneBlockCustomBlock>> creator) {
        shortCreatorList.add(creator);
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

    /**
     * Create a custom block from the short type
     *
     * @param type the short type
     * @return the custom block
     */
    public static Optional<OneBlockCustomBlock> create(String type) {
        for (Function<String, Optional<? extends OneBlockCustomBlock>> creator : shortCreatorList) {
            Optional<? extends OneBlockCustomBlock> customBlock = creator.apply(type);
            if (customBlock.isPresent()) {
                return Optional.of(customBlock.get());
            }
        }
        return Optional.empty();
    }
}
