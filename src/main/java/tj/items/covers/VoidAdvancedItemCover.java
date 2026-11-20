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
import tj.integration.ae2.IItemConfigSetter;
import tj.mui.TJGuiTextures;
import tj.mui.TJGuiUtils;
import tj.mui.widgets.TJModularUI;
import tj.mui.widgets.impl.*;
import tj.textures.TJTextures;
import tj.util.TJItemUtils;

import java.util.regex.Pattern;


public class VoidAdvancedItemCover extends VoidItemCover implements IItemConfigSetter {

    private VoidMode voidMode = VoidMode.NORMAL;

    public VoidAdvancedItemCover(ICoverable coverHolder, EnumFacing attachedSide) {
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
        final WidgetGroup widgetGroup = new WidgetGroup(new Position(61, 8));
        final SelectionWidgetGroup selectionWidgetGroup = new SelectionWidgetGroup(61, 8, 54, 54);
        for (int i = 0; i < this.itemFilter.getSlots(); i++) {
            final int x = 18 * (i % 4);
            final int y = 18 * (i / 4);
            widgetGroup.addWidget(new TJPhantomItemSlotWidget(x, y, 18, 18, i, this.itemFilter, item -> {
                if (item.isEmpty()) return;
                this.itemType.put(item, item);
            }, this.itemType::remove).setPutItemsPredicate(item -> !this.itemType.containsKey(item))
                    .setBackgroundTextures(GuiTextures.SLOT));
            selectionWidgetGroup.addSelectionBox(i, x, y, 18, 18);
        }
        final PanelWidgetGroup panelWidgetGroup = TJGuiUtils.createTickPanel(this);
        return TJModularUI.builder(this, player)
                .setWidthHeight(200, 208)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(this.getPickItem()).setLocale("metaitem.void_advanced_item_cover.name"))
                .widget(new TJCycleButtonWidget<>(7, 84, 162, 18, VoidMode.class, () -> this.voidMode, this::setVoidMode)
                        .setBackgroundTextures(GuiTextures.VANILLA_BUTTON))
                .widget(new TJToggleButtonWidget(174, 126, 18, 18, TJGuiTextures.TOGGLE_RESET_BUTTON, () -> false, this::onReset)
                        .setTitleHoverTooltipText("machine.universal.toggle.reset.disabled"))
                .widget(new TJToggleButtonWidget(174, 184, 18, 18, TJGuiTextures.TOGGLE_POWER_BUTTON, () -> this.isWorking, this::setWorkingEnabled)
                        .setToggleTitleTooltipHoverText("machine.universal.toggle.run.mode.disabled",
                                "machine.universal.toggle.run.mode.enabled"))
                .widget(new NewTextFieldWidget<>(7, 103, 162, 18, true, () -> String.valueOf(this.tickTime),
                        (text, id) -> this.setTickTime(Long.parseLong(text)))
                        .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                        .setTooltipText("machine.universal.ticks.operation")
                        .setUpdateOnTyping(true)
                        .setMaxStringLength(11))
                .widget(new TJToggleButtonWidget(174, 103, 18, 18, panelWidgetGroup::isPanelOpen, (BooleanConsumer) value -> {
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
                .widget(TJGuiUtils.createItemConfigPanel(this, selectionWidgetGroup))
                .widget(panelWidgetGroup)
                .build();
    }

    @Override
    public void update() {
        if (this.isWorking && this.coverHolder.getOffsetTimer() % this.tickTime == 0) {
            if (this.voidMode == VoidMode.EXACT) {
                for (int i = 0; i < this.itemHandler.getSlots(); i++) {
                    final ItemStack stack = this.itemHandler.getStackInSlot(i);
                    if (stack.isEmpty()) continue;
                    final ItemStack filterStack = this.itemType.get(stack);
                    if (filterStack != null && stack.getCount() > filterStack.getCount())
                        this.itemHandler.extractItem(i, stack.getCount() - filterStack.getCount(), false);
                }
            } else for (int i = 0; i < this.itemHandler.getSlots(); i++) {
                final ItemStack stack = this.itemHandler.getStackInSlot(i);
                if (stack.isEmpty() || !this.itemType.containsKey(stack)) continue;
                this.itemHandler.extractItem(i, Integer.MAX_VALUE, false);
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
    public void setItemCount(long count, int index) {
        if (index < 0 || index >= this.itemFilter.getSlots()) return;
        final ItemStack stack = this.itemFilter.getStackInSlot(index);
        if (stack.isEmpty()) return;
        stack.setCount((int) Math.min(Integer.MAX_VALUE, count));
        this.itemType.put(stack, stack);
        this.markAsDirty();
    }

    @Override
    public int getItemCount(int index) {
        return index >= 0 && index < this.itemFilter.getSlots() ? this.itemFilter.getStackInSlot(index).getCount() : 0;
    }

    public void setVoidMode(VoidMode voidMode) {
        this.voidMode = voidMode;
        this.markAsDirty();
    }
}
