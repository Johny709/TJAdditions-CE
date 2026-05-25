package tj.items.covers;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.CycleButtonWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.util.Position;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.NewTextFieldWidget;
import tj.gui.widgets.TJLabelWidget;
import tj.gui.widgets.impl.SelectionWidgetGroup;
import tj.gui.widgets.impl.TJPhantomFluidSlotWidget;
import tj.textures.TJTextures;

import java.util.regex.Pattern;

public class VoidAdvancedFluidCover extends VoidFluidCover {

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
        final WidgetGroup widgetGroup = new WidgetGroup(new Position(63, 48));
        final SelectionWidgetGroup selectionWidgetGroup = new SelectionWidgetGroup(63, 48, 54, 54);
        for (int i = 0; i < this.fluidFilter.getTanks(); i++) {
            final int index = i;
            widgetGroup.addWidget(new TJPhantomFluidSlotWidget(18 * (i % 3), 18 * (i / 3), 18, 18, i, this.fluidFilter, fluid -> {
                if (fluid == null) return;
                this.fluidFilter.getTankAt(index).drain(Integer.MAX_VALUE, true);
                this.fluidFilter.getTankAt(index).fill(fluid, true);
                this.fluidType.put(fluid, fluid);
            }).setBackgroundTexture(GuiTextures.FLUID_SLOT));
            selectionWidgetGroup.addSubWidget(i, new NewTextFieldWidget<>(-37, -20, 124, 18, true, () -> String.valueOf(this.fluidFilter.getTankAt(index).getFluidAmount()), this::setFluidCount)
                    .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                    .setTextId(String.valueOf(index))
                    .setUpdateOnTyping(true)
                    .setMaxStringLength(11));
            selectionWidgetGroup.addSubWidget(i, new ClickButtonWidget(-56, -20, 18, 18, "/2", data -> this.setFluidCount(String.valueOf((long) this.fluidFilter.getTankAt(index).getFluidAmount() / 2), String.valueOf(index))));
            selectionWidgetGroup.addSubWidget(i, new ClickButtonWidget(88, -20, 18, 18, "*2", data -> this.setFluidCount(String.valueOf((long) this.fluidFilter.getTankAt(index).getFluidAmount() * 2), String.valueOf(index))));
            selectionWidgetGroup.addSelectionBox(i, 18 * (i % 3), 18 * (i / 3), 18, 18);
        }
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 208)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(this.getPickItem()).setLocale("metaitem.void_advanced_item_cover.name"))
                .widget(new NewTextFieldWidget<>(26, 7, 124, 18, true, () -> String.valueOf(this.tickTime), this::setTickTime)
                        .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                        .setTooltipText("machine.universal.ticks.operation")
                        .setUpdateOnTyping(true))
                .widget(new ClickButtonWidget(7, 7, 18, 18, "/2", data -> this.setTickTime(String.valueOf((long) this.tickTime / 2), "")))
                .widget(new ClickButtonWidget(151, 7, 18, 18, "*2", data -> this.setTickTime(String.valueOf((long) this.tickTime * 2), "")))
                .widget(new CycleButtonWidget(43, 106, 90, 18, VoidMode.class, () -> this.voidMode, this::setVoidMode))
                .widget(new ToggleButtonWidget(151, 106, 18, 18, TJGuiTextures.POWER_BUTTON, () -> this.isWorking, this::setWorking)
                        .setTooltipText("machine.universal.toggle.run.mode"))
                .widget(widgetGroup)
                .widget(selectionWidgetGroup)
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 126)
                .build(this, player);
    }

    @Override
    public void update() {
        if (this.isWorking && this.coverHolder.getOffsetTimer() % this.tickTime == 0) {
            switch (this.voidMode) {
                case SUPPLY:
                    for (IFluidTankProperties fluidTankProperties : this.fluidHandler.getTankProperties()) {
                        final FluidStack fluidStack = fluidTankProperties.getContents();
                        if (fluidStack == null) continue;
                        final FluidStack filterStack = this.fluidType.get(fluidStack);
                        if (filterStack == null) continue;
                        this.fluidHandler.drain(filterStack, true);
                    }
                    break;
                case EXACT:
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
                    break;
                default:
                    for (IFluidTankProperties fluidTankProperties : this.fluidHandler.getTankProperties()) {
                        final FluidStack fluidStack = fluidTankProperties.getContents();
                        if (fluidStack == null) continue;
                        final FluidStack filterStack = this.fluidType.get(fluidStack);
                        if (filterStack != null)
                            this.fluidHandler.drain(filterStack, true);
                    }
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

    public void setFluidCount(String text, String id) {
        final int index = Integer.parseInt(id);
        FluidStack stack = this.fluidFilter.getTankAt(index).drain(Integer.MAX_VALUE, false);
        if (stack == null) return;
        stack = this.fluidFilter.getTankAt(index).drain(Integer.MAX_VALUE, true);
        if (stack == null) return;
        stack.amount = Math.max(1, (int) Math.min(Integer.MAX_VALUE, Long.parseLong(text)));
        this.fluidFilter.getTankAt(index).fill(stack, true);
        this.fluidType.put(stack, stack);
        this.markAsDirty();
    }

    public void setVoidMode(VoidMode voidMode) {
        this.voidMode = voidMode;
        this.markAsDirty();
    }
}
