package tj.items.item;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import tj.TJ;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

import static tj.items.item.TJItems.UPGRADES;

public class ItemMaxCapacityUpgrade extends Item {

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flagIn) {
        tooltip.add(I18n.format("item.me.max_capacity_upgrade.description"));
        UPGRADES.forEach((key, value) -> tooltip.add(I18n.format("item." + Optional.ofNullable(key.getRegistryName()).orElse(new ResourceLocation(TJ.MODID, "")).getPath() + ".name", value)));
    }
}
