package tj.util;

import net.minecraft.util.EnumFacing;

public final class EnumFacingHelper {

    private EnumFacingHelper() {}

    public static EnumFacing getTopFacingFrom(EnumFacing facing) {
        switch (facing) {
            case UP:
            case DOWN: return facing.rotateAround(EnumFacing.Axis.X);
            case SOUTH: return facing.rotateAround(EnumFacing.Axis.Y).rotateAround(EnumFacing.Axis.Z);
            case NORTH: return facing.rotateAround(EnumFacing.Axis.Y).rotateAround(EnumFacing.Axis.Z).getOpposite();
            case EAST: return facing.rotateAround(EnumFacing.Axis.Z).getOpposite();
            default: return facing.rotateAround(EnumFacing.Axis.Z);
        }
    }

    public static EnumFacing getBottomFacingFrom(EnumFacing facing) {
        return getTopFacingFrom(facing).getOpposite();
    }

    public static EnumFacing getLeftFacingFrom(EnumFacing facing) {
        switch (facing) {
            case UP: return facing.rotateAround(EnumFacing.Axis.Z).getOpposite();
            case DOWN: return facing.rotateAround(EnumFacing.Axis.Z);
            default: return facing.rotateY();
        }
    }

    public static EnumFacing getRightFacingFrom(EnumFacing facing) {
        return getLeftFacingFrom(facing).getOpposite();
    }

    public static EnumFacing getBottomFacingFromSpin(EnumFacing facing) {
        switch (facing) {
            case NORTH: return EnumFacingHelper.getBottomFacingFrom(facing);
            case SOUTH: return EnumFacingHelper.getTopFacingFrom(facing);
            case WEST: return EnumFacingHelper.getLeftFacingFrom(facing);
            case EAST: return EnumFacingHelper.getRightFacingFrom(facing);
            default: return facing;
        }
    }

    public static EnumFacing getLeftFacingFromSpin(EnumFacing facing) {
        switch (facing) {
            case NORTH: return EnumFacingHelper.getLeftFacingFrom(facing);
            case SOUTH: return EnumFacingHelper.getRightFacingFrom(facing);
            case WEST: return EnumFacingHelper.getBottomFacingFrom(facing);
            case EAST: return EnumFacingHelper.getTopFacingFrom(facing);
            default: return facing;
        }
    }

    public static EnumFacing getRightFacingFromSpin(EnumFacing facing) {
        switch (facing) {
            case NORTH: return EnumFacingHelper.getRightFacingFrom(facing);
            case SOUTH: return EnumFacingHelper.getLeftFacingFrom(facing);
            case WEST: return EnumFacingHelper.getTopFacingFrom(facing);
            case EAST: return EnumFacingHelper.getBottomFacingFrom(facing);
            default: return facing;
        }
    }
}
