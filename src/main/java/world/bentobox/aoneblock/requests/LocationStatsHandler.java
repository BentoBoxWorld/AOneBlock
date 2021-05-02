package world.bentobox.aoneblock.requests;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import world.bentobox.aoneblock.AOneBlock;
import world.bentobox.bentobox.api.addons.request.AddonRequestHandler;
import world.bentobox.bentobox.api.user.User;

/**
 * Provides stats based on the user's location.<br>
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
public class LocationStatsHandler extends AddonRequestHandler {

    private final AOneBlock addon;
    private static final Object PLAYER = "player";

    public LocationStatsHandler(AOneBlock addon) {
        super("location-stats");
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
        - empty map if UUID is invalid or player is offline
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
        if (user == null || !user.isOnline()) {
            return Collections.emptyMap();
        }
        // No null check required
        Map<String, String> result = new HashMap<>();
        result.put("count", addon.getPlaceholdersManager().getCountByLocation(user));
        result.put("doneScale", addon.getPlaceholdersManager().getDoneScaleByLocation(user));
        result.put("nextPhaseBlocks", addon.getPlaceholdersManager().getNextPhaseBlocksByLocation(user));
        result.put("nextPhase", addon.getPlaceholdersManager().getNextPhaseByLocation(user));
        result.put("percentDone", addon.getPlaceholdersManager().getPercentDoneByLocation(user));
        result.put("phase", addon.getPlaceholdersManager().getPhaseByLocation(user));
        return result;
    }

}
