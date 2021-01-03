package world.bentobox.aoneblock.oneblocks;

/**
 * Requirement for finishing a phase
 * @author tastybento
 *
 */
public class Requirement {
    public enum ReqType {
        ECO("economy-balance", Double.class),
        BANK("bank-balance", Double.class),
        LEVEL("level", Long.class),
        PERMISSION("permission", String.class);

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

    private Object requirement;
    private final ReqType type;

    /**
     * @param money - money required to
     */
    public Requirement(ReqType type, Object requirement) {
        this.type = type;
        this.requirement = requirement;
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
     * @return the type
     */
    public ReqType getType() {
        return type;
    }



}
