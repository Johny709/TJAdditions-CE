package tj.integration.ae2.items;

import appeng.api.definitions.IItemDefinition;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;


public class TJBlockContainerItemStorageCell extends TJItemStorageCell {

    public TJBlockContainerItemStorageCell(IItemDefinition material, int kiloBytes) {
        super(material, kiloBytes);
    }

    @Override
    public int getBytesPerType(@Nonnull ItemStack cellItem) {
        return 1;
    }

    @Override
    public int getTotalTypes(@Nonnull ItemStack cellItem) {
        return 1;
    }
}
