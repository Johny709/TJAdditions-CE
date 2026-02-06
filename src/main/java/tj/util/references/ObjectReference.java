package tj.util.references;

/**
 * Simple object holder to pass as reference. Use {@link java.util.concurrent.atomic.AtomicReference} for concurrency.
 * @param <V> Type of object to hold.
 */
public final class ObjectReference<V> {

    private V value;

    public ObjectReference() {}

    public ObjectReference(V value) {
        this.value = value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public V getValue() {
        return value;
    }
}
