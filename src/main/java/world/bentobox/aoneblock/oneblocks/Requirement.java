package world.bentobox.aoneblock.oneblocks;

/**
 * Requirement for finishing a phase
 * @author tastybento
 *
 */
public record Requirement(ReqType type, Object requirement) {
    /**
     * Requirement type enum
     *
     */
    public enum ReqType {
        /**
         * Economy balance
         */
        ECO("economy-balance", Double.class),
        /**
         * Bank Balance
         */
        BANK("bank-balance", Double.class),
        /**
         * Island Level
         */
        LEVEL("level", Long.class),
        /**
         * Permission
         */
        PERMISSION("permission", String.class),
        /**
         * Cooldown
         */
        COOLDOWN("cooldown", Long.class);

        private final String key;
        private final Class<?> clazz;

        ReqType(String string, Class<?> class1) {
            this.key = string;
            this.clazz = class1;
        }

        /**
         * @return the key
         */
        public String getKey() {
            return key;
        }

        /**
         * @return the clazz
         */
        public Class<?> getClazz() {
            return clazz;
        }
    }

    /**
     * @return the bank balance req
     */
    public double getBank() {
        return (double)requirement;
    }

    /**
     * @return the economy balance req
     */
    public double getEco() {
        return (double)requirement;
    }

    /**
     * @return the level
     */
    public long getLevel() {
        return (long)requirement;
    }

    /**
     * @return the permission
     */
    public String getPermission() {
        return (String)requirement;
    }

    /**
     * @return the cooldown
     */
    public long getCooldown() {
        return (long)requirement;
    }

    /**
     * @return the type
     */
    public ReqType getType() {
        return type;
    }



}
