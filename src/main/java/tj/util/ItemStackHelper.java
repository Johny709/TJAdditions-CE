package tj.util;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.function.BiConsumer;


public final class ItemStackHelper {

    private ItemStackHelper() {}

    /**
     * Tries to insert ItemStack into player's main inventory. Make sure to return a new copy of ItemStack before inserting an item from ItemHandler
     * @param inventory player inventory
     * @param stack the ItemStack to insert
     * @return ItemStack reminder. returns empty when ItemStack is fully inserted. returns the stack unmodified when unable to insert at all.
     */
    public static ItemStack insertInMainInventory(InventoryPlayer inventory, ItemStack stack) {
        return insertInPlayerInventory(inventory, stack, true, false, false);
    }

    /**
     * Tries to insert ItemStack into player's armor slot inventory. Make sure to return a new copy of ItemStack before inserting an item from ItemHandler
     * @param inventory player inventory
     * @param stack the ItemStack to insert
     * @return ItemStack reminder. returns empty when ItemStack is fully inserted. returns the stack unmodified when unable to insert at all.
     */
    public static ItemStack insertInArmorSlots(InventoryPlayer inventory, ItemStack stack) {
        return insertInPlayerInventory(inventory, stack, false, true, false);
    }

    /**
     * Tries to insert ItemStack into player's Off-Hand slot inventory. Make sure to return a new copy of ItemStack before inserting an item from ItemHandler
     * @param inventory player inventory
     * @param stack the ItemStack to insert
     * @return ItemStack reminder. returns empty when ItemStack is fully inserted. returns the stack unmodified when unable to insert at all.
     */
    public static ItemStack insertInOffHand(InventoryPlayer inventory, ItemStack stack) {
        return insertInPlayerInventory(inventory, stack, false, false, true);
    }

    /**
     * Tries to insert ItemStack into player inventory. Make sure to return a new copy of ItemStack before inserting an item from ItemHandler
     * @param inventory player inventory
     * @param stack the ItemStack to insert
     * @param mainInventory if you should insert in main inventory
     * @param armor if you should insert in armor slots
     * @param offHand if you should insert in off-Hand slot
     * @return ItemStack reminder. returns empty when ItemStack is fully inserted. returns the stack unmodified when unable to insert at all.
     */
    public static ItemStack insertInPlayerInventory(InventoryPlayer inventory, ItemStack stack, boolean mainInventory, boolean armor, boolean offHand) {
        if (inventory == null || stack.isEmpty())
            return stack;
        if (mainInventory)
            insertToAvailableSlots(inventory.mainInventory, stack);
        if (armor)
            insertToAvailableSlots(inventory.armorInventory, stack);
        if (offHand)
            insertToAvailableSlots(inventory.offHandInventory, stack);
        return stack;
    }

    private static void insertToAvailableSlots(NonNullList<ItemStack> stackList, ItemStack stack) {
        for (int i = 0; i < stackList.size(); i++) {
            ItemStack inventoryStack = stackList.get(i);
            if (inventoryStack.isEmpty()) {
                int shrink = Math.min(stack.getCount(), 64);
                ItemStack newStack = stack.copy();
                newStack.setCount(shrink);
                stackList.set(i, newStack);
                stack.shrink(shrink);
            } else if (inventoryStack.isItemEqual(stack) && ItemStack.areItemStackShareTagsEqual(inventoryStack, stack)) {
                int reminder = inventoryStack.getMaxStackSize() - inventoryStack.getCount();
                int shrink = Math.min(reminder, stack.getCount());
                inventoryStack.grow(shrink);
                stack.shrink(shrink);
            }
        }
    }

    /**
     * Tries to extract ItemStack from player inventory.
     * @param inventory player inventory
     * @param stack the ItemStack to extract
     * @param amount amount to extract
     * @param mainInventory if you should extract from main inventory
     * @param armor if you should extract from armor slots
     * @param offHand if you should extract from off-Hand slot
     * @return ItemStack that was extracted. returns empty if ItemStack wasn't able to extract at all.
     */
    public static ItemStack extractFromPlayerInventory(InventoryPlayer inventory, ItemStack stack, int amount, boolean mainInventory, boolean armor, boolean offHand) {
        if (inventory == null || stack.isEmpty())
            return ItemStack.EMPTY;
        ItemStack newStack = stack.copy();
        int count = 0;
        if (mainInventory)
            count += extractFromAvailableSlots(inventory.mainInventory, stack, amount);
        if (armor)
            count += extractFromAvailableSlots(inventory.armorInventory, stack, amount);
        if (offHand)
            count += extractFromAvailableSlots(inventory.offHandInventory, stack, amount);
        newStack.setCount(count);
        return newStack;
    }

    private static int extractFromAvailableSlots(NonNullList<ItemStack> stackList, ItemStack stack, int amount) {
        int count = 0;
        for (ItemStack inventoryStack : stackList) {
            if (!inventoryStack.isEmpty() && inventoryStack.isItemEqual(stack) && ItemStack.areItemStackShareTagsEqual(inventoryStack, stack)) {
                int extracted = Math.min(inventoryStack.getCount(), amount);
                inventoryStack.shrink(extracted);
                count += extracted;
                amount -= extracted;
            }
            if (amount < 1)
                break;
        }
        return count;
    }

    /**
     * Tries to insert into container inventory or item handler
     * @param itemHandler container inventory
     * @param stack the ItemStack to insert
     * @param simulate test to see if the item can be inserted without actually inserting the item for real.
     * @return ItemStack reminder. returns empty when ItemStack is fully inserted. returns the stack unmodified when unable to insert at all.
     */
    public static ItemStack insertIntoItemHandler(IItemHandler itemHandler, @Nonnull ItemStack stack, boolean simulate) {
        if (itemHandler == null || stack.isEmpty())
            return stack;

        stack = simulate ? stack.copy() : stack;
        for (int i = 0; i < itemHandler.getSlots() && !stack.isEmpty(); i++) {
            ItemStack slotStack = itemHandler.getStackInSlot(i);
            int maxStackSize = itemHandler.getSlotLimit(i);
            if (slotStack.isEmpty()) {
                stack = itemHandler.insertItem(i, stack, simulate);
            } else if (slotStack.isItemEqual(stack) && ItemStack.areItemStackShareTagsEqual(slotStack, stack)) {
                int reminder = Math.max(0, maxStackSize - slotStack.getCount());
                int extracted = Math.min(stack.getCount(), reminder);
                stack.shrink(extracted);
                if (!simulate)
                    slotStack.grow(extracted);
            }
        }
        return stack;
    }

    /**
     * Tries to insert into container inventory or item handler. Only recommended for client-side simulations.
     * @param itemHandler container inventory
     * @param stack the ItemStack to insert
     * @param simulate test to see if the item can be inserted without actually inserting the item for real.
     * @param beforeInsertedCallback run callback before the item gets inserted into slot.
     * @param afterInsertedCallback run callback after the item has been inserted into slot.
     * @return ItemStack reminder. returns empty when ItemStack is fully inserted. returns the stack unmodified when unable to insert at all.
     */
    public static ItemStack insertIntoItemHandlerWithCallback(IItemHandler itemHandler, @Nonnull ItemStack stack, boolean simulate, BiConsumer<Integer, ItemStack> beforeInsertedCallback, BiConsumer<Integer, ItemStack> afterInsertedCallback) {
        if (itemHandler == null || stack.isEmpty())
            return stack;

        stack = simulate ? stack.copy() : stack;
        for (int i = 0; i < itemHandler.getSlots() && !stack.isEmpty(); i++) {
            ItemStack slotStack = itemHandler.getStackInSlot(i);
            int maxStackSize = itemHandler.getSlotLimit(i);
            if (slotStack.isEmpty()) {
                beforeInsertedCallback.accept(i, stack);
                stack = itemHandler.insertItem(i, stack, simulate);
                afterInsertedCallback.accept(i, stack);
            } else if (slotStack.isItemEqual(stack) && ItemStack.areItemStackShareTagsEqual(slotStack, stack)) {
                beforeInsertedCallback.accept(i, stack);
                int reminder = Math.max(0, maxStackSize - slotStack.getCount());
                int extracted = Math.min(stack.getCount(), reminder);
                stack.shrink(extracted);
                if (!simulate)
                    slotStack.grow(extracted);
                afterInsertedCallback.accept(i, stack);
            }
        }
        return stack;
    }

    /**
     * Tries to extract from container inventory or item handler
     * @param itemHandler container inventory
     * @param stack the ItemStack to extract
     * @param amount the amount of items to extract and will be added to ItemStack count
     * @param simulate test to see if the item can be extracted without actually extracting the item for real.
     * @return The ItemStack extracted
     */
    public static ItemStack extractFromItemHandler(IItemHandler itemHandler, @Nonnull ItemStack stack, int amount, boolean simulate) {
        if (itemHandler == null || stack.isEmpty())
            return stack;

        stack = simulate ? stack.copy() : stack;
        int count = 0;
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack slotStack = itemHandler.getStackInSlot(i);
            if (slotStack.isItemEqual(stack) && ItemStack.areItemStackShareTagsEqual(slotStack, stack)) {
                int extracted = Math.min(slotStack.getCount(), amount);
                count += extracted;
                amount -= extracted;
                if (!simulate)
                    itemHandler.extractItem(i, extracted, false);
                if (amount < 1)
                    break;
            }
        }
        stack.grow(count);
        return stack;
    }

    /**
     * Tries to extract from container inventory or item handler with ingredients
     * @param itemHandler container inventory
     * @param ingredient the ItemStack to extract
     * @param amount the amount of items to extract and will be added to ItemStack count
     * @param simulate test to see if the item can be extracted without actually extracting the item for real.
     * @return The amount extracted
     */
    public static int extractFromItemHandlerByIngredient(IItemHandler itemHandler, @Nonnull Ingredient ingredient, int amount, boolean simulate) {
        if (itemHandler == null)
            return 0;

        int count = 0;
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack slotStack = itemHandler.getStackInSlot(i);
            if (ingredient.apply(slotStack)) {
                int extracted = itemHandler.extractItem(i, amount, simulate).getCount();
                count += extracted;
                amount -= extracted;
                if (amount < 1)
                    break;
            }
        }
        return count;
    }
}
