package tj.integration.ae2;

public interface ITickSetter {

    default int getTickTime() {
        return 1;
    }

    default void setTickTime(long tickTime) {}
}
