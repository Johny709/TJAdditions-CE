package tj.capability;

public interface IGeneratorInfo {

    default long getConsumption() {
        return 0;
    }

    default long getProduction() {
        return 0;
    }

    /**
     * Everything is prefix until "suffix" String is passed in and will be part of suffix afterward.
     */
    default String[] consumptionInfo() {
        return null;
    }

    /**
     * Everything is prefix until "suffix" String is passed in and will be part of suffix afterward.
     */
    default String[] productionInfo() {
        return null;
    }
}
