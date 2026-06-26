package tj.machines.multi.steam;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.gui.Widget;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.Textures;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import gregtech.common.blocks.BlockFireboxCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.builder.multicontrollers.GUIDisplayBuilder;
import tj.builder.multicontrollers.TJMultiblockControllerBase;
import tj.capability.IProgressBar;
import tj.capability.ProgressBar;
import tj.capability.impl.handler.ICoilHandler;
import tj.capability.impl.workable.MultiSmelterWorkableHandler;
import tj.mui.TJGuiTextures;
import tj.mui.widgets.impl.TJToggleButtonWidget;
import tj.textures.TJTextures;
import tj.util.EnumFacingHelper;
import tj.util.TJFluidUtils;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.UnaryOperator;

import static gregicadditions.capabilities.GregicAdditionsCapabilities.*;
import static gregtech.common.blocks.BlockFireboxCasing.ACTIVE;

public class MetaTileEntitySteamOven extends TJMultiblockControllerBase implements ICoilHandler, IProgressBar {

    private final SteamOvenWorkableHandler workableHandler = new SteamOvenWorkableHandler(this);

    public MetaTileEntitySteamOven(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, false);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder metaTileEntityHolder) {
        return new MetaTileEntitySteamOven(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.multiblock.temporary"));
    }

    @Override
    protected boolean checkStructureComponents(List<IMultiblockPart> parts, Map<MultiblockAbility<Object>, List<Object>> abilities) {
        return abilities.containsKey(STEAM) && super.checkStructureComponents(parts, abilities);
    }

    @Override
    protected boolean shouldUpdate(MTETrait trait) {
        return false;
    }

    @Override
    protected void updateFormedValid() {
        if (((this.getProblems() >> 5) & 1) != 0)
            this.workableHandler.update();
    }

    @Override
    protected void mainDisplayTab(List<Widget> widgetGroup) {
        super.mainDisplayTab(widgetGroup);
        if (!this.hasDistinct()) return;
        widgetGroup.add(new TJToggleButtonWidget(175, 151, 18, 18, TJGuiTextures.TOGGLE_DISTINCT_BUTTON, this.workableHandler::isDistinct, this.workableHandler::setDistinct)
                .setToggleTitleTooltipHoverText("machine.universal.toggle.distinct.mode.disabled", "machine.universal.toggle.distinct.mode.enabled"));
    }

    @Override
    protected void addDisplayText(GUIDisplayBuilder builder) {
        super.addDisplayText(builder);
        if (!this.isStructureFormed()) return;
        builder.addVoltageInLine(this.inputEnergyContainer)
                .addVoltageTierLine(1)
                .addFluidInputLine(this.importFluidTank, this.workableHandler.getSteam())
                .addParallelLine(this.workableHandler.getParallelsPerformed(), this.getParallel())
                .addIsWorkingLine(this.workableHandler.isWorkingEnabled(), this.workableHandler.isActive(), this.workableHandler.getProgress(), this.workableHandler.getMaxProgress(), this.workableHandler.hasProblem(), 998)
                .addRecipeInputLine(this.workableHandler, 999)
                .addRecipeOutputLine(this.workableHandler, 1000);
        if (this.hasDistinct())
            builder.addDistinctLine(this.workableHandler.isDistinct(), 997);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("FFF", "XXX", "~C~")
                .aisle("FFF", "X#X", "~C~")
                .aisle("FFF", "XSX", "~C~")
                .where('S', this.selfPredicate())
                .where('C', statePredicate(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.BRONZE_BRICKS)))
                .where('X', statePredicate(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.BRONZE_BRICKS))
                        .or(abilityPartPredicate(STEAM_IMPORT_ITEMS, STEAM_EXPORT_ITEMS)))
                .where('F', statePredicate(GTUtility.getAllPropertyValues(MetaBlocks.BOILER_FIREBOX_CASING.getState(BlockFireboxCasing.FireboxCasingType.BRONZE_FIREBOX), ACTIVE))
                        .or(abilityPartPredicate(STEAM)))
                .where('#', isAirPredicate())
                .where('~', tile -> true)
                .build();
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.importFluidTank = new FluidTankList(true, this.getAbilities(STEAM));
        this.importItemInventory = new ItemHandlerList(this.getAbilities(STEAM_IMPORT_ITEMS));
        this.exportItemInventory = new ItemHandlerList(this.getAbilities(STEAM_EXPORT_ITEMS));
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.importFluidTank = new FluidTankList(true);
        this.importItemInventory = new ItemHandlerList(Collections.emptyList());
        this.exportItemInventory = new ItemHandlerList(Collections.emptyList());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.ELECTRIC_FURNACE_OVERLAY.render(renderState, translation, pipeline, this.getFrontFacing(), this.workableHandler.isActive());
        TJTextures.TJ_LOGO.renderSided(EnumFacingHelper.getLeftFacingFrom(this.getFrontFacing()), renderState, translation, pipeline);
        TJTextures.TJ_LOGO.renderSided(EnumFacingHelper.getRightFacingFrom(this.getFrontFacing()), renderState, translation, pipeline);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return !(sourcePart instanceof IMultiblockAbilityPart<?>) || ((IMultiblockAbilityPart<?>) sourcePart).getAbility() != STEAM ? Textures.BRONZE_PLATED_BRICKS : this.workableHandler.isActive() ? Textures.BRONZE_FIREBOX_ACTIVE : Textures.BRONZE_FIREBOX;
    }

    @Override
    public int getLightValueForPart(IMultiblockPart sourcePart) {
        return !(sourcePart instanceof IMultiblockAbilityPart<?>) || ((IMultiblockAbilityPart<?>) sourcePart).getAbility() != STEAM ? 0 : this.workableHandler.isActive() ? 15 : 0;
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
    public int getParallel() {
        return 8;
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        return RecipeMaps.FURNACE_RECIPES;
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.workableHandler.isWorkingEnabled();
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {
        this.workableHandler.setWorkingEnabled(isActivationAllowed);
    }

    @Override
    public int[][] getBarMatrix() {
        return new int[1][1];
    }

    @Override
    public void getProgressBars(Queue<UnaryOperator<ProgressBar.ProgressBarBuilder>> bars) {
        bars.add(bar -> bar.setProgress(this::getSteamAmount).setMaxProgress(this::getSteamCapacity)
                .setLocale("tj.multiblock.bars.fluid").setParams(() -> new String[]{this.workableHandler.getSteam() != null ? this.workableHandler.getSteam().getLocalizedName() : ""})
                .setFluidStackSupplier(this.workableHandler::getSteam));
    }

    private long getSteamAmount() {
        return TJFluidUtils.getFluidAmountFromTanks(this.workableHandler.getSteam(), this.importFluidTank);
    }

    private long getSteamCapacity() {
        return TJFluidUtils.getFluidCapacityFromTanks(this.workableHandler.getSteam(), this.importFluidTank);
    }

    private static class SteamOvenWorkableHandler extends MultiSmelterWorkableHandler {

        private FluidStack steam;

        public SteamOvenWorkableHandler(MetaTileEntity metaTileEntity) {
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

        @Override
        public int getDuration() {
            return 192;
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
