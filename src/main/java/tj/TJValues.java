package tj;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.unification.material.type.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.capability.impl.workable.BasicEnergyHandler;

import java.text.DecimalFormat;
import java.util.Collections;

import static gregicadditions.GAMaterials.*;
import static gregtech.api.unification.material.MarkerMaterials.Tier.*;

public final class TJValues {

    private TJValues() {}

    public static final int[] VC = {0x6b5f55, 0x9b9b9b, 0xf78b2c, 0xffb900, 0x656565, 0xffffff, 0xffb0b0, 0xffe9e9, 0xc6ffad, 0xffc4f8, 0x2776ff, 0xf0ff00, 0x9500cd, 0xff6565, 0xfff7f7};
    public static final String[] VCC = {"§c", "§7", "§6", "§e", "§8", "§a", "§d", "§b", "§2", "§9", "§5", "§1§l", "§c§l§n", "§4§l§n", "§f§l§n"};

    public static final long[] VOC = { 8, 32, 128, 512, 2048, 8192, 32768, 131072, 524288, 2097152, 8388608, 33554432,
            134217728, 536870912, Integer.MAX_VALUE, 8589934592L, 34359738368L, 137438953472L, 549755813888L, 2199023255552L,
            8796093022208L, 35184372088832L, 140737488355328L, 562949953421312L, 2251799813685248L, 9007199254740992L,
            36028797018963968L, 144115188075855872L, 576460752303423488L, 2305843009213693952L, Long.MAX_VALUE };

    public static final Material[] CIRCUIT_TIERS = {Primitive, Basic, Good, Advanced, Extreme, Elite, Master, Ultimate, Superconductor, Infinite, UEV, UIV, UMV, UXV, MAX};

    public static final BlockPos DUMMY_POS = new BlockPos(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);

    public static final IFluidTank DUMMY_TANK = new FluidTank(0);

    public static final BasicEnergyHandler DUMMY_ENERGY = new BasicEnergyHandler(0);

    public static final IMultipleTankHandler DUMMY_FLUID_HANDLER = new FluidTankList(true);

    public static final IItemHandlerModifiable DUMMY_ITEM_HANDLER = new ItemHandlerList(Collections.emptyList());

    public static final DecimalFormat thousandFormat = new DecimalFormat(",###");

    public static final DecimalFormat thousandTwoPlaceFormat = new DecimalFormat(",##0.00");

    public static boolean isFalse() {
        return false;
    }
}
