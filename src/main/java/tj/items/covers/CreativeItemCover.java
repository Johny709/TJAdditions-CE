package tj.items.covers;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.IControllable;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.*;
import gregtech.api.util.Position;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import tj.mui.TJGuiTextures;
import tj.mui.widgets.ButtonWidget;
import tj.mui.widgets.impl.NewTextFieldWidget;
import tj.mui.widgets.impl.TJLabelWidget;
import tj.mui.widgets.impl.SelectionWidgetGroup;
import tj.mui.widgets.impl.TJPhantomItemSlotWidget;
import tj.items.handlers.LargeItemStackHandler;
import tj.textures.TJTextures;
import tj.util.TJItemUtils;

import java.util.List;
import java.util.regex.Pattern;


public class CreativeItemCover extends CoverBehavior implements CoverWithUI, ITickable, IControllable {

    private final LargeItemStackHandler itemFilter = new LargeItemStackHandler(9, Integer.MAX_VALUE);
    private final IItemHandler itemHandler;
    private boolean isWorking;
    private int speed = 1;

    public CreativeItemCover(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
        this.itemHandler = this.coverHolder.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
    }

    @Override
    public boolean canAttach() {
        return this.coverHolder.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, this.attachedSide) != null;
    }

    @Override
    public void renderCover(CCRenderState ccRenderState, Matrix4 matrix4, IVertexOperation[] iVertexOperations, Cuboid6 cuboid6, BlockRenderLayer blockRenderLayer) {
        TJTextures.COVER_CREATIVE_FLUID.renderSided(this.attachedSide, cuboid6, ccRenderState, iVertexOperations, matrix4);
    }

    @Override
    public EnumActionResult onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, CuboidRayTraceResult hitResult) {
        if (!playerIn.world.isRemote) {
            this.openUI((EntityPlayerMP) playerIn);
        }
        return EnumActionResult.SUCCESS;
    }

    private void displayText(List<ITextComponent> textList) {
        textList.add(new TextComponentTranslation("metaitem.creative.cover.display.ticks", this.speed));
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        final WidgetGroup widgetGroup = new WidgetGroup(new Position(61, 25));
        final SelectionWidgetGroup selectionWidgetGroup = new SelectionWidgetGroup(61, 25, 54, 54);
        final ButtonWidget<?> clickButtonDivide = new ButtonWidget<>(-54, -20, 18, 18, "/2", data -> this.setItemCount(String.valueOf(Long.parseLong(this.getItemCount(selectionWidgetGroup.getIndex())) / 2), String.valueOf(selectionWidgetGroup.getIndex())));
        final ButtonWidget<?> clickButtonMultiply = new ButtonWidget<>(90, -20, 18, 18, "*2", data -> this.setItemCount(String.valueOf(Long.parseLong(this.getItemCount(selectionWidgetGroup.getIndex())) * 2), String.valueOf(selectionWidgetGroup.getIndex())));
        final NewTextFieldWidget<?> itemCountTextField = new NewTextFieldWidget<>(-35, -20, 124, 18, true, null, this::setItemCount)
                .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                .setUpdateOnTyping(true)
                .setMaxStringLength(11);
        itemCountTextField.setTextSupplier(() -> this.getItemCount((int) itemCountTextField.getTextIdLong()));
        selectionWidgetGroup.setIndexListener(itemCountTextField::setTextIdLong);
        for (int i = 0; i < this.itemFilter.getSlots(); i++) {
            widgetGroup.addWidget(new TJPhantomItemSlotWidget(18 * (i % 3), 18 * (i / 3), 18, 18, i, this.itemFilter)
                    .setBackgroundTextures(GuiTextures.SLOT));
            selectionWidgetGroup.addSubWidget(i, clickButtonDivide.setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
            selectionWidgetGroup.addSubWidget(i, clickButtonMultiply.setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
            selectionWidgetGroup.addSubWidget(i, itemCountTextField);
            selectionWidgetGroup.addSelectionBox(i, 18 * (i % 3), 18 * (i / 3), 18, 18);
        }
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 187)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(this.getPickItem()).setLocale("cover.creative_item.title"))
                .widget(new ImageWidget(61, 80, 55, 18, GuiTextures.DISPLAY))
                .widget(new AdvancedTextWidget(63, 85, this::displayText, 0xFFFFFF))
                .widget(new ButtonWidget<>(43, 80, 18, 18, "+", this::onIncrement).setBackgroundTextures(GuiTextures.VANILLA_BUTTON))
                .widget(new ButtonWidget<>(116, 80, 18, 18, "-", this::onDecrement).setBackgroundTextures(GuiTextures.VANILLA_BUTTON))
                .widget(new ToggleButtonWidget(134, 80, 18, 18, TJGuiTextures.TOGGLE_RESET_BUTTON, () -> false, this::onReset)
                        .setTooltipText("machine.universal.toggle.reset"))
                .widget(new ToggleButtonWidget(152, 80, 18, 18, TJGuiTextures.TOGGLE_POWER_BUTTON, this::isWorkingEnabled, this::setWorkingEnabled)
                        .setTooltipText("machine.universal.toggle.run.mode"))
                .widget(widgetGroup)
                .widget(selectionWidgetGroup)
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 105)
                .build(this, player);
    }

    @Override
    public void onAttached(ItemStack itemStack) {
        final NBTTagCompound compound = itemStack.getOrCreateSubCompound("init");
        if (compound.hasKey("speed"))
            this.speed = compound.getInteger("speed");
        if (compound.hasKey("power"))
            this.isWorking = compound.getBoolean("power");
        for (int i = 0; i < 9; i++) {
            if (compound.hasKey("slot:" + i))
                this.itemFilter.setStackInSlot(i, new ItemStack(compound.getCompoundTag("slot:" + i)));
        }
    }

    @Override
    public void update() {
        if (this.isWorking && this.coverHolder.getOffsetTimer() % this.speed == 0) {
            for (int i = 0; i < 9; i++) {
                final ItemStack filterStack = this.itemFilter.getStackInSlot(i).copy();
                TJItemUtils.insertIntoItemHandler(this.itemHandler, filterStack, false);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("Speed", this.speed);
        data.setTag("itemFilter", this.itemFilter.serializeNBT());
        data.setBoolean("IsWorking", this.isWorking);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.itemFilter.deserializeNBT(data.getCompoundTag("itemFilter"));
        this.isWorking = data.getBoolean("IsWorking");
        if (data.hasKey("Speed"))
            this.speed = data.getInteger("Speed");
    }

    private void setItemCount(String text, String id) {
        final int index = Integer.parseInt(id);
        if (index < 0 || index >= this.itemFilter.getSlots()) return;
        final ItemStack stack = this.itemFilter.getStackInSlot(index);
        if (stack.isEmpty()) return;
        stack.setCount((int) Math.min(Integer.MAX_VALUE, Long.parseLong(text)));
        this.markAsDirty();
    }

    private String getItemCount(int index) {
        return String.valueOf(this.itemFilter.getStackInSlot(index).getCount());
    }

    @Override
    public void setWorkingEnabled(boolean isWorking) {
        this.isWorking = isWorking;
        this.markAsDirty();
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.isWorking;
    }

    private void onReset(boolean reset) {
        this.speed = 1;
        this.markAsDirty();
    }

    private void onIncrement(Widget.ClickData clickData) {
        int value = clickData.isCtrlClick ? 100
                : clickData.isShiftClick ? 10
                : 1;
        this.speed = MathHelper.clamp(this.speed +value, 1, Integer.MAX_VALUE);
        this.markAsDirty();
    }

    private void onDecrement(Widget.ClickData clickData) {
        int value = clickData.isCtrlClick ? 100
                : clickData.isShiftClick ? 10
                : 1;
        this.speed = MathHelper.clamp(this.speed -value, 1, Integer.MAX_VALUE);
        this.markAsDirty();
    }
}
