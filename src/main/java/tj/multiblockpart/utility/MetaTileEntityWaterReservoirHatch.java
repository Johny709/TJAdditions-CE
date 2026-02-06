package tj.multiblockpart.utility;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregicadditions.client.ClientHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.unification.material.Materials;
import gregtech.common.metatileentities.electric.multiblockpart.MetaTileEntityMultiblockPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.TJValues;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.TJLabelWidget;
import tj.gui.widgets.TJProgressBarWidget;
import tj.textures.TJTextures;
import tj.util.TJFluidUtils;

import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityWaterReservoirHatch extends MetaTileEntityMultiblockPart implements IMultiblockAbilityPart<IFluidTank> {

    private final FluidTank fluidTank = new FluidTank(Materials.Water.getFluid(Integer.MAX_VALUE), Integer.MAX_VALUE) {
        @Nullable
        @Override
        public FluidStack drainInternal(int maxDrain, boolean doDrain) {
            return new FluidStack(this.fluid, maxDrain);
        }
    };

    public MetaTileEntityWaterReservoirHatch(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        this.initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder metaTileEntityHolder) {
        return new MetaTileEntityWaterReservoirHatch(this.metaTileEntityId, this.getTier());
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        return new FluidTankList(false, this.fluidTank);
    }

    @Override
    protected ModularUI createUI(EntityPlayer player) {
        return ModularUI.defaultBuilder()
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL)
                        .setItemLabel(this.getStackForm()).setLocale(this.getMetaFullName()))
                .widget(new TJProgressBarWidget(4, 4, 168, 80, this::getWaterAmount, this::getWaterCapacity, true, ProgressWidget.MoveType.VERTICAL)
                        .setLocale("tj.multiblock.bars.fluid", () -> new Object[]{this.fluidTank.getFluid().getLocalizedName()})
                        .setTexture(GuiTextures.FLUID_SLOT)
                        .setFluid(this.fluidTank::getFluid)
                        .setInverted(true))
                .bindPlayerInventory(player.inventory)
                .build(this.getHolder(), player);
    }

    private long getWaterAmount() {
        return TJFluidUtils.getFluidAmountFromTanks(this.fluidTank.getFluid(), this.getImportFluids());
    }

    private long getWaterCapacity() {
        return TJFluidUtils.getFluidCapacityFromTanks(this.fluidTank.getFluid(), this.getImportFluids());
    }

    @Override
    public MultiblockAbility<IFluidTank> getAbility() {
        return MultiblockAbility.IMPORT_FLUIDS;
    }

    @Override
    public void registerAbilities(List<IFluidTank> abilityList) {
        abilityList.addAll(this.importFluids.getFluidTanks());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.getController() == null) {
            int oldBaseColor = renderState.baseColour;
            int oldAlphaOverride = renderState.alphaOverride;

            renderState.baseColour = TJValues.VC[10] << 8;
            renderState.alphaOverride = 0xFF;

            for (EnumFacing facing : EnumFacing.VALUES)
                TJTextures.SUPER_HATCH_OVERLAY.renderSided(facing, renderState, translation, pipeline);

            renderState.baseColour = oldBaseColor;
            renderState.alphaOverride = oldAlphaOverride;
        }
        ClientHandler.COVER_INFINITE_WATER.renderSided(getFrontFacing(), renderState, translation, pipeline);
    }
}
