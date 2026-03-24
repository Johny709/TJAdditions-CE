package tj.machines.multi.electric;

import gregicadditions.capabilities.GregicAdditionsCapabilities;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.BlockWorldState;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.electric.multiblockpart.MetaTileEntityMultiblockPart;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import tj.builder.multicontrollers.TJRecipeMapMultiblockController;
import tj.capability.OverclockManager;
import tj.capability.impl.handler.IDistillationHandler;
import tj.capability.impl.workable.BasicRecipeLogic;
import tj.textures.TJOrientedOverlayRenderer;
import tj.textures.TJTextures;
import tj.util.ItemStackHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiFunction;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;

public class MetaTileEntityTJDistillationTower extends TJRecipeMapMultiblockController implements IDistillationHandler {

    public static final IMultipleTankHandler VOID_TANK = new FluidTankList(true,  new FluidTank(Integer.MAX_VALUE) {
        @Override
        public int fill(FluidStack resource, boolean doFill) {
            return resource.amount;
        }
    });
    private final List<BlockPos> outputHatchPos = new ArrayList<>();

    public MetaTileEntityTJDistillationTower(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.DISTILLATION_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityTJDistillationTower(this.metaTileEntityId);
    }

    @Override
    protected BasicRecipeLogic<IDistillationHandler> createRecipeLogic() {
        return new DistillationRecipeLogic(this);
    }

    @Override
    public void preOverclock(OverclockManager<?> overclockManager, Recipe recipe) {
        overclockManager.setParallel(1);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(RIGHT, FRONT, DOWN)
                .aisle("XXX", "XMX", "XXX")
                .aisle("XXX", "X#X", "XXX").setRepeatable(0, 11)
                .aisle("ZSZ", "ZZZ", "ZZZ")
                .where('S', this.selfPredicate())
                .where('Z', statePredicate(this.getCasingState()).or(abilityPartPredicate(MultiblockAbility.IMPORT_FLUIDS, MultiblockAbility.EXPORT_ITEMS, MultiblockAbility.INPUT_ENERGY)))
                .where('X', this.countMatch("outputHatchCount", tilePredicate(outputHatchPredicate())).or(statePredicate(this.getCasingState())).or(abilityPartPredicate(GregicAdditionsCapabilities.MAINTENANCE_HATCH, MultiblockAbility.INPUT_ENERGY)))
                .where('M', abilityPartPredicate(GregicAdditionsCapabilities.MUFFLER_HATCH))
                .where('#', isAirPredicate())
                .validateLayer(0, context -> context.getOrDefault("outputHatchCount", 0) == 1)
                .validateLayer(1, context -> context.getOrDefault("outputHatchCount", 0) == 1)
                .build();
    }

    private IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN);
    }

    public static BiFunction<BlockWorldState, MetaTileEntity, Boolean> outputHatchPredicate() {
        return (state, tile) -> {
            if (tile instanceof MetaTileEntityMultiblockPart) {
                MetaTileEntityMultiblockPart multiblockPart = (MetaTileEntityMultiblockPart) tile;
                if (multiblockPart instanceof IMultiblockAbilityPart<?>) {
                    IMultiblockAbilityPart<?> abilityPart = (IMultiblockAbilityPart<?>) multiblockPart;
                    boolean isOutputHatch = abilityPart.getAbility() == MultiblockAbility.EXPORT_FLUIDS;
                    if (isOutputHatch) state.getMatchContext().getOrCreate("outputHatches", HashSet::new).add(state.getPos());
                    return isOutputHatch;
                }
            }
            return false;
        };
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.outputHatchPos.addAll(context.getOrDefault("outputHatches", new HashSet<>()));
        this.outputHatchPos.sort(Comparator.comparingInt(pos -> Math.abs(pos.getX() - this.getPos().getX()) + Math.abs(pos.getY() - this.getPos().getY()) + Math.abs(pos.getZ() - this.getPos().getZ())));
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.outputHatchPos.clear();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.CLEAN_STAINLESS_STEEL_CASING;
    }

    @Override
    public TJOrientedOverlayRenderer getFrontalOverlay() {
        return TJTextures.TJ_DISTILLERY_OVERLAY;
    }

    @Override
    public IMultipleTankHandler getOutputHatchAt(int index) {
        if (index >= this.outputHatchPos.size())
            return null;
        TileEntity tileEntity = this.getWorld().getTileEntity(this.outputHatchPos.get(index));
        if (!(tileEntity instanceof MetaTileEntityHolder))
            return null;
        MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) tileEntity).getMetaTileEntity();
        return metaTileEntity != null ? metaTileEntity.getExportFluids() : null;
    }

    public static class DistillationRecipeLogic extends BasicRecipeLogic<IDistillationHandler> {

        public DistillationRecipeLogic(MetaTileEntity metaTileEntity) {
            super(metaTileEntity);
        }

        @Override
        protected boolean completeRecipe() {
            for (int i = this.itemOutputIndex; i < this.itemOutputs.size(); i++) {
                ItemStack stack = this.itemOutputs.get(i);
                if (this.voidingItems || ItemStackHelper.insertIntoItemHandler(this.handler.getExportItemInventory(), stack, true).isEmpty()) {
                    ItemStackHelper.insertIntoItemHandler(this.handler.getExportItemInventory(), stack, false);
                    this.itemOutputIndex++;
                } else return false;
            }
            for (int i = this.fluidOutputIndex; i < this.fluidOutputs.size(); i++) {
                FluidStack stack = this.fluidOutputs.get(i);
                IMultipleTankHandler fluidTank = this.handler.getOutputHatchAt(i);
                if (fluidTank == null)
                    fluidTank = VOID_TANK;
                if (this.voidingFluids || fluidTank.fill(stack, false) == stack.amount) {
                    fluidTank.fill(stack, true);
                    this.fluidOutputIndex++;
                } else return false;
            }
            this.recipeRecheck = true;
            this.itemOutputIndex = 0;
            this.fluidOutputIndex = 0;
            this.itemInputs.clear();
            this.itemOutputs.clear();
            this.fluidInputs.clear();
            this.fluidOutputs.clear();
            return true;
        }
    }
}
