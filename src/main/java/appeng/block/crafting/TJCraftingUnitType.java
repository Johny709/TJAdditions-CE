package appeng.block.crafting;

public enum TJCraftingUnitType {
    UNIT(0),
    STORAGE_65M(65536),
    STORAGE_262M(262144),
    STORAGE_1048M(1048576),
    STORAGE_SINGULARITY(2097152);

    private final int bytes;

    TJCraftingUnitType(int bytes) {
        this.bytes = bytes;
    }

    public int getBytes() {
        return this.bytes;
    }
}
