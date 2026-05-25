package tj.capability.impl.handler;

public interface ICoilHandler extends IRecipeHandler {

    default int getCoilLevel() {
        return 1;
    }

    default int getCoilTemperature() {
        return 0;
    }

    default int getCoilEnergyDiscount() {
        return 1;
    }
}
