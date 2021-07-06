package world.bentobox.aoneblock.requests;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.bentobox.api.addons.request.AddonRequestHandler;
import world.bentobox.bentobox.api.user.User;

/**
 * Provides stats on the player's island.<br>
 * Submit "player" -> UUID to {@link #handle(Map)}.<br>
 * Return map is a Map<String, String> of the following:
 *      <ul><li>"count" - block count of island</li>
 *      <li>"doneScale" - character scale of phase completion</li>
 *      <li>"nextPhaseBlocks" - number of blocks to next phase</li>
 *      <li>"nextPhase" - next phase name</li>
 *      <li>"percentDone" - percentage done of this phase</li>
 *      <li>"phase" - current phase name</li></ul>
 * @author tastybento
 *
 */
public class IslandStatsHandler extends AddonRequestHandler {

    private final AOneBlock addon;
    private static final Object PLAYER = "player";
    
    public IslandStatsHandler(AOneBlock addon) {
        super("island-stats");
        this.addon = addon;
    }

    /* (non-Javadoc)
     * @see world.bentobox.bentobox.api.addons.request.AddonRequestHandler#handle(java.util.Map)
     */
    @Override
    public Object handle(Map<String, Object> map) {
        /*
        What we need in the map:
        "player" -> UUID
        
        What we will return:
        - empty map if UUID is invalid
        - a map of island stats:
        
        "count" - block count of island
        "doneScale" - character scale of phase completion
        "nextPhaseBlocks" - number of blocks to next phase
        "nextPhase" - next phase name
        "percentDone" - percentage done of this phase
        "phase" - current phase name
     */

        if (map == null || map.isEmpty() || map.get(PLAYER) == null || !(map.get(PLAYER) instanceof UUID)) {
            return Collections.emptyMap();
        }

        User user = User.getInstance((UUID)map.get(PLAYER));
        // No null check required
        Map<String, String> result = new HashMap<>();
        result.put("count", addon.getPlaceholdersManager().getCount(user));
        result.put("doneScale", addon.getPlaceholdersManager().getDoneScale(user));
        result.put("nextPhaseBlocks", addon.getPlaceholdersManager().getNextPhaseBlocks(user));
        result.put("nextPhase", addon.getPlaceholdersManager().getNextPhase(user));
        result.put("percentDone", addon.getPlaceholdersManager().getPercentDone(user));
        result.put("phase", addon.getPlaceholdersManager().getPhase(user));
    return result;
    }

}
