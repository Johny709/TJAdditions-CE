package tj.integration.jei;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.ItemStackKey;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class PartInfo {
    public final ItemStackKey itemStackKey;
    public boolean isController = false;
    public boolean isTile = false;
    public final int blockId;
    public int amount = 0;

    public PartInfo(final ItemStackKey itemStackKey, final BlockInfo blockInfo) {
        this.itemStackKey = itemStackKey;
        this.blockId = Block.getIdFromBlock(blockInfo.getBlockState().getBlock());
        TileEntity tileEntity = blockInfo.getTileEntity();
        if (tileEntity != null) {
            this.isTile = true;
            MetaTileEntity mte = ((MetaTileEntityHolder) tileEntity).getMetaTileEntity();
            if (mte instanceof MultiblockControllerBase)
                this.isController = true;
        }
    }

    public ItemStack getItemStack() {
        ItemStack result = this.itemStackKey.getItemStack();
        result.setCount(this.amount);
        return result;
    }
}
