package tj.mui.widgets;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IUIHolder;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import tj.mui.TJGuiUtils;

import java.util.ArrayList;
import java.util.List;

public final class TJModularUI {

    private final TJWidgetGroup<?> widgetGroup = new TJWidgetGroup<>();
    private final List<Runnable> closeListeners = new ArrayList<>();
    private final List<Runnable> openListeners = new ArrayList<>();
    private final EntityPlayer player;
    private final IUIHolder holder;
    private TextureArea background = GuiTextures.BORDERED_BACKGROUND;
    private WidgetGroup playerSlotGroup;
    private int width = 176;
    private int height = 166;

    private TJModularUI(IUIHolder holder, EntityPlayer player) {
        this.player = player;
        this.holder = holder;
    }

    public static TJModularUI builder(IUIHolder holder, EntityPlayer player) {
        return new TJModularUI(holder, player);
    }

    /**
     * @param background set a background. Defaults to {@link GuiTextures#BORDERED_BACKGROUND}
     */
    public TJModularUI setBackground(TextureArea background) {
        this.background = background;
        return this;
    }

    public TJModularUI widget(Widget widget) {
        this.widgetGroup.addWidget(widget);
        return this;
    }

    public TJModularUI image(int x, int y, int width, int height, TextureArea textureArea) {
        this.widgetGroup.addWidget(new ImageWidget(x, y, width, height, textureArea));
        return this;
    }

    /**
     * @param width set height of GUI container. Default: 176
     */
    public TJModularUI setWidth(int width) {
        this.width = width;
        return this;
    }

    /**
     * @param height set height of GUI container. Default: 166
     */
    public TJModularUI setHeight(int height) {
        this.height = height;
        return this;
    }

    /**
     * @param width set height of GUI container. Default: 176
     * @param height set height of GUI container. Default: 166
     */
    public TJModularUI setWidthHeight(int width, int height) {
        this.setWidth(width).setHeight(height);
        return this;
    }

    public TJModularUI bindOpenListener(Runnable runnable) {
        this.openListeners.add(runnable);
        return this;
    }

    public TJModularUI bindCloseListener(Runnable runnable) {
        this.closeListeners.add(runnable);
        return this;
    }

    /**
     * @param itemBlacklist prevents slots being interacted with if they contain the passed in {@link ItemStack}
     */
    public TJModularUI bindPlayerInventory(ItemStack... itemBlacklist) {
        final int inventoryStartY = this.height - 82;
        this.playerSlotGroup = TJGuiUtils.bindPlayerInventory(new WidgetGroup(), player.inventory, 7, inventoryStartY, itemBlacklist);
        return this;
    }

    public ModularUI build() {
        final int inventoryStartY = this.height - 82;
        final ModularUI.Builder builder = ModularUI.builder(this.background, this.width, this.height);
        for (Runnable listener : this.openListeners)
            builder.bindOpenListener(listener);
        for (Runnable listener : this.closeListeners)
            builder.bindCloseListener(listener);
        if (this.playerSlotGroup == null) {
            builder.bindPlayerInventory(this.player.inventory, inventoryStartY);
        } else builder.widget(this.playerSlotGroup);
        return builder.widget(this.widgetGroup)
                .build(this.holder, this.player);
    }
}
