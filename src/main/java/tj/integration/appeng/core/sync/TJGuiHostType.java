package tj.integration.appeng.core.sync;

public enum TJGuiHostType {
    ITEM_OR_WORLD, ITEM, WORLD;

    public boolean isItem() {
        return this != WORLD;
    }

    boolean isTile() {
        return this != ITEM;
    }
}
