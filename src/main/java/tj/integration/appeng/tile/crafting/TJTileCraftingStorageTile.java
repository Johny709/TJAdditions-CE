package tj.integration.appeng.tile.crafting;

import appeng.api.definitions.IBlocks;
import appeng.core.Api;
import appeng.tile.crafting.TileCraftingStorageTile;
import appeng.tile.crafting.TileCraftingTile;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;

import net.minecraft.util.EnumFacing;
import tj.integration.appeng.IApiBlocks;
import tj.integration.appeng.block.crafting.TJBlockCraftingUnit;

import java.util.EnumSet;
import java.util.Optional;

public class TJTileCraftingStorageTile extends TileCraftingStorageTile {
    private static final int KILO_SCALAR = 1024;

    @Override
    protected ItemStack getItemFromTile(Object obj) {
        final int storage = Math.round((float) ((TileCraftingTile) obj).getStorageBytes() / KILO_SCALAR);

        IApiBlocks blocks = ((IApiBlocks)(IBlocks) Api.INSTANCE.definitions().blocks());
        Optional<ItemStack> is;
        switch (storage) {
            case 65536: is = blocks.getCraftingStorage65m().maybeStack(1);
                break;
            case 262144: is = blocks.getCraftingStorage262m().maybeStack(1);
                break;
            case 1048576: is = blocks.getCraftingStorage1048m().maybeStack(1);
                break;
            case 2097152: is = blocks.getCraftingStorageSingularity().maybeStack(1);
                break;
            default: is = Optional.empty();
        }
        return is.orElseGet(() -> super.getItemFromTile(obj));
    }

    @Override
    public void updateMeta(boolean updateFormed) {
        if (this.world == null || this.notLoaded() || this.isInvalid()) {
            return;
        }

        final boolean formed = this.isFormed();
        boolean power = false;

        if (this.getProxy().isReady()) {
            power = this.getProxy().isActive();
        }

        final IBlockState current = this.world.getBlockState(this.pos);

        // The tile might try to update while being destroyed
        if (current.getBlock() instanceof TJBlockCraftingUnit) {
            final IBlockState newState = current.withProperty(TJBlockCraftingUnit.POWERED, power).withProperty(TJBlockCraftingUnit.FORMED, formed);

            if (current != newState) {
                // Not using flag 2 here (only send to clients, prevent block update) will cause infinite loops
                // In case there is an inconsistency in the crafting clusters.
                this.world.setBlockState(this.pos, newState, 2);
            }
        }

        if (updateFormed) {
            if (formed) {
                this.getProxy().setValidSides(EnumSet.allOf(EnumFacing.class));
            } else {
                this.getProxy().setValidSides(EnumSet.noneOf(EnumFacing.class));
            }
        }
    }

    @Override
    public boolean isAccelerator() {
        return false;
    }

    @Override
    public boolean isStorage() {
        return true;
    }

    @Override
    public int getStorageBytes() {
        if (this.world == null || this.notLoaded() || this.isInvalid()) {
            return 0;
        }
        return (int) Math.min(Integer.MAX_VALUE, (long) ((TJBlockCraftingUnit) this.world.getBlockState(this.pos).getBlock()).getType().getBytes() * KILO_SCALAR);
    }
}
