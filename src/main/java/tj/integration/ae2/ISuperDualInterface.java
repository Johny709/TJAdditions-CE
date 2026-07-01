package tj.integration.ae2;

public interface ISuperDualInterface extends ISuperInterface, ISuperFluidInterface {

    @Override
    default int getTickTime() {
        return ISuperInterface.super.getTickTime();
    }

    @Override
    default void setTickTime(String tickTime, String id) {
        ISuperInterface.super.setTickTime(tickTime, id);
    }
}
