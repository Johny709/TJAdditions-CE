package tj.util;

public final class Counter {

    public static final Counter DUMMY_COUNTER = new Counter(0);

    private long value;

    public Counter(long initialValue) {
        this.value = initialValue;
    }

    public void increment(int amount) {
        this.value += amount;
    }

    public void decrement(int amount) {
        this.value -= amount;
    }

    public long getValue() {
        return this.value;
    }
}
