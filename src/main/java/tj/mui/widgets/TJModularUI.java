package tj.mui.widgets;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IUIHolder;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.TextureArea;
import net.minecraft.entity.player.EntityPlayer;

public final class TJModularUI {

    private final TJWidgetGroup<?> widgetGroup = new TJWidgetGroup<>();
    private final EntityPlayer player;
    private final IUIHolder holder;
    private TextureArea background = GuiTextures.BORDERED_BACKGROUND;
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

    public ModularUI build() {
        final int inventoryStartY = this.height - 82;
        return ModularUI.builder(this.background, this.width, this.height)
                .bindPlayerInventory(this.player.inventory, inventoryStartY)
                .widget(this.widgetGroup)
                .build(this.holder, this.player);
    }
}
