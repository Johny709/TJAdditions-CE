package tj.integration.ae2.tile.crafting;

import appeng.tile.crafting.TileCraftingTile;
import net.minecraft.item.ItemStack;
import tj.integration.ae2.TJAE2API;
import tj.integration.ae2.block.crafting.TJBlockCraftingUnit;

import java.util.Optional;

public class TJTileCraftingStorageTile extends TileCraftingTile {
    private static final int KILO_SCALAR = 1024;

    @Override
    protected ItemStack getItemFromTile(Object obj) {
        final int storage = Math.round((float) ((TileCraftingTile) obj).getStorageBytes() / KILO_SCALAR);

        Optional<ItemStack> is;
        switch (storage) {
            case 65536: is = TJAE2API.INSTANCE.getCraftingStorage65m().maybeStack(1);
                break;
            case 262144: is = TJAE2API.INSTANCE.getCraftingStorage262m().maybeStack(1);
                break;
            case 1048576: is = TJAE2API.INSTANCE.getCraftingStorage1048m().maybeStack(1);
                break;
            case 2097152: is = TJAE2API.INSTANCE.getCraftingStorageSingularity().maybeStack(1);
                break;
            default: is = java.util.Optional.empty();
        }
        return is.orElseGet(() -> super.getItemFromTile(obj));
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
