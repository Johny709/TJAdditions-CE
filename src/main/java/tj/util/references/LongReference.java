package tj.util.references;

/**
 * Simple long holder to pass as reference. Use {@link java.util.concurrent.atomic.AtomicInteger} for concurrency.
 */
public final class LongReference {

    private long value;

    public LongReference() {}

    public LongReference(long value) {
        this.value = value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public long getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return String.valueOf(this.value);
    }
}
