package tj.items;

public enum LinkingMode {
    BLOCK("metaitem.linking.device.mode.block"),
    BLOCK_PROMPT("metaitem.linking.device.mode.block.prompt"),
    ENTITY("metaitem.linking.device.mode.entity"),
    ENTITY_PROMPT("metaitem.linking.device.mode.entity.prompt");

    LinkingMode(String mode) {
        this.mode = mode;
    }

    private final String mode;

    public String getMode() {
        return mode;
    }
}
