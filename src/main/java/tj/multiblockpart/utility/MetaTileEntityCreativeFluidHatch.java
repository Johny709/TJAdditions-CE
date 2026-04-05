package tj.multiblockpart.utility;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregicadditions.GAValues;
import gregicadditions.machines.multi.multiblockpart.GAMetaTileEntityMultiblockPart;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.render.Textures;
import gregtech.api.util.Position;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.TJValues;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.NewTextFieldWidget;
import tj.gui.widgets.TJLabelWidget;
import tj.gui.widgets.impl.SelectionWidgetGroup;
import tj.gui.widgets.impl.TJPhantomFluidSlotWidget;
import tj.textures.TJTextures;

import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class MetaTileEntityCreativeFluidHatch extends GAMetaTileEntityMultiblockPart {

    private FluidTankList ghostTanks;

    public MetaTileEntityCreativeFluidHatch(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GAValues.MAX);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityCreativeFluidHatch(this.metaTileEntityId);
    }

    @Override
    public void update() {
        super.update();
        if (!this.getWorld().isRemote) {
            for (int i = 0; i < this.importFluids.getTanks(); i++) {
                final IFluidTank tank = this.importFluids.getTankAt(i);
                tank.drain(Integer.MAX_VALUE, true);
                tank.fill(this.ghostTanks.getTankAt(i).getFluid(), true);
            }
        }
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.ghostTanks = new FluidTankList(true, IntStream.range(0, 16)
                .mapToObj(i -> new FluidTank(Integer.MAX_VALUE) {
                    @Override
                    public FluidStack drain(FluidStack resource, boolean doDrain) {
                        return this.getFluid();
                    }

                    @Override
                    public FluidStack drain(int maxDrain, boolean doDrain) {
                        return this.getFluid();
                    }
                }).collect(Collectors.toList()));
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        return new FluidTankList(true, IntStream.range(0, 16)
                .mapToObj(i -> new FluidTank(Integer.MAX_VALUE))
                .collect(Collectors.toList()));
    }

    @Override
    protected ModularUI createUI(EntityPlayer player) {
        final int tier = Math.min(3, this.getTier() / 3);
        final WidgetGroup widgetGroup = new WidgetGroup(new Position(43, 24));
        final SelectionWidgetGroup selectionWidgetGroup = new SelectionWidgetGroup(43, 24, 72, 72);
        for (int i = 0; i < this.ghostTanks.getTanks(); i++) {
            final int finalI = i;
            final int x = (tier == 3 ? 18 : 0) + 18 * (i % (tier + 1));
            final int y = 18 * (i / (tier + 1));
            widgetGroup.addWidget(new TJPhantomFluidSlotWidget(x, y, 18, 18, () -> this.ghostTanks.getTankAt(finalI).getFluid(), fluidStack -> {
                if (fluidStack != null) {
                    this.ghostTanks.getTankAt(finalI).drain(Integer.MAX_VALUE, true);
                    this.ghostTanks.getTankAt(finalI).fill(fluidStack, true);
                }
            }).setBackgroundTexture(GuiTextures.FLUID_SLOT));
            selectionWidgetGroup.addSubWidget(i, new NewTextFieldWidget<>(21, -14, 72, 18, () -> String.valueOf(this.ghostTanks.getTankAt(finalI).getFluidAmount()), (text, id) -> {
                FluidStack fluidStack = this.ghostTanks.getTankAt(Integer.parseInt(id)).getFluid();
                if (fluidStack != null) {
                    fluidStack = fluidStack.copy();
                    fluidStack.amount = (int) Math.min(Integer.MAX_VALUE, Long.parseLong(text));
                    this.ghostTanks.getTankAt(finalI).drain(Integer.MAX_VALUE, true);
                    this.ghostTanks.getTankAt(finalI).fill(fluidStack, true);
                }
            }).setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                    .setTextId(String.valueOf(i))
                    .setUpdateOnTyping(true)
                    .setMaxStringLength(11)
                    .enableBackground(true));
            selectionWidgetGroup.addSelectionBox(i, x, y, 18, 18);
        }
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 196, 148 + 18 * (tier - 1))
                .widget(new TJLabelWidget(7, -19, 180, 19, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(this.getStackForm()).setLocale(this.getMetaFullName()))
                .widget(widgetGroup)
                .widget(selectionWidgetGroup)
                .bindPlayerInventory(player.inventory, 63 + 18 * (tier - 1))
                .build(this.getHolder(), player);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.getController() == null) {
            final int oldBaseColor = renderState.baseColour;
            final int oldAlphaOverride = renderState.alphaOverride;

            renderState.baseColour = TJValues.VC[this.getTier() - 2] << 8; // TODO get better MAX color overlay. use UMV color overlay for the time being
            renderState.alphaOverride = 0xFF;

            for (EnumFacing facing : EnumFacing.VALUES)
                TJTextures.SUPER_HATCH_OVERLAY.renderSided(facing, renderState, translation, pipeline);

            renderState.baseColour = oldBaseColor;
            renderState.alphaOverride = oldAlphaOverride;
        }
        Textures.PIPE_IN_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        Textures.FLUID_HATCH_INPUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("ghostTanks", this.ghostTanks.serializeNBT());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.ghostTanks.deserializeNBT(data.getCompoundTag("ghostTanks"));
    }
}
