package tj.items.behaviours;

import gregtech.api.items.metaitem.stats.IItemBehaviour;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import java.util.List;

public class EnderCoverBehaviour implements IItemBehaviour {

    private final int transferRate;
    private final long capacity;

    public EnderCoverBehaviour(EnderCoverType enderCoverType, int tier) {
        this.capacity = enderCoverType == EnderCoverType.FLUID ? (int) Math.min(Math.pow(4, tier) * 1000, Integer.MAX_VALUE)
                : enderCoverType == EnderCoverType.ITEM ? (int) Math.min(Math.pow(4, tier) * 10, Integer.MAX_VALUE)
                : (long) (Math.pow(4, tier) * 1000);
        this.transferRate = enderCoverType == EnderCoverType.FLUID ? (int) Math.min(Math.pow(4, tier) * 16, Integer.MAX_VALUE)
                : enderCoverType == EnderCoverType.ITEM ? (int) Math.min(Math.round(Math.pow(4, tier) / 20), Integer.MAX_VALUE)
                : (int) Math.min(Math.pow(4, tier) * 8, Integer.MAX_VALUE);
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        lines.add(I18n.format("metaitem.ender_cover.description"));
        lines.add(I18n.format("metaitem.ender_cover.transfer", this.transferRate));
        lines.add(I18n.format("metaitem.ender_cover.capacity", this.capacity));
    }

    public enum EnderCoverType {
        FLUID,
        ITEM,
        ENERGY
    }
}
