package tj.gui;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

public class GuiUtils {

    /**
     *
     * @param widgetGroup widget group for slots to get added to
     * @param inventoryPlayer inventory of player
     * @param x X position
     * @param y Y position
     * @param stack the stack used to open this GUI. this prevents the item being taken out from slot while GUI is open.
     * @return widget
     */
    public static WidgetGroup bindPlayerInventory(WidgetGroup widgetGroup, InventoryPlayer inventoryPlayer, int x, int y, ItemStack stack) {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                widgetGroup.addWidget((new SlotWidget(inventoryPlayer, col + (row + 1) * 9, x + col * 18, y + row * 18))
                        .setBackgroundTexture(GuiTextures.SLOT)
                        .setLocationInfo(true, false));
            }
        }
        return bindPlayerHotbar(widgetGroup, inventoryPlayer, x, y + 58, stack);
    }

    private static WidgetGroup bindPlayerHotbar(WidgetGroup widgetGroup, InventoryPlayer inventoryPlayer, int x, int y, ItemStack stack) {
        for (int slot = 0; slot < 9; ++slot) {
            boolean interact = inventoryPlayer.player.inventory.getStackInSlot(slot) != stack;
            widgetGroup.addWidget((new SlotWidget(inventoryPlayer, slot, x + slot * 18, y, interact, interact))
                    .setBackgroundTexture(GuiTextures.SLOT)
                    .setLocationInfo(true, true));
        }
        return widgetGroup;
    }
}
