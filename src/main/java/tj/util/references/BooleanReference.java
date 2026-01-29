package tj.util.references;

/**
 * Simple boolean holder to pass as reference. Use {@link java.util.concurrent.atomic.AtomicBoolean} for concurrency.
 */
public final class BooleanReference {

    private boolean value;

    public BooleanReference() {}

    public BooleanReference(boolean value) {
        this.value = value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public boolean isValue() {
        return this.value;
    }
}
