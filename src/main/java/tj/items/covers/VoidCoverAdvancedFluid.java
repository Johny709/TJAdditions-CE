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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.NewTextFieldWidget;
import tj.gui.widgets.TJCycleButtonWidget;
import tj.gui.widgets.TJLabelWidget;
import tj.gui.widgets.impl.SelectionWidgetGroup;
import tj.gui.widgets.impl.TJPhantomFluidSlotWidget;
import tj.textures.TJTextures;

import java.util.regex.Pattern;

public class VoidCoverAdvancedFluid extends VoidCoverFluid {

    private VoidMode voidMode = VoidMode.NORMAL;

    public VoidCoverAdvancedFluid(ICoverable coverHolder, EnumFacing attachedSide) {
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
    public ModularUI createUI(EntityPlayer player) {
        final WidgetGroup widgetGroup = new WidgetGroup(new Position(63, 27));
        final SelectionWidgetGroup selectionWidgetGroup = new SelectionWidgetGroup(63, 27, 54, 54);
        for (int i = 0; i < this.fluidFilter.getTanks(); i++) {
            final int index = i;
            widgetGroup.addWidget(new TJPhantomFluidSlotWidget(18 * (i % 3), 18 * (i / 3), 18, 18, i, this.fluidFilter, null)
                    .setBackgroundTexture(GuiTextures.FLUID_SLOT));
            selectionWidgetGroup.addSubWidget(i, new NewTextFieldWidget<>(0, -20, 54, 18, true, () -> String.valueOf(this.fluidFilter.getTankAt(index).getFluidAmount()), (text, id) -> {
                FluidStack stack = this.fluidFilter.getTankAt(index).drain(Integer.MAX_VALUE, false);
                if (stack == null) return;
                stack = this.fluidFilter.getTankAt(index).drain(Integer.MAX_VALUE, true);
                stack.amount = Math.max(1, (int) Math.min(Integer.MAX_VALUE, Long.parseLong(text)));
                this.fluidFilter.getTankAt(index).fill(stack, true);
            }).setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                    .setUpdateOnTyping(true)
                    .setMaxStringLength(11));
            selectionWidgetGroup.addSelectionBox(i, 18 * (i % 3), 18 * (i / 3), 18, 18);
        }
        widgetGroup.addWidget(new TJCycleButtonWidget(0, 54, 54, 54, VoidMode.class, () -> this.voidMode, this::setVoidMode)
                .setToggle(true)
                .setButtonTexture(GuiTextures.TOGGLE_BUTTON_BACK));
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 105 + 82)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(this.getPickItem()).setLocale("metaitem.void_advanced_item_cover.name"))
                .widget(widgetGroup)
                .widget(selectionWidgetGroup)
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 105)
                .build(this, player);
    }

    @Override
    public void update() {
        for (int i = 0; i < 9; i++) {
            final FluidStack stack = this.fluidFilter.getTankAt(i).getFluid();
            if (stack == null) continue;
            switch (this.voidMode) {
                case NORMAL:
                case SUPPLY:
                    this.fluidHandler.drain(stack, true);
                    break;
                case EXACT:
                    final FluidStack stack1 = this.fluidHandler.drain(stack, false);
                    if (stack1 != null && stack1.amount > stack.amount) {
                        stack1.amount -= stack.amount;
                        this.fluidHandler.drain(stack1, true);
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

    public void setVoidMode(VoidMode voidMode) {
        this.voidMode = voidMode;
        this.markAsDirty();
    }
}
