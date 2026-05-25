package tj.integration.appeng.items;

import appeng.api.definitions.IItemDefinition;
import net.minecraft.item.ItemStack;


public class TJBlockContainerItemStorageCell extends TJItemStorageCell {

    public TJBlockContainerItemStorageCell(IItemDefinition material, int kiloBytes) {
        super(material, kiloBytes);
    }

    @Override
    public int getBytesPerType(ItemStack cellItem) {
        return 1;
    }

    @Override
    public int getTotalTypes(ItemStack cellItem) {
        return 1;
    }
}
