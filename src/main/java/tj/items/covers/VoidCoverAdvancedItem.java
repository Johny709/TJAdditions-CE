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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.NewTextFieldWidget;
import tj.gui.widgets.TJCycleButtonWidget;
import tj.gui.widgets.TJLabelWidget;
import tj.gui.widgets.impl.SelectionWidgetGroup;
import tj.gui.widgets.impl.TJPhantomItemSlotWidget;
import tj.textures.TJTextures;

import java.util.regex.Pattern;

public class VoidCoverAdvancedItem extends VoidCoverItem {

    private VoidMode voidMode = VoidMode.NORMAL;

    public VoidCoverAdvancedItem(ICoverable coverHolder, EnumFacing attachedSide) {
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
        for (int i = 0; i < this.itemFilter.getSlots(); i++) {
            final int index = i;
            widgetGroup.addWidget(new TJPhantomItemSlotWidget(18 * (i % 3), 18 * (i / 3), 18, 18, i, this.itemFilter)
                    .setBackgroundTextures(GuiTextures.SLOT));
            selectionWidgetGroup.addSubWidget(i, new NewTextFieldWidget<>(0, -20, 54, 18, true, () -> String.valueOf(this.itemFilter.getStackInSlot(index).getCount()), (text, id) -> {
                ItemStack stack = this.itemFilter.extractItem(index, Integer.MAX_VALUE, true);
                if (stack.isEmpty()) return;
                stack = this.itemFilter.extractItem(index, Integer.MAX_VALUE, false);
                stack.setCount(Math.max(1, (int) Math.min(Integer.MAX_VALUE, Long.parseLong(text))));
                this.itemFilter.insertItem(index, stack, false);
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
        ItemStack stack, stack1;
        for (int i = 0; i < this.itemHandler.getSlots(); i++) {
            for (int j = 0; j < this.itemFilter.getSlots(); j++) {
                if ((stack = this.itemHandler.getStackInSlot(i)).isItemEqual(stack1 = this.itemFilter.getStackInSlot(j))) {
                    switch (this.voidMode) {
                        case NORMAL: this.itemHandler.extractItem(i, Integer.MAX_VALUE, false);
                            break;
                        case SUPPLY: this.itemHandler.extractItem(i, stack1.getCount(), false);
                            break;
                        case EXACT:
                            if (stack.getCount() > stack1.getCount())
                                this.itemHandler.extractItem(i, stack.getCount() - stack1.getCount(), false);
                    }
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
