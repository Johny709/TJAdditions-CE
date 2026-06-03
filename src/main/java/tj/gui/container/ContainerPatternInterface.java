package tj.gui.container;

import appeng.helpers.IInterfaceHost;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import tj.util.TJItemUtils;

import javax.annotation.Nonnull;


public class ContainerPatternInterface extends Container {

    private final InventoryPlayer inventoryPlayer;
    private final IInterfaceHost interfaceHost;

    public ContainerPatternInterface(InventoryPlayer inventoryPlayer, IInterfaceHost interfaceHost) {
        this.inventoryPlayer = inventoryPlayer;
        this.interfaceHost = interfaceHost;

        final int startX = 8;
        final int startY = 210;

        // Player inventory
        for (int i = 0; i < 27; i++) {
            this.addSlotToContainer(new Slot(this.inventoryPlayer, i + 9, startX + (18 * (i % 9)), startY + (18 * (i / 9))));
        }

        // Player hotbar
        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(this.inventoryPlayer, i, startX + (18 * (i % 9)), 58 + startY));
        }

        // upgrades
        final IItemHandler upgrades = this.interfaceHost.getInventoryByName("upgrades");
        for (int i = 0; i < upgrades.getSlots(); i++) {
            this.addSlotToContainer(new SlotItemHandler(upgrades, i, 187, startX + (18 * i)));
        }

        // storage
        final IItemHandler storage = this.interfaceHost.getInterfaceDuality().getStorage();
        for (int i = 0; i < storage.getSlots(); i++) {
            this.addSlotToContainer(new SlotItemHandler(storage, i, startX + (18 * (i % 9)), 53 + (18 * (i / 9))));
        }

        // patterns
        final IItemHandler patterns = this.interfaceHost.getInterfaceDuality().getPatterns();
        for (int i = 0; i < patterns.getSlots(); i++) {
            this.addSlotToContainer(new SlotItemHandler(patterns, i, startX + (18 * (i % 9)), 110 + (18 * (i / 9))));
        }
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(@Nonnull EntityPlayer playerIn, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        final Slot slot = this.getSlot(index);
        if (slot.getHasStack()) {
            itemStack = slot.getStack().copy();
            final int count = itemStack.getCount();
            final int inventorySize = playerIn.inventory.mainInventory.size();
            if (index < inventorySize) {
                final IItemHandler upgrades = this.interfaceHost.getInterfaceDuality().getInventoryByName("upgrades");
                final IItemHandler patterns = this.interfaceHost.getInterfaceDuality().getPatterns();
                final IItemHandler storage = this.interfaceHost.getInterfaceDuality().getStorage();
                if ((itemStack = TJItemUtils.insertIntoItemHandler(upgrades, itemStack, false)).getCount() == count) {
                    if ((itemStack = TJItemUtils.insertIntoItemHandler(patterns, itemStack, false)).getCount() == count)
                        if ((itemStack = TJItemUtils.insertIntoItemHandler(storage, itemStack, false)).getCount() == count)
                            return ItemStack.EMPTY;
                }
            } else if (!mergeItemStack(itemStack, 0, inventorySize, false)) {
                return ItemStack.EMPTY;
            }
            slot.putStack(itemStack);
        }
        return itemStack;
    }

    @Override
    public boolean canInteractWith(@Nonnull EntityPlayer playerIn) {
        return true;
    }
}
