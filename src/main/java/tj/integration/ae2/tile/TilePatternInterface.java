package tj.integration.ae2.tile;

import appeng.tile.misc.TileInterface;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import tj.blocks.block.TJBlocks;
import tj.integration.ae2.helpers.DualitySuperInterface;

public class TilePatternInterface extends TileInterface {

    public TilePatternInterface() {
        ObfuscationReflectionHelper.setPrivateValue(TileInterface.class, this, new DualitySuperInterface(this.getProxy(), this, 18, 18, 36), "duality");
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return TJBlocks.PATTERN_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY);
    }
}
