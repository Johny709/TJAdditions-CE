package tj.items.covers;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.util.Position;
import gregtech.api.util.function.BooleanConsumer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import tj.integration.ae2.IFluidConfigSetter;
import tj.mui.TJGuiTextures;
import tj.mui.TJGuiUtils;
import tj.mui.widgets.TJModularUI;
import tj.mui.widgets.impl.*;
import tj.textures.TJTextures;
import tj.util.TJItemUtils;

import java.util.regex.Pattern;

public class VoidAdvancedFluidCover extends VoidFluidCover implements IFluidConfigSetter {

    private VoidMode voidMode = VoidMode.NORMAL;

    public VoidAdvancedFluidCover(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
    }

    @Override
    public void renderCover(CCRenderState ccRenderState, Matrix4 matrix4, IVertexOperation[] iVertexOperations, Cuboid6 cuboid6, BlockRenderLayer blockRenderLayer) {
        super.renderCover(ccRenderState, matrix4, iVertexOperations, cuboid6, blockRenderLayer);
        final int oldBaseColor = ccRenderState.baseColour;
        final int oldAlphaOverride = ccRenderState.alphaOverride;

        ccRenderState.baseColour = 0x27FF00 << 8;
        ccRenderState.alphaOverride = 0xFF;

        TJTextures.INSIDE_OVERLAY_BASE.renderSided(this.attachedSide, cuboid6, ccRenderState, iVertexOperations, matrix4);

        ccRenderState.baseColour = oldBaseColor;
        ccRenderState.alphaOverride = oldAlphaOverride;
    }

    @Override
    public void onAttached(ItemStack itemStack) {
        super.onAttached(itemStack);
        final NBTTagCompound compound = itemStack.getOrCreateSubCompound("voidFilter");
        if (compound.hasKey("voidMode"))
            this.voidMode = VoidMode.values()[compound.getInteger("voidMode")];
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        final WidgetGroup widgetGroup = new WidgetGroup(new Position(61, 48));
        final SelectionWidgetGroup selectionWidgetGroup = new SelectionWidgetGroup(61, 48, 54, 54);
        for (int i = 0; i < this.fluidFilter.getTanks(); i++) {
            final int index = i;
            widgetGroup.addWidget(new TJPhantomFluidSlotWidget(18 * (i % 3), 18 * (i / 3), 18, 18, i, this.fluidFilter, fluid -> {
                if (fluid == null) return;
                this.fluidFilter.getTankAt(index).drain(Integer.MAX_VALUE, true);
                this.fluidFilter.getTankAt(index).fill(fluid, true);
                this.fluidType.put(fluid, fluid);
            }).setBackgroundTexture(GuiTextures.FLUID_SLOT));
            selectionWidgetGroup.addSelectionBox(i, 18 * (i % 3), 18 * (i / 3), 18, 18);
        }
        final PanelWidgetGroup panelWidgetGroup = TJGuiUtils.createTickPanel(this);
        return TJModularUI.builder(this, player)
                .setWidthHeight(200, 208)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(this.getPickItem()).setLocale("metaitem.void_advanced_item_cover.name"))
                .widget(new TJCycleButtonWidget<>(43, 106, 90, 18, VoidMode.class, () -> this.voidMode, this::setVoidMode).setBackgroundTextures(GuiTextures.VANILLA_BUTTON))
                .widget(new TJToggleButtonWidget(174, 105, 18, 18, TJGuiTextures.TOGGLE_RESET_BUTTON, () -> false, this::onReset)
                        .setTitleHoverTooltipText("machine.universal.toggle.reset.disabled"))
                .widget(new TJToggleButtonWidget(174, 162, 18, 18, TJGuiTextures.TOGGLE_POWER_BUTTON, () -> this.isWorking, this::setWorkingEnabled)
                        .setToggleTitleTooltipHoverText("machine.universal.toggle.run.mode.disabled",
                                "macine.universal.toggle.run.mode.enabled"))
                .widget(new NewTextFieldWidget<>(7, 82, 162, 18, true, () -> String.valueOf(this.tickTime),
                        (text, id) -> this.setTickTime(Long.parseLong(text)))
                        .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                        .setTooltipText("machine.universal.ticks.operation")
                        .setUpdateOnTyping(true)
                        .setMaxStringLength(11))
                .widget(new TJToggleButtonWidget(174, 82, 18, 18, panelWidgetGroup::isPanelOpen, (BooleanConsumer) value -> {
                    if (!value && panelWidgetGroup.isPanelOpen()) {
                        panelWidgetGroup.closePanel();
                    } else if (value) {
                        panelWidgetGroup.openPanel();
                    }
                }).setToggleTexture(GuiTextures.TOGGLE_BUTTON_BACK)
                        .setItemDisplay(TJItemUtils.getItemStackFromName("enderio:item_material", 1, 11)) // infinity bimetal gear
                        .setHoverTooltipText("tj.machine.universal.ticks.operation_configure")
                        .useToggleTexture(true))
                .widget(widgetGroup)
                .widget(selectionWidgetGroup)
                .widget(TJGuiUtils.createFluidConfigPanel(this, selectionWidgetGroup))
                .widget(panelWidgetGroup)
                .build();
    }

    @Override
    public void update() {
        if (this.isWorking && this.coverHolder.getOffsetTimer() % this.tickTime == 0) {
            if (this.voidMode == VoidMode.EXACT) {
                for (IFluidTankProperties fluidTankProperties : this.fluidHandler.getTankProperties()) {
                    final FluidStack fluidStack = fluidTankProperties.getContents();
                    if (fluidStack == null) continue;
                    final FluidStack filterStack = this.fluidType.get(fluidStack);
                    if (filterStack != null && fluidStack.amount > filterStack.amount) {
                        final FluidStack stack = filterStack.copy();
                        stack.amount = fluidStack.amount - filterStack.amount;
                        this.fluidHandler.drain(stack, true);
                    }
                }
            } else for (IFluidTankProperties fluidTankProperties : this.fluidHandler.getTankProperties()) {
                final FluidStack fluidStack = fluidTankProperties.getContents();
                if (fluidStack == null) continue;
                final FluidStack filterStack = this.fluidType.get(fluidStack);
                if (filterStack != null)
                    this.fluidHandler.drain(filterStack, true);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("voidMode", this.voidMode.ordinal());
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.voidMode = VoidMode.values()[tagCompound.getInteger("voidMode")];
    }

    private void onReset(boolean reset) {
        this.tickTime = 1;
        this.markAsDirty();
    }

    @Override
    public void setFluidCount(long count, int index) {
        if (index < 0 || index >= this.fluidFilter.getTanks()) return;
        final FluidStack fluidStack = this.fluidFilter.getTankAt(index).getFluid();
        if (fluidStack == null) return;
        fluidStack.amount = (int) Math.min(Integer.MAX_VALUE, count);
        this.fluidType.put(fluidStack, fluidStack);
        this.markAsDirty();
    }

    @Override
    public int getFluidCount(int index) {
        return index >= 0 && index < this.fluidFilter.getTanks() ? this.fluidFilter.getTankAt(index).getFluidAmount() : 0;
    }

    public void setVoidMode(VoidMode voidMode) {
        this.voidMode = voidMode;
        this.markAsDirty();
    }
}
