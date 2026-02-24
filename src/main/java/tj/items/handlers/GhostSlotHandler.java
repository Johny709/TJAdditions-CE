package tj.items.handlers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.IItemHandlerModifiable;

public class GhostSlotHandler {

    private final boolean[] areGhostItems;

    public GhostSlotHandler(int size) {
        this.areGhostItems = new boolean[size];
    }

    public boolean[] getAreGhostItems() {
        return this.areGhostItems;
    }

    public void writeInitialSyncData(PacketBuffer buffer) {
        buffer.writeInt(this.areGhostItems.length);
        for (boolean isGhostItem : this.areGhostItems)
            buffer.writeBoolean(isGhostItem);
    }

    public void readInitialSyncData(PacketBuffer buffer) {
        int size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            this.areGhostItems[i] = buffer.readBoolean();
        }
    }

    public void clearInventory(IItemHandlerModifiable itemHandlerModifiable, NonNullList<ItemStack> itemBuffer) {
        for (int i = 0; i < itemHandlerModifiable.getSlots(); i++) {
            ItemStack stack = itemHandlerModifiable.getStackInSlot(i);
            if (!stack.isEmpty() && !this.areGhostItems[i]) {
                itemHandlerModifiable.setStackInSlot(i, ItemStack.EMPTY);
                itemBuffer.add(stack);
            }
        }
    }

    public void writeToNBT(NBTTagCompound data) {
        NBTTagList ghostItemList = new NBTTagList();
        for (boolean isGhostItem : this.areGhostItems)
            ghostItemList.appendTag(new NBTTagByte((byte) (isGhostItem ? 1 : 0)));
        data.setTag("ghostItems", ghostItemList);
    }

    public void readFromNBT(NBTTagCompound data) {
        NBTTagList ghostItemList = data.getTagList("ghostItems", 1);
        for (int i = 0; i < ghostItemList.tagCount(); i++)
            this.areGhostItems[i] = ((NBTTagByte) ghostItemList.get(i)).getByte() == 1;
    }
}
