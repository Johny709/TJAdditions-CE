package tj.items.behaviours;

import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.metaitem.stats.IItemColorProvider;
import gregtech.api.items.metaitem.stats.IItemNameProvider;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class TurbineUpgradeBehaviour implements IItemBehaviour, IItemColorProvider, IItemNameProvider {

    private final int color;
    private final int extraParallels;

    public TurbineUpgradeBehaviour(int color, int extraParallels) {
        this.color = color;
        this.extraParallels = extraParallels;
    }

    @Override
    public int getItemStackColor(ItemStack itemStack, int i) {
        return this.color;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getItemStackDisplayName(ItemStack itemStack, String unlocalizedName) {
        return I18n.format(unlocalizedName, this.extraParallels / 4);
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        lines.add(I18n.format("metaitem.turbine_upgrade.description", this.extraParallels));
    }

    public int getExtraParallels() {
        return this.extraParallels;
    }
}
