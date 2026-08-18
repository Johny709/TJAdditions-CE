package tj.integration.ae2.items;

import appeng.api.AEApi;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.integration.ae2.part.PartStockingFluidInterface;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static tj.integration.ae2.blocks.BlockStockingFluidInterface.FLUID_DUALITY_INSTANCE;

public class ItemPartStockingFluidInterface extends Item implements IPartItem<IPart> {

    public ItemPartStockingFluidInterface() {}

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flagIn) {
        if (FLUID_DUALITY_INSTANCE != null) {
            tooltip.add(I18n.format("tile.me.super_fluid_interface.fluid_tanks", FLUID_DUALITY_INSTANCE.getTanks().getSlots()));
            tooltip.add(I18n.format("tile.me.super_fluid_interface.upgrade_slots", FLUID_DUALITY_INSTANCE.getInventoryByName("upgrades").getSlots()));
        }
    }

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
