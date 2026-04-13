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
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.render.Textures;
import gregtech.api.util.Position;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
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

import javax.annotation.Nullable;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class MetaTileEntityCreativeFluidHatch extends GAMetaTileEntityMultiblockPart implements IMultiblockAbilityPart<IFluidTank> {

    public MetaTileEntityCreativeFluidHatch(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GAValues.MAX);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityCreativeFluidHatch(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("cover.creative.only"));
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        return new FluidTankList(true, IntStream.range(0, 16)
                .mapToObj(i -> new FluidTank(Integer.MAX_VALUE) {
                    @Override
                    public FluidStack drain(FluidStack resource, boolean doDrain) {
                        FluidStack fluidStack = this.getFluid();
                        if (fluidStack == null || !fluidStack.isFluidEqual(resource)) return null;
                        fluidStack = fluidStack.copy();
                        fluidStack.amount = Math.min(fluidStack.amount, resource.amount);
                        return fluidStack;
                    }

                    @Override
                    public FluidStack drain(int maxDrain, boolean doDrain) {
                        if (maxDrain == Integer.MIN_VALUE)
                            super.drain(Integer.MAX_VALUE, doDrain);
                        FluidStack fluidStack = this.getFluid();
                        if (fluidStack == null) return null;
                        fluidStack = fluidStack.copy();
                        fluidStack.amount = Math.min(fluidStack.amount, maxDrain);
                        return fluidStack;
                    }
                }).collect(Collectors.toList()));
    }

    @Override
    protected ModularUI createUI(EntityPlayer player) {
        final WidgetGroup widgetGroup = new WidgetGroup(new Position(43, 24));
        final SelectionWidgetGroup selectionWidgetGroup = new SelectionWidgetGroup(43, 24, 72, 72);
        for (int i = 0; i < this.importFluids.getTanks(); i++) {
            final int finalI = i;
            final int x = 18 + 18 * (i % 4);
            final int y = 18 * (i / 4);
            widgetGroup.addWidget(new TJPhantomFluidSlotWidget(x, y, 18, 18, i, this.importFluids, fluidStack -> {})
                    .setBackgroundTexture(GuiTextures.FLUID_SLOT));
            selectionWidgetGroup.addSubWidget(i, new NewTextFieldWidget<>(21, -14, 72, 18, () -> String.valueOf(this.importFluids.getTankAt(finalI).getFluidAmount()), (text, id) -> {
                final FluidStack fluidStack = this.importFluids.getTankAt(finalI).getFluid();
                if (fluidStack != null) {
                    fluidStack.amount = (int) Math.min(Integer.MAX_VALUE, Long.parseLong(text));
                }
            }).setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                    .setUpdateOnTyping(true)
                    .setMaxStringLength(11)
                    .enableBackground(true));
            selectionWidgetGroup.addSelectionBox(i, x, y, 18, 18);
        }
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 196, 184)
                .widget(new TJLabelWidget(7, -19, 180, 19, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(this.getStackForm()).setLocale(this.getMetaFullName()))
                .widget(widgetGroup)
                .widget(selectionWidgetGroup)
                .bindPlayerInventory(player.inventory, 100)
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
    public MultiblockAbility<IFluidTank> getAbility() {
        return MultiblockAbility.IMPORT_FLUIDS;
    }

    @Override
    public void registerAbilities(List<IFluidTank> list) {
        list.addAll(this.getImportFluids().getFluidTanks());
    }
}
