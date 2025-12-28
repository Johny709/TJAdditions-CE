package tj.gui.widgets;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ISlotHandler {

    @SideOnly(Side.CLIENT)
    void onRemove();

    @SideOnly(Side.CLIENT)
    void setSimulatedAmount(int amount);

    @SideOnly(Side.CLIENT)
    int getSimulatedAmount();

    int index();

    ItemStack insert(ItemStack stack, boolean simulate);

    ItemStack extract(int amount, ItemStack stack, boolean simulate);
}
