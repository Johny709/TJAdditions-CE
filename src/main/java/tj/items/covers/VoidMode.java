package tj.items.covers;

import net.minecraft.util.IStringSerializable;

public enum VoidMode implements IStringSerializable {
    NORMAL("void_cover.mode.normal"),
    SUPPLY("void_cover.mode.supply"),
    EXACT("void_cover.mode.exact");

    private final String mode;

    VoidMode(String mode) {
        this.mode = mode;
    }

    @Override
    public String getName() {
        return this.mode;
    }
}
