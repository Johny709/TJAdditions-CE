package tj.util;

public final class TJUtility {

    private TJUtility() {}

    /**
     * EU gets rounded down by tier minimum EU.
     * e.g. 32 = tier 1 (LV), 128 = tier 2 (MV). 64 or 120 is still tier 1 (LV).
     * @param voltage EU
     * @return tier
     */
    public static byte getTierByVoltage(long voltage) {
        long eut = 8;
        for (byte i = 0; eut > 0; i++) {
            if ((eut *= 4) > voltage)
                return i;
        }
        return 0;
    }

    /**
     * EU gets rounded up to next tier EU.
     * e.g. 32 = tier 1 (LV), 128 = tier 2 (MV). 64 or 120 becomes tier 2 (MV).
     * @param voltage EU
     * @return tier
     */
    public static byte getTierFromVoltage(long voltage) {
        long eut = 2;
        for (byte i = 0; eut > 0; i++) {
            if ((eut *= 4) >= voltage)
                return i;
        }
        return 0;
    }
}
