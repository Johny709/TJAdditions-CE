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
import tj.integration.ae2.part.PartPatternInterface;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static tj.integration.ae2.blocks.BlockPatternInterface.DUALITY_INSTANCE;

public class ItemPartPatternInterface extends Item implements IPartItem<IPart> {

    public ItemPartPatternInterface() {}

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flagIn) {
        if (DUALITY_INSTANCE != null) {
            tooltip.add(I18n.format("tile.me.super_interface.pattern_slots", DUALITY_INSTANCE.getPatterns().getSlots()));
            tooltip.add(I18n.format("tile.me.super_interface.storage_slots", DUALITY_INSTANCE.getStorage().getSlots()));
            tooltip.add(I18n.format("tile.me.super_interface.upgrade_slots", DUALITY_INSTANCE.getInventoryByName("upgrades").getSlots()));
        }
    }

    @Nullable
    @Override
    public IPart createPartFromItemStack(ItemStack itemStack) {
        return new PartPatternInterface(itemStack);
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUse(@Nonnull EntityPlayer player, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
        return AEApi.instance().partHelper().placeBus(player.getHeldItem(hand), pos, facing, player, hand, worldIn);
    }
}
