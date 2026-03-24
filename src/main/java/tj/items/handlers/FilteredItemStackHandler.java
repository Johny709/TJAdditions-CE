package tj.items.handlers;

import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.util.TriConsumer;

import javax.annotation.Nonnull;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

/**
 * Recommended to use TJSlotWidget for player interacting with this.
 */
public class FilteredItemStackHandler extends LargeItemStackHandler {

    private final MetaTileEntity tileEntity;
    private TriConsumer<Integer, ItemStack, Boolean> onContentsChangedPre;
    private BiConsumer<Integer, ItemStack> onContentsChangedPost;
    private BiPredicate<Integer, ItemStack> itemStackPredicate;

    public FilteredItemStackHandler(MetaTileEntity tileEntity) {
        this(tileEntity, 1, 64);
    }

    public FilteredItemStackHandler(MetaTileEntity tileEntity, int slots) {
        this(tileEntity, slots, 64);
    }

    public FilteredItemStackHandler(MetaTileEntity tileEntity, int slots, int capacity) {
        super(slots, capacity);
        this.tileEntity = tileEntity;
    }

    /**
     * Listener to detect items going to be inserted or extracted. Not detected on simulation.
     * @param onContentsChanged (slot, ItemStack, isInsert) ->
     */
    public FilteredItemStackHandler setOnContentsChangedPre(TriConsumer<Integer, ItemStack, Boolean> onContentsChanged) {
        this.onContentsChangedPre = onContentsChanged;
        return this;
    }

    /**
     * Listener to detect items after they've been inserted or extracted. Not detected on simulation.
     * @param onContentsChangedPost (slot, ItemStack) ->
     */
    public FilteredItemStackHandler setOnContentsChangedPost(BiConsumer<Integer, ItemStack> onContentsChangedPost) {
        this.onContentsChangedPost = onContentsChangedPost;
        return this;
    }

    /**
     * @param itemStackPredicate (slot, ItemStack) ->
     */
    public FilteredItemStackHandler setItemStackPredicate(BiPredicate<Integer, ItemStack> itemStackPredicate) {
        this.itemStackPredicate = itemStackPredicate;
        return this;
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (this.itemStackPredicate != null && !this.itemStackPredicate.test(slot, stack))
            return stack;
        if (!simulate && this.onContentsChangedPre != null)
            this.onContentsChangedPre.accept(slot, stack, true);
        return super.insertItem(slot, stack, simulate);
    }

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack upgradeStack = this.getStackInSlot(slot);
        if (!simulate && this.onContentsChangedPre != null)
            this.onContentsChangedPre.accept(slot, upgradeStack, false);
        return super.extractItem(slot, amount, simulate);
    }

    @Override
    protected void onContentsChanged(int slot) {
        if (this.onContentsChangedPost != null)
            this.onContentsChangedPost.accept(slot, this.getStackInSlot(slot));
        if (this.tileEntity != null)
            this.tileEntity.markDirty();
    }
}
