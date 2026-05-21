package tj.machines.multi.steam;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.Textures;
import gregtech.api.unification.material.Materials;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import tj.builder.multicontrollers.GUIDisplayBuilder;
import tj.builder.multicontrollers.TJRecipeMapMultiblockController;
import tj.capability.IProgressBar;
import tj.capability.ProgressBar;
import tj.capability.impl.handler.IRecipeHandler;
import tj.capability.impl.workable.BasicRecipeLogic;
import tj.util.TJFluidUtils;

import java.util.Queue;
import java.util.function.UnaryOperator;

import static gregicadditions.capabilities.GregicAdditionsCapabilities.*;

public class MetaTileEntitySteamGrinder extends TJRecipeMapMultiblockController implements IProgressBar {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {STEAM, STEAM_IMPORT_ITEMS, STEAM_EXPORT_ITEMS};

    public MetaTileEntitySteamGrinder(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.MACERATOR_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntitySteamGrinder(this.metaTileEntityId);
    }

    @Override
    protected BasicRecipeLogic<? extends IRecipeHandler> createRecipeLogic() {
        return new SteamGrinderWorkableHandler(this);
    }

    @Override
    protected void addDisplayText(GUIDisplayBuilder builder) {
        super.addDisplayText(builder);
        if (!this.isStructureFormed()) return;
        builder.addFluidInputLine(this.importFluidTank, ((SteamGrinderWorkableHandler) this.recipeLogic).getSteam());
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("CCC", "CCC", "CCC")
                .aisle("CCC", "C#C", "CCC")
                .aisle("CCC", "CSC", "CCC")
                .where('S', this.selfPredicate())
                .where('C', statePredicate(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.BRONZE_BRICKS)).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('#', isAirPredicate())
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.BRONZE_PLATED_BRICKS;
    }

    @Override
    public int getParallel() {
        return 8;
    }

    @Override
    public int getTier() {
        return 1;
    }

    @Override
    public long getMaxVoltage() {
        return 32;
    }

    @Override
    public int[][] getBarMatrix() {
        return new int[1][1];
    }

    @Override
    public void getProgressBars(Queue<UnaryOperator<ProgressBar.ProgressBarBuilder>> bars) {
        final FluidStack fluidStack = ((SteamGrinderWorkableHandler) this.recipeLogic).getSteam();
        bars.add(bar -> bar.setProgress(this::getSteamAmount).setMaxProgress(this::getSteamCapacity)
                .setLocale("tj.multiblock.bars.fluid").setParams(() -> new Object[]{fluidStack != null ? fluidStack.getLocalizedName() : ""})
                .setFluidStackSupplier(((SteamGrinderWorkableHandler) this.recipeLogic)::getSteam));
    }

    private long getSteamAmount() {
        return TJFluidUtils.getFluidAmountFromTanks(((SteamGrinderWorkableHandler) this.recipeLogic).getSteam(), this.importFluidTank);
    }

    private long getSteamCapacity() {
        return TJFluidUtils.getFluidCapacityFromTanks(((SteamGrinderWorkableHandler) this.recipeLogic).getSteam(), this.importFluidTank);
    }

    private static class SteamGrinderWorkableHandler extends BasicRecipeLogic<IRecipeHandler> {

        private FluidStack steam;

        public SteamGrinderWorkableHandler(MetaTileEntity metaTileEntity) {
            super(metaTileEntity);
        }

        @Override
        protected boolean startRecipe() {
            boolean canStart = super.startRecipe();
            if (canStart)
                this.steam = Materials.Steam.getFluid((int) (this.energyPerTick * 2));
            return canStart;
        }

        @Override
        protected void progressRecipe(int progress) {
            if (this.steam == null || this.steam.isFluidStackIdentical(this.handler.getImportFluidTank().drain(this.steam, true))) {
                this.progress++;
            } else if (this.progress > 1)
                this.progress--;
        }

        public FluidStack getSteam() {
            return this.steam;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            final NBTTagCompound compound = super.serializeNBT();
            if (this.steam != null)
                compound.setTag("fluidStack", this.steam.writeToNBT(new NBTTagCompound()));
            return compound;
        }

        @Override
        public void deserializeNBT(NBTTagCompound compound) {
            super.deserializeNBT(compound);
            if (compound.hasKey("fluidStack"))
                this.steam = FluidStack.loadFluidStackFromNBT(compound.getCompoundTag("fluidStack"));
        }
    }
}
