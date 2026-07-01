package tj.mui.widgets;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.UnaryOperator;

public interface ISlotGroup {

    @SideOnly(Side.CLIENT)
    void addSlotToDrag(ISlotHandler slotHandler, Runnable callback);

    @SideOnly(Side.CLIENT)
    int getTimer();

    @SideOnly(Side.CLIENT)
    void setTimer(int timer);

    default UnaryOperator<ItemStack> getItemStackTransfer() {
        return null;
    }
}
