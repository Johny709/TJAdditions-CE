package tj.util.references;

/**
 * Simple integer holder to pass as reference. Use {@link java.util.concurrent.atomic.AtomicInteger} for concurrency.
 */
public class IntegerReference {

    private int value;

    public IntegerReference() {}

    public IntegerReference(int value) {
        this.value = value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
