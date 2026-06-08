package tj.items.item;

import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ItemUnbreakableShears extends ItemShears {
    public ItemUnbreakableShears() {
        super();
        this.setMaxDamage(Integer.MAX_VALUE);
    }

    @Override
    public void setDamage(@Nonnull ItemStack stack, int damage) {}
}
