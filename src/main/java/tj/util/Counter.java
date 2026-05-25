package tj.util;

public class Counter {

    public static final Counter DUMMY_COUNTER = new Counter(0) {
        @Override
        public void increment(int amount) {}

        @Override
        public void decrement(int amount) {}
    };

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
