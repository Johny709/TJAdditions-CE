package tj.items.item;

import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import tj.TJ;

public class UnbreakableShears extends ItemShears {
    public UnbreakableShears() {
        super();
        this.setRegistryName(TJ.MODID, "unbreakable_shears");
        this.setTranslationKey("unbreakable_shears");
        this.setMaxDamage(Integer.MAX_VALUE);
    }

    @Override
    public void setDamage(ItemStack stack, int damage) {}
}
