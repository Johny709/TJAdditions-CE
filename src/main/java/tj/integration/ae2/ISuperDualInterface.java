package tj.integration.ae2;

public interface ISuperDualInterface extends ISuperInterface, ISuperFluidInterface {

    @Override
    default int getTickTime() {
        return ISuperInterface.super.getTickTime();
    }

    @Override
    default void setTickTime(long tickTime) {
        ISuperInterface.super.setTickTime(tickTime);
    }
}
