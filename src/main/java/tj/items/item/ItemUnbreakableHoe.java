package tj.items.item;

import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ItemUnbreakableHoe extends ItemHoe {
    public ItemUnbreakableHoe(ToolMaterial material) {
        super(material);
        this.setMaxDamage(Integer.MAX_VALUE);
    }

    @Override
    public void setDamage(@Nonnull ItemStack stack, int damage) {}
}
