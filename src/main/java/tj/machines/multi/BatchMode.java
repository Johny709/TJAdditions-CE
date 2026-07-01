package tj.machines.multi;

public enum BatchMode {
    ONE(1),
    FOUR(4),
    SIXTEEN(16),
    SIXTY_FOUR(64),
    TWO_HUNDRED_FIFTY_SIX(256);

    private final int amount;

    BatchMode(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
            return amount;
        }
}
