package tj.items.item;

import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import tj.TJ;

public class UnbreakableHoe extends ItemHoe {
    public UnbreakableHoe(ToolMaterial material) {
        super(material);
        this.setRegistryName(TJ.MODID, "unbreakable_hoe");
        this.setTranslationKey("unbreakable_hoe");
        this.setMaxDamage(Integer.MAX_VALUE);
    }

    @Override
    public void setDamage(ItemStack stack, int damage) {}
}
