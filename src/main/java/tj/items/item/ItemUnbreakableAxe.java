package tj.items.item;

import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ItemUnbreakableAxe extends ItemAxe {
    public ItemUnbreakableAxe(ToolMaterial material) {
        super(material);
        this.setMaxDamage(Integer.MAX_VALUE);
    }

    @Override
    public void setDamage(@Nonnull ItemStack stack, int damage) {}
}
