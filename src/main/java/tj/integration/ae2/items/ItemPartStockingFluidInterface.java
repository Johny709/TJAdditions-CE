package tj.integration.ae2.items;

import appeng.api.AEApi;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import tj.integration.ae2.part.PartStockingFluidInterface;
import tj.integration.ae2.part.PartSuperFluidInterface;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemPartStockingFluidInterface extends Item implements IPartItem<IPart> {

    public ItemPartStockingFluidInterface() {}

    @Nullable
    @Override
    public IPart createPartFromItemStack(ItemStack itemStack) {
        return new PartStockingFluidInterface(itemStack);
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUse(@Nonnull EntityPlayer player, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
        return AEApi.instance().partHelper().placeBus(player.getHeldItem(hand), pos, facing, player, hand, worldIn);
    }
}
