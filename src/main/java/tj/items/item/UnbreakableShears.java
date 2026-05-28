package tj.items.item;

import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import tj.TJ;

import javax.annotation.Nonnull;

public class UnbreakableShears extends ItemShears {
    public UnbreakableShears() {
        super();
        this.setMaxDamage(Integer.MAX_VALUE);
    }

    @Override
    public void setDamage(@Nonnull ItemStack stack, int damage) {}
}
