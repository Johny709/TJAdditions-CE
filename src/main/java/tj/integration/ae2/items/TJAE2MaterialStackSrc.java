package tj.integration.ae2.items;

import appeng.core.features.IStackSrc;
import com.google.common.base.Preconditions;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import tj.integration.ae2.items.materials.TJAE2MaterialType;


public class TJAE2MaterialStackSrc implements IStackSrc {

    private final TJAE2MaterialType src;
    private final boolean enabled;

    public TJAE2MaterialStackSrc(final TJAE2MaterialType src, boolean enabled) {
        Preconditions.checkNotNull(src);

        this.src = src;
        this.enabled = enabled;
    }

    @Override
    public ItemStack stack(final int stackSize) {
        return this.src.stack(stackSize);
    }

    @Override
    public Item getItem() {
        return this.src.getItemInstance();
    }

    @Override
    public int getDamage() {
        return this.src.getDamageValue();
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}
