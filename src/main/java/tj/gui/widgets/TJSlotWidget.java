package tj.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.igredient.IIngredientSlot;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.GTLog;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.lwjgl.input.Keyboard;
import tj.util.ItemStackHelper;

import java.io.IOException;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class TJSlotWidget<R extends TJSlotWidget<R>> extends Widget implements ISlotHandler, IIngredientSlot {

    private final IItemHandler itemHandler;
    protected int slotIndex;
    private Supplier<IItemHandler> itemHandlerSupplier;
    protected BooleanSupplier takeItemsPredicate;
    protected BooleanSupplier putItemsPredicate;
    private TextureArea[] backgroundTexture;
    private ISlotGroup widgetGroup;
    private boolean simulating;

    @SideOnly(Side.CLIENT)
    private int simulatedAmount;

    @SideOnly(Side.CLIENT)
    private boolean isDragging;

    @SideOnly(Side.CLIENT)
    private boolean slotModified;

    public TJSlotWidget(IItemHandler itemHandler, int slotIndex, int x, int y) {
        super(new Position(x, y), new Size(18, 18));
        this.itemHandler = itemHandler;
        this.slotIndex = slotIndex;
    }

    public R setItemHandlerSupplier(Supplier<IItemHandler> itemHandlerSupplier) {
        this.itemHandlerSupplier = itemHandlerSupplier;
        return (R) this;
    }

    public R setWidgetGroup(ISlotGroup widgetGroup) {
        this.widgetGroup = widgetGroup;
        return (R) this;
    }

    public R setTakeItemsPredicate(BooleanSupplier takeItemsPredicate) {
        this.takeItemsPredicate = takeItemsPredicate;
        return (R) this;
    }

    public R setPutItemsPredicate(BooleanSupplier putItemsPredicate) {
        this.putItemsPredicate = putItemsPredicate;
        return (R) this;
    }

    public R setBackgroundTexture(TextureArea... backgroundTexture) {
        this.backgroundTexture = backgroundTexture;
        return (R) this;
    }

    protected IItemHandler getItemHandler() {
        return this.itemHandlerSupplier != null ? this.itemHandlerSupplier.get() : this.itemHandler;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInForeground(int mouseX, int mouseY) {
        ItemStack stack;
        if (this.isMouseOverElement(mouseX, mouseY) && this.getItemHandler() != null && !(stack = this.getItemHandler().getStackInSlot(this.slotIndex)).isEmpty()) {
            List<String> tooltip = getItemToolTip(stack);
            String itemStoredText = I18n.format("gregtech.item_list.item_stored", stack.getCount());
            tooltip.add(TextFormatting.GRAY + itemStoredText);
            drawHoveringText(stack, tooltip, -1, mouseX, mouseY);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        Position pos = this.getPosition();
        int stackX = pos.getX() + 1;
        int stackY = pos.getY() + 1;
        if (this.backgroundTexture != null)
            for (TextureArea textureArea : this.backgroundTexture) {
                textureArea.draw(pos.getX(), pos.getY(), 18, 18);
            }
        if (this.getItemHandler() != null) {
            ItemStack stack = this.getItemHandler().getStackInSlot(this.slotIndex);
            if (!stack.isEmpty()) {
                drawItemStack(stack, stackX, stackY, null);
            }
            if (this.simulating || this.isMouseOverElement(mouseX, mouseY))
                drawSelectionOverlay(stackX, stackY, 16, 16);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        this.isDragging = true;
        if (this.isMouseOverElement(mouseX, mouseY)) {
            this.slotModified = true;
            if (this.widgetGroup == null || this.widgetGroup.getTimer() < 1) {
                boolean isCtrlKeyPressed = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
                boolean isShiftKeyPressed = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
                this.writeClientAction(1, buffer -> {
                    buffer.writeBoolean(isCtrlKeyPressed);
                    buffer.writeBoolean(isShiftKeyPressed);
                    buffer.writeInt(button);
                });
            } else {
                this.writeClientAction(3, buffer -> buffer.writeInt(64 - this.gui.entityPlayer.inventory.getItemStack().getCount()));
                return false;
            }
            if (button == 0 && this.widgetGroup != null && !this.gui.entityPlayer.inventory.getItemStack().isEmpty())
                this.widgetGroup.addSlotToDrag(this, () -> {
                    this.simulating = true;
                    this.writeClientAction(4, buffer -> buffer.writeBoolean(this.simulating));
                });
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        if (this.isDragging && this.isMouseOverElement(mouseX, mouseY)) {
            if (!this.slotModified) {
                this.slotModified = true;
                if (button == 0 && this.widgetGroup != null)
                    this.widgetGroup.addSlotToDrag(this, () -> {
                        this.simulating = true;
                        this.writeClientAction(4, buffer -> buffer.writeBoolean(this.simulating));
                    });
                this.writeClientAction(2, buffer -> buffer.writeInt(button));
            }
        } else this.slotModified = false;
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseReleased(int mouseX, int mouseY, int button) {
        this.isDragging = false;
        return false;
    }

    @Override
    public void detectAndSendChanges() {
        if (!this.simulating && this.getItemHandler() != null)
            this.writeUpdateInfo(1, buffer -> buffer.writeItemStack(this.getItemHandler().getStackInSlot(this.slotIndex)));
    }

    @Override
    public ItemStack insert(ItemStack stack, boolean simulate) {
        if (this.getItemHandler() == null || this.getItemHandler().getSlots() <= this.slotIndex)
            return stack;
        ItemStack inventoryStack = this.getItemHandler().getStackInSlot(this.slotIndex);
        if (inventoryStack.isEmpty() || inventoryStack.isItemEqual(stack) && ItemStack.areItemStackShareTagsEqual(inventoryStack, stack))
            return this.getItemHandler().insertItem(this.slotIndex, stack, simulate);
        else return stack;
    }

    @Override
    public ItemStack extract(int amount, ItemStack stack, boolean simulate) {
        if (this.getItemHandler() == null || this.getItemHandler().getSlots() <= this.slotIndex)
            return ItemStack.EMPTY;
        ItemStack inventoryStack = this.getItemHandler().getStackInSlot(this.slotIndex);
        if (inventoryStack.isEmpty() || inventoryStack.isItemEqual(stack) && ItemStack.areItemStackShareTagsEqual(inventoryStack, stack))
            return this.getItemHandler().extractItem(this.slotIndex, amount, simulate);
        return ItemStack.EMPTY;
    }

    private void insertAmount(ItemStack stack, int amount) {
        ItemStack oneStack = stack.copy();
        oneStack.setCount(amount);
        stack.shrink(amount - this.insert(oneStack, false).getCount());
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        EntityPlayer player = this.gui.entityPlayer;
        ItemStack handStack = player.inventory.getItemStack();
        ItemStack newStack = handStack;
        switch (id) {
            case 1:
                boolean isCtrlKeyPressed = buffer.readBoolean();
                boolean isShiftKeyPressed = buffer.readBoolean();
                int button = buffer.readInt();
                if (button == 0) {
                    if (handStack.isEmpty())
                        if (this.getItemHandler() != null && (this.takeItemsPredicate == null || this.takeItemsPredicate.getAsBoolean())) {
                            int amount = isCtrlKeyPressed ? Integer.MAX_VALUE : 64;
                            newStack = this.getItemHandler().extractItem(this.slotIndex, amount, false);
                            if (isShiftKeyPressed)
                                newStack = ItemStackHelper.insertInMainInventory(player.inventory, newStack);
                            if (this.widgetGroup != null)
                                this.writeUpdateInfo(3, buffer1 -> buffer1.writeInt(5));
                        } else return;
                    else if (this.widgetGroup == null && (this.putItemsPredicate == null || this.putItemsPredicate.getAsBoolean()))
                        // if this slot was not added to a slot group then let this slot handle the stack insertion
                        newStack = this.insert(handStack, false);
                    else return;
                } else if (button == 1) {
                    if (handStack.isEmpty()) {
                        if (this.getItemHandler() != null && (this.takeItemsPredicate == null || this.takeItemsPredicate.getAsBoolean())) {
                            ItemStack stack = this.getItemHandler().getStackInSlot(this.slotIndex);
                            newStack = this.getItemHandler().extractItem(this.slotIndex, Math.max(1, stack.getCount() / 2), false);
                        } else return;
                    } else {
                        if (this.putItemsPredicate == null || this.putItemsPredicate.getAsBoolean()) {
                            this.insertAmount(handStack, 1);
                        } else return;
                    }
                } else if (button == 2) {
                    if (this.getItemHandler() != null && handStack.isEmpty() && player.isCreative()) {
                        newStack = this.getItemHandler().getStackInSlot(this.slotIndex).copy();
                        newStack.setCount(64);
                    }
                }
                break;
            case 2:
                int button1 = buffer.readInt();
                if (button1 == 1)
                    this.insertAmount(newStack, 1);
                break;
            case 3:
                int amount = buffer.readInt();
                if (this.getItemHandler() != null)
                    newStack = ItemStackHelper.extractFromItemHandler(this.getItemHandler(), newStack, amount, false);
                break;
            case 4:
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
        switch (id) {
            case 1:
                try {
                    ItemStack stack = buffer.readItemStack();
                    if (this.getItemHandler() instanceof IItemHandlerModifiable)
                        ((IItemHandlerModifiable) this.getItemHandler()).setStackInSlot(this.slotIndex, stack);
                } catch (IOException e) {
                    GTLog.logger.error(e);
                }
                break;
            case 2:
                try {
                    this.gui.entityPlayer.inventory.setItemStack(buffer.readItemStack());
                } catch (IOException e) {
                    GTLog.logger.error(e);
                }
                break;
            case 3:
                if (this.widgetGroup != null)
                    this.widgetGroup.setTimer(buffer.readInt());
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
        return this.getItemHandler() != null ? this.getItemHandler().getStackInSlot(this.slotIndex) : null;
    }
}
