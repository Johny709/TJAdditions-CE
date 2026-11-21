package tj.mui.widgets.impl;

import gregtech.api.gui.INativeWidget;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.igredient.IIngredientSlot;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.lwjgl.input.Keyboard;
import tj.TJ;
import tj.mui.TJGuiUtils;
import tj.mui.widgets.ISlotGroup;
import tj.mui.widgets.ISlotHandler;
import tj.mui.widgets.TJWidget;
import tj.util.TJItemUtils;
import tj.util.TooltipHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class TJSlotWidget<R extends TJSlotWidget<R>> extends TJWidget<R> implements ISlotHandler, IIngredientSlot, INativeWidget{

    private final IItemHandler itemHandler;
    private final TJSlotItemHandler slotItemHandler;
    protected SlotLocationInfo slotLocationInfo = new SlotLocationInfo(false, false);
    private Supplier<IItemHandler> itemHandlerSupplier;
    protected Predicate<ItemStack> takeItemsPredicate;
    protected Predicate<ItemStack> putItemsPredicate;
    protected TextureArea[] activeBackgroundTexture;
    protected TextureArea[] inactiveBackgroundTexture;
    protected ISlotGroup slotGroup;
    protected NBTTagCompound compound;
    protected boolean simulating;
    protected int slotIndex;
    protected int itemCount;

    @Nonnull
    protected ItemStack itemStack = ItemStack.EMPTY;

    @SideOnly(Side.CLIENT)
    private int simulatedAmount;

    @SideOnly(Side.CLIENT)
    private boolean slotModified;

    public TJSlotWidget(IItemHandler itemHandler, int slotIndex, int x, int y) {
        super(new Position(x, y), new Size(18, 18));
        this.itemHandler = itemHandler;
        this.slotIndex = slotIndex;
        this.slotItemHandler = new TJSlotItemHandler(this.itemHandler, slotIndex, x, y);
    }

    public TJSlotWidget(Supplier<IItemHandler> itemHandlerSupplier, int slotIndex, int x, int y) {
        super(new Position(x, y), new Size(18, 18));
        this.itemHandler = itemHandlerSupplier.get();
        this.slotIndex = slotIndex;
        this.slotItemHandler = new TJSlotItemHandler(itemHandlerSupplier, slotIndex, x, y);
        this.itemHandlerSupplier = itemHandlerSupplier;
    }

    public R setSlotGroup(ISlotGroup slotGroup) {
        this.slotGroup = slotGroup;
        return this.thisWidget();
    }

    public R setTakeItemsPredicate(Predicate<ItemStack> takeItemsPredicate) {
        this.takeItemsPredicate = takeItemsPredicate;
        return this.thisWidget();
    }

    public R setPutItemsPredicate(Predicate<ItemStack> putItemsPredicate) {
        this.putItemsPredicate = putItemsPredicate;
        return this.thisWidget();
    }

    public R setActiveBackgroundTexture(TextureArea... activeBackgroundTexture) {
        this.activeBackgroundTexture = activeBackgroundTexture;
        return this.thisWidget();
    }

    public R setInactiveBackgroundTexture(TextureArea... inactiveBackgroundTexture) {
        this.inactiveBackgroundTexture = inactiveBackgroundTexture;
        return this.thisWidget();
    }

    public R setSlotLocationInfo(boolean isPlayerInventory, boolean isHotbarSlot) {
        this.slotLocationInfo = new SlotLocationInfo(isPlayerInventory, isHotbarSlot);
        return this.thisWidget();
    }

    @Override
    public void initWidget() {
        super.initWidget();
        this.slotItemHandler.xPos = this.getPosition().getX() + 1;
        this.slotItemHandler.yPos = this.getPosition().getY() + 1;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInForeground(int mouseX, int mouseY) {
        if (!this.isActive) return;
        if (!this.itemStack.isEmpty() && this.isMouseOverElement(mouseX, mouseY) && this.getItemHandler() != null) {
            final List<String> tooltips = getItemToolTip(this.itemStack);
            final String itemStoredText = I18n.format("gregtech.item_list.item_stored", this.itemCount);
            tooltips.add(TextFormatting.GRAY + itemStoredText);
            TooltipHelper.shiftText(tooltips, this::addToHoverTooltip);
            this.drawHoveringText(this.itemStack, tooltips, -1, mouseX, mouseY);
        }
    }

    @SideOnly(Side.CLIENT)
    protected void addToHoverTooltip(List<String> tooltips) {
        tooltips.add(I18n.format("tj.machine.universal.slot.description"));
        tooltips.add(""); // add empty line
        tooltips.add(I18n.format("tj.machine.universal.slot.left_click"));
        tooltips.add(""); // add empty line
        tooltips.add(I18n.format("tj.machine.universal.slot.left_click_empty"));
        tooltips.add(""); // add empty line
        tooltips.add(I18n.format("tj.machine.universal.slot.shift_left_click"));
        tooltips.add(""); // add empty line
        tooltips.add(I18n.format("tj.machine.universal.slot.right_click"));
        tooltips.add(""); // add empty line
        tooltips.add(I18n.format("tj.machine.universal.slot.middle_click"));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        final Position pos = this.getPosition();
        if (this.isActive && this.activeBackgroundTexture != null) {
            for (TextureArea textureArea : this.activeBackgroundTexture) {
                textureArea.draw(pos.x, pos.y, 18, 18);
            }
        } else if (this.inactiveBackgroundTexture != null) for (TextureArea textureArea : this.inactiveBackgroundTexture) {
            textureArea.draw(pos.x, pos.y, 18, 18);
        }
        if (this.isActive) {
            if (!this.itemStack.isEmpty() || this.simulating) {
                TJGuiUtils.drawItemStack(pos.x, pos.y,
                        this.simulating ? this.getItemHandler().getStackInSlot(this.slotIndex) : this.itemStack,
                        this.simulating ? this.getItemHandler().getStackInSlot(this.slotIndex).getCount() : this.itemCount,
                        null);
            }
            if (this.simulating || this.isMouseOverElement(mouseX, mouseY))
                drawSelectionOverlay(pos.x + 1, pos.y + 1, 16, 16);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (!this.isActive)
            return false;
        if (this.isMouseOverElement(mouseX, mouseY)) {
            this.slotModified = true;
            if (this.slotGroup == null || this.slotGroup.getTimer() < 1) {
                final boolean isCtrlKeyPressed = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
                final boolean isShiftKeyPressed = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
                this.writeClientAction(1, buffer -> {
                    buffer.writeBoolean(isCtrlKeyPressed);
                    buffer.writeBoolean(isShiftKeyPressed);
                    buffer.writeInt(button);
                });
            } else {
                this.writeClientAction(3, buffer -> buffer.writeInt(64 - this.gui.entityPlayer.inventory.getItemStack().getCount()));
                return false;
            }
            if (button == 0 && this.slotGroup != null && !this.gui.entityPlayer.inventory.getItemStack().isEmpty())
                this.slotGroup.addSlotToDrag(this, () -> {
                    this.simulating = true;
                    this.writeClientAction(4, buffer -> buffer.writeBoolean(this.simulating));
                });
            return true;
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        if (this.isActive && this.isMouseOverElement(mouseX, mouseY)) {
            if (!this.slotModified) {
                this.slotModified = true;
                if (!this.gui.entityPlayer.inventory.getItemStack().isEmpty() || (this.slotGroup != null && this.slotGroup.isDragging())) {
                    if (button == 0 && this.slotGroup != null) {
                        this.slotGroup.addSlotToDrag(this, () -> {
                            this.simulating = true;
                            this.writeClientAction(4, buffer -> buffer.writeBoolean(this.simulating));
                        });
                    }
                    if (button == 1)
                        this.writeClientAction(2, buffer -> buffer.writeInt(button));
                } else if (button == 0) {
                    this.writeClientAction(1, buffer -> {
                        buffer.writeBoolean(false);
                        buffer.writeBoolean(true);
                        buffer.writeInt(button);
                    });
                }
            }
        } else this.slotModified = false;
        return false;
    }

    @Override
    public ItemStack insert(ItemStack stack, boolean simulate) {
        if (this.getItemHandler() == null || this.getItemHandler().getSlots() <= this.slotIndex)
            return stack;
        return this.getItemHandler().insertItem(this.slotIndex, stack, simulate);
    }

    @Override
    public ItemStack extract(int amount, ItemStack stack, boolean simulate) {
        if (this.getItemHandler() == null || this.getItemHandler().getSlots() <= this.slotIndex)
            return ItemStack.EMPTY;
        return this.getItemHandler().extractItem(this.slotIndex, amount, simulate);
    }

    private void insertAmount(ItemStack stack, int amount) {
        final ItemStack oneStack = stack.copy();
        oneStack.setCount(amount);
        stack.shrink(amount - this.insert(oneStack, false).getCount());
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (this.getItemHandler() == null || this.simulating) return;
        final ItemStack itemStack = this.getItemHandler().getStackInSlot(this.slotIndex);
        if (!itemStack.isItemEqual(this.itemStack)) {
            this.itemStack = itemStack;
            this.writeUpdateInfo(1, buffer -> buffer.writeItemStack(this.itemStack));
        }
        final int itemCount = itemStack.getCount();
        if (this.itemCount != itemCount) {
            this.itemCount = itemCount;
            this.writeUpdateInfo(4, buffer -> buffer.writeInt(this.itemCount));
        }
        if (itemStack.getTagCompound() != null && !itemStack.getTagCompound().equals(this.compound)) {
            this.compound = itemStack.getTagCompound();
            this.itemStack.setTagCompound(this.compound);
            this.writeUpdateInfo(5, buffer -> buffer.writeCompoundTag(this.compound));
        }
        if (itemStack.getTagCompound() == null && this.compound != null) {
            this.compound = null;
            this.itemStack.setTagCompound(null);
            this.writeUpdateInfo(6, buffer -> {});
        }
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        final EntityPlayer player = this.gui.entityPlayer;
        final ItemStack handStack = player.inventory.getItemStack();
        ItemStack newStack = handStack;
        switch (id) {
            case 1:
                final boolean isCtrlKeyPressed = buffer.readBoolean();
                final boolean isShiftKeyPressed = buffer.readBoolean();
                final int button = buffer.readInt();
                if (!this.isActive) break;
                if (button == 0) {
                    if (handStack.isEmpty()) {
                        if (this.getItemHandler() != null) {
                            final int amount = isCtrlKeyPressed ? Integer.MAX_VALUE : 64;
                            if (this.takeItemsPredicate != null &&
                                    !this.takeItemsPredicate.test(this.getItemHandler().extractItem(this.slotIndex, amount, true))) return;
                            newStack = this.getItemHandler().extractItem(this.slotIndex, amount, false);
                            if (this.slotGroup != null) {
                                if (isShiftKeyPressed) {
                                    if (this.slotGroup.getItemStackTransfer() != null)
                                        newStack = this.slotGroup.getItemStackTransfer().apply(newStack);
                                } else this.writeUpdateInfo(3, buffer1 -> buffer1.writeInt(5));
                            }
                            if (isShiftKeyPressed)
                                newStack = TJItemUtils.insertInMainInventory(player.inventory, newStack);
                        } else break;
                    } else if (this.slotGroup == null && (this.putItemsPredicate == null || this.putItemsPredicate.test(handStack))) {
                        // if this slot was not added to a slot group then let this slot handle the stack insertion
                        newStack = TJItemUtils.insertOrSwap(this.itemHandler, this.slotIndex, handStack);
                    } else break;
                } else if (button == 1) {
                    if (handStack.isEmpty()) {
                        if (this.getItemHandler() != null) {
                            final ItemStack stack = this.getItemHandler().getStackInSlot(this.slotIndex);
                            if (this.takeItemsPredicate == null || this.takeItemsPredicate.test(stack))
                                newStack = this.getItemHandler().extractItem(this.slotIndex, Math.max(1, stack.getCount() / 2), false);
                        } else break;
                    } else {
                        if (this.putItemsPredicate == null || this.putItemsPredicate.test(handStack)) {
                            this.insertAmount(handStack, 1);
                        } else break;
                    }
                } else if (button == 2) {
                    if (this.getItemHandler() != null && handStack.isEmpty() && player.isCreative()) {
                        newStack = this.getItemHandler().getStackInSlot(this.slotIndex).copy();
                        newStack.setCount(64);
                    }
                }
                break;
            case 2:
                final int button1 = buffer.readInt();
                if (this.isActive && button1 == 1)
                    this.insertAmount(newStack, 1);
                break;
            case 3:
                final int amount = buffer.readInt();
                if (this.isActive && this.getItemHandler() != null)
                    newStack = TJItemUtils.extractFromItemHandler(this.getItemHandler(), newStack, amount, false);
                break;
            case 4:
                if (this.isActive)
                    this.simulating = buffer.readBoolean();
                return;
        }
        final ItemStack finalStack = newStack;
        player.inventory.setItemStack(finalStack);
        this.writeUpdateInfo(2, buffer1 -> buffer1.writeItemStack(finalStack));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        try {
            switch (id) {
                case 1:
                    this.itemStack = buffer.readItemStack();
                    this.itemStack.setCount(1);
                    break;
                case 2:
                    this.gui.entityPlayer.inventory.setItemStack(buffer.readItemStack());
                    break;
                case 3:
                    if (this.slotGroup != null)
                        this.slotGroup.setTimer(buffer.readInt());
                    break;
                case 4:
                    this.itemCount = buffer.readInt();
                    break;
                case 5:
                    this.compound = buffer.readCompoundTag();
                    this.itemStack.setTagCompound(this.compound);
                    break;
                case 6:
                    this.compound = null;
                    this.itemStack.setTagCompound(null);
            }
        } catch (IOException e) {
            TJ.logger.info(e);
        }
    }

    @Override
    public int index() {
        return this.slotIndex;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onRemove() {
        this.simulatedAmount = 0;
        this.simulating = false;
        this.writeClientAction(4, buffer -> buffer.writeBoolean(this.simulating));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setSimulatedAmount(int amount) {
        this.simulatedAmount = amount;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getSimulatedAmount() {
        return this.simulatedAmount;
    }

    @Override
    public Object getIngredientOverMouse(int mouseX, int mouseY) {
        if (!this.isMouseOverElement(mouseX, mouseY))
            return null;
        return this.itemStack.isEmpty() ? null : this.itemStack;
    }

    @Override
    public void setEnabled(boolean b) {}

    public void forceUpdate() {
        this.itemStack = ItemStack.EMPTY;
        this.itemCount = 0;
        this.compound = null;
    }

    @Override
    public SlotLocationInfo getSlotLocationInfo() {
        return this.slotLocationInfo;
    }

    @Override
    public boolean canMergeSlot(ItemStack itemStack) {
        return this.isActive;
    }

    @Override
    public ItemStack slotClick(int i, ClickType clickType, EntityPlayer entityPlayer) {
        return ItemStack.EMPTY;
    }

    @Override
    public Slot getHandle() {
        return this.slotItemHandler;
    }

    protected IItemHandler getItemHandler() {
        return this.itemHandlerSupplier != null ? this.itemHandlerSupplier.get() : this.itemHandler;
    }

    private static class TJSlotItemHandler extends SlotItemHandler {

        private Supplier<IItemHandler> itemHandlerSupplier;

        public TJSlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        public TJSlotItemHandler(Supplier<IItemHandler> itemHandlerSupplier, int index, int xPosition, int yPosition) {
            super(itemHandlerSupplier.get(), index, xPosition, yPosition);
            this.itemHandlerSupplier = itemHandlerSupplier;
        }

        @Override
        public boolean isItemValid(@Nonnull ItemStack stack) {
            final int count = stack.getCount();
            return this.getItemHandler().insertItem(this.getSlotIndex(), stack, true).getCount() != count;
        }

        @Override
        public boolean canTakeStack(EntityPlayer playerIn) {
            return false;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public boolean isEnabled() {
            return false;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void setBackgroundLocation(@Nonnull ResourceLocation texture) {}

        @Nullable
        @Override
        @SideOnly(Side.CLIENT)
        public TextureAtlasSprite getBackgroundSprite() {
            return null;
        }

        @Override
        public IItemHandler getItemHandler() {
            return this.itemHandlerSupplier != null ? this.itemHandlerSupplier.get() : super.getItemHandler();
        }
    }
}
