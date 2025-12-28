package tj.textures;

import net.minecraft.util.EnumFacing;

public enum OverlayFace {
    FRONT, BACK, TOP, BOTTOM, SIDE;

    public static OverlayFace bySide(EnumFacing side, EnumFacing frontFacing) {
        if (side == frontFacing) {
            return FRONT;
        } else if (side.getOpposite() == frontFacing) {
            return BACK;
        } else if (side == EnumFacing.UP) {
            return TOP;
        } else if (side == EnumFacing.DOWN) {
            return BOTTOM;
        }else return SIDE;
    }
}
