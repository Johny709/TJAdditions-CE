package tj.util;

public final class Counter {
    private int value;

    public Counter(int initialValue) {
        this.value = initialValue;
    }

    public void increment(int amount) {
        this.value += amount;
    }

    public void decrement(int amount) {
        this.value -= amount;
    }

    public int getValue() {
        return this.value;
    }
}
