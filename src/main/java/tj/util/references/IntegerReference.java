package tj.util.references;

/**
 * Simple integer holder to pass as reference. Use {@link java.util.concurrent.atomic.AtomicInteger} for concurrency.
 */
public final class IntegerReference {

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

    @Override
    public String toString() {
        return String.valueOf(this.value);
    }
}
