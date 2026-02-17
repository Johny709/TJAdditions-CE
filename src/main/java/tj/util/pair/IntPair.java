package tj.util.pair;

public final class IntPair<R> {

    private final int left;
    private final R right;

    public IntPair(int left, R right) {
        this.left = left;
        this.right = right;
    }

    public static <R> IntPair<R> of(int left, R right) {
        return new IntPair<>(left, right);
    }

    public int getLeft() {
        return this.left;
    }

    public int getKey() {
        return this.getLeft();
    }

    public R getRight() {
        return this.right;
    }

    public R getValue() {
        return this.getRight();
    }
}
