package tj.gui.container;

import appeng.api.config.LevelType;
import appeng.api.config.OperationMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import tj.TJ;
import tj.mui.TJGuiUtils;
import tj.network.PacketToggleButtonPress;
import tj.util.TJItemUtils;

import javax.annotation.Nonnull;


public class ContainerPatternInterface extends Container {

    private final InventoryPlayer inventoryPlayer;
    private final IInterfaceHost interfaceHost;
    private boolean blockingMode;
    private boolean interfaceTerminal;
    private boolean sendFluid;
    private boolean splittingItemsFluids;

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

    public void readClientPacket(PacketToggleButtonPress packetToggleButtonPress) {
        switch (packetToggleButtonPress.id) {
            case 0: this.interfaceHost.getInterfaceDuality().getConfigManager().putSetting(Settings.BLOCK, packetToggleButtonPress.value ? YesNo.YES : YesNo.NO);
                break;
            case 1: this.interfaceHost.getInterfaceDuality().getConfigManager().putSetting(Settings.INTERFACE_TERMINAL, packetToggleButtonPress.value ? YesNo.YES : YesNo.NO);
                break;
            case 2: this.interfaceHost.getInterfaceDuality().getConfigManager().putSetting(Settings.OPERATION_MODE, packetToggleButtonPress.value ? OperationMode.FILL : OperationMode.EMPTY);
                ObfuscationReflectionHelper.setPrivateValue(DualityInterface.class, this.interfaceHost.getInterfaceDuality(), packetToggleButtonPress.value, "fluidPacket");
                break;
            case 3: this.interfaceHost.getInterfaceDuality().getConfigManager().putSetting(Settings.LEVEL_TYPE, packetToggleButtonPress.value ? LevelType.ITEM_LEVEL : LevelType.ENERGY_LEVEL);
                ObfuscationReflectionHelper.setPrivateValue(DualityInterface.class, this.interfaceHost.getInterfaceDuality(), packetToggleButtonPress.value, "allowSplitting");

        }
    }

    public void readServerPacket(PacketToggleButtonPress packetToggleButtonPress) {
        switch (packetToggleButtonPress.id) {
            case 0: this.blockingMode = packetToggleButtonPress.value;
                break;
            case 1: this.interfaceTerminal = packetToggleButtonPress.value;
                break;
            case 2: this.sendFluid = packetToggleButtonPress.value;
                break;
            case 3: this.splittingItemsFluids = packetToggleButtonPress.value;
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
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (!TJGuiUtils.isServer()) return;
        final boolean blockingMode = this.interfaceHost.getInterfaceDuality().getConfigManager().getSetting(Settings.BLOCK).ordinal() == 0;
        if (this.blockingMode != blockingMode) {
            this.blockingMode = blockingMode;
            ((EntityPlayerMP) this.inventoryPlayer.player).connection.sendPacket(new PacketToggleButtonPress(0, (int) this.inventoryPlayer.player.posX, (int) this.inventoryPlayer.player.posY, (int) this.inventoryPlayer.player.posZ, this.inventoryPlayer.player.world.provider.getDimension(), this.blockingMode));
        }
        final boolean interfaceTerminal = this.interfaceHost.getInterfaceDuality().getConfigManager().getSetting(Settings.INTERFACE_TERMINAL).ordinal() == 0;
        if (this.interfaceTerminal != interfaceTerminal) {
            this.interfaceTerminal = interfaceTerminal;
            ((EntityPlayerMP) this.inventoryPlayer.player).connection.sendPacket(new PacketToggleButtonPress(1, (int) this.inventoryPlayer.player.posX, (int) this.inventoryPlayer.player.posY, (int) this.inventoryPlayer.player.posZ, this.inventoryPlayer.player.world.provider.getDimension(), this.interfaceTerminal));
        }
        final boolean sendFluid = this.interfaceHost.getInterfaceDuality().getConfigManager().getSetting(Settings.OPERATION_MODE).ordinal() == 0;
        if (this.sendFluid != sendFluid) {
            this.sendFluid = sendFluid;
            ((EntityPlayerMP) this.inventoryPlayer.player).connection.sendPacket(new PacketToggleButtonPress(2, (int) this.inventoryPlayer.player.posX, (int) this.inventoryPlayer.player.posY, (int) this.inventoryPlayer.player.posZ, this.inventoryPlayer.player.world.provider.getDimension(), this.sendFluid));
        }
        final boolean splittingItemsFluids = this.interfaceHost.getInterfaceDuality().getConfigManager().getSetting(Settings.LEVEL_TYPE).ordinal() == 0;
        if (this.splittingItemsFluids != splittingItemsFluids) {
            this.splittingItemsFluids = splittingItemsFluids;
            ((EntityPlayerMP) this.inventoryPlayer.player).connection.sendPacket(new PacketToggleButtonPress(3, (int) this.inventoryPlayer.player.posX, (int) this.inventoryPlayer.player.posY, (int) this.inventoryPlayer.player.posZ, this.inventoryPlayer.player.world.provider.getDimension(), this.splittingItemsFluids));
        }
    }

    @Override
    public boolean canInteractWith(@Nonnull EntityPlayer playerIn) {
        return true;
    }

    public boolean isBlockingMode() {
        return this.blockingMode;
    }

    public boolean isInterfaceTerminal() {
        return this.interfaceTerminal;
    }

    public boolean isSendFluid() {
        return this.sendFluid;
    }

    public boolean isSplittingItemsFluids() {
        return this.splittingItemsFluids;
    }
}
