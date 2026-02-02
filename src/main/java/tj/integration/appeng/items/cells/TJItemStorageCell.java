package tj.integration.appeng.items.cells;

import appeng.api.AEApi;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.InventoryAdaptor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import tj.integration.appeng.items.materials.TJAE2MaterialType;


public class TJItemStorageCell extends TJAbstractStorageCell<IAEItemStack> {

    protected final int perType;
    protected final double idleDrain;

    public TJItemStorageCell(TJAE2MaterialType materialType, int kiloBytes) {
        super(materialType, kiloBytes);
        this.idleDrain = 4.5 + (materialType.ordinal() * 0.5);
        this.perType = Math.min(16_777_215, kiloBytes / 128);
    }

    @Override
    public int getBytesPerType(ItemStack cellItem) {
        return this.perType;
    }

    @Override
    public double getIdleDrain() {
        return this.idleDrain;
    }

    @Override
    public IStorageChannel<IAEItemStack> getChannel() {
        return AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
    }

    @Override
    protected void dropEmptyStorageCellCase(final InventoryAdaptor ia, final EntityPlayer player) {
        AEApi.instance().definitions().materials().emptyStorageCell().maybeStack(1).ifPresent(is ->
        {
            final ItemStack extraA = ia.addItems(is);
            if (!extraA.isEmpty()) {
                player.dropItem(extraA, false);
            }
        });
    }
}
