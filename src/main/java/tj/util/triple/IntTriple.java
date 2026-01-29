package tj.util.triple;

public final class IntTriple {

    private final int left;
    private final int middle;
    private final int right;

    public IntTriple(int left, int middle, int right) {
        this.left = left;
        this.middle = middle;
        this.right = right;
    }

    public int getLeft() {
        return this.left;
    }

    public int getMiddle() {
        return this.middle;
    }

    public int getRight() {
        return this.right;
    }
}
