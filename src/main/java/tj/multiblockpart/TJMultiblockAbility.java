package tj.multiblockpart;

import tj.capability.impl.handler.IRecipeMapProvider;
import tj.multiblockpart.utility.MetaTileEntityMachineController;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;

public class TJMultiblockAbility<T> extends MultiblockAbility<T> {
    public static final MultiblockAbility<IItemHandlerModifiable> CIRCUIT_SLOT = new TJMultiblockAbility<>();
    public static final MultiblockAbility<MetaTileEntityMachineController> REDSTONE_CONTROLLER = new TJMultiblockAbility<>();
    public static final MultiblockAbility<IFluidTank> STEAM_OUTPUT = new TJMultiblockAbility<>();
    public static final MultiblockAbility<IRecipeMapProvider> CRAFTER = new TJMultiblockAbility<>();
}
