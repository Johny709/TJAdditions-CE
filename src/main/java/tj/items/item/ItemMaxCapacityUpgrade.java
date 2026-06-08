package tj.items.item;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static tj.items.item.TJItems.UPGRADES;

public class ItemMaxCapacityUpgrade extends Item {

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flagIn) {
        tooltip.add(I18n.format("item.me.max_capacity_upgrade.description"));
        UPGRADES.forEach((key, value) -> tooltip.add(I18n.format("item.me.max_capacity_upgrade.count", new ItemStack(key).getDisplayName(), value)));
    }
}
