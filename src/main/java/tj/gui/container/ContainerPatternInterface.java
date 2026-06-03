package tj.gui.container;

import appeng.helpers.IInterfaceHost;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraftforge.items.IItemHandler;

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

        final IItemHandler upgradeHandler = this.interfaceHost.getInventoryByName("upgrades");
        final IInventory upgradeInventory = new InventoryBasic("me.upgrades", true, upgradeHandler.getSlots());
        for (int i = 0; i < upgradeHandler.getSlots(); i++) {
            this.addSlotToContainer(new Slot(upgradeInventory, i, 187, startX + (18 * i)));
        }
    }

    @Override
    public boolean canInteractWith(@Nonnull EntityPlayer playerIn) {
        return true;
    }
}
