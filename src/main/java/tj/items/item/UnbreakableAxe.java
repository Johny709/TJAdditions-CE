package tj.items.item;

import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import tj.TJ;

public class UnbreakableAxe extends ItemAxe {
    public UnbreakableAxe(ToolMaterial material) {
        super(material);
        this.setRegistryName(TJ.MODID, "unbreakable_axe");
        this.setTranslationKey("unbreakable_axe");
        this.setMaxDamage(Integer.MAX_VALUE);
    }

    @Override
    public void setDamage(ItemStack stack, int damage) {}
}
