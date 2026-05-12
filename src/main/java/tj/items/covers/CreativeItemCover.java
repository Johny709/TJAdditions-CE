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
import tj.gui.TJGuiTextures;
import tj.gui.widgets.NewTextFieldWidget;
import tj.gui.widgets.TJLabelWidget;
import tj.gui.widgets.impl.SelectionWidgetGroup;
import tj.gui.widgets.impl.TJPhantomItemSlotWidget;
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
    private long timer = 1L;

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

    @Override
    public ModularUI createUI(EntityPlayer player) {
        final WidgetGroup widgetGroup = new WidgetGroup(new Position(61, 25));
        final SelectionWidgetGroup selectionWidgetGroup = new SelectionWidgetGroup(61, 25, 54, 54);
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
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 187)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(this.getPickItem()).setLocale("cover.creative_item.title"))
                .widget(new ImageWidget(61, 80, 55, 18, GuiTextures.DISPLAY))
                .widget(new AdvancedTextWidget(63, 85, this::displayText, 0xFFFFFF))
                .widget(new ClickButtonWidget(43, 80, 18, 18, "+", this::onIncrement))
                .widget(new ClickButtonWidget(116, 80, 18, 18, "-", this::onDecrement))
                .widget(new ToggleButtonWidget(134, 80, 18, 18, TJGuiTextures.RESET_BUTTON, () -> false, this::onReset)
                        .setTooltipText("machine.universal.toggle.reset"))
                .widget(new ToggleButtonWidget(152, 80, 18, 18, TJGuiTextures.POWER_BUTTON, this::isWorkingEnabled, this::setWorkingEnabled)
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
        if (this.isWorking && ++this.timer % this.speed == 0) {
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

    private void displayText(List<ITextComponent> textList) {
        textList.add(new TextComponentTranslation("metaitem.creative.cover.display.ticks", this.speed));
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
