package tj.gui.gui;

import appeng.helpers.IInterfaceHost;
import gregtech.api.gui.GuiTextures;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.items.IItemHandler;
import tj.gui.container.ContainerPatternInterface;
import tj.mui.TJGuiTextures;

import java.util.ArrayList;
import java.util.List;

public class GuiPatternInterface extends GuiContainer {

    private final IInterfaceHost interfaceHost;

    public GuiPatternInterface(InventoryPlayer inventoryPlayer, IInterfaceHost interfaceHost) {
        super(new ContainerPatternInterface(inventoryPlayer, interfaceHost));
        this.interfaceHost = interfaceHost;
        this.xSize = 211;
        this.ySize = 292;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        final List<Slot> slots = this.inventorySlots.inventorySlots;
        final ITooltipFlag tooltipFlag = this.mc.player.isSneaking() ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL;
        for (Slot slot : slots) {
            if (this.isMouseOverSlot(this.guiLeft + slot.xPos, this.guiTop + slot.yPos, mouseX, mouseY)) {
                final ItemStack itemStack = slot.getStack();
                if (itemStack.isEmpty()) continue;
                final List<String> tooltips = new ArrayList<>(itemStack.getTooltip(this.mc.player, tooltipFlag));
                GuiUtils.drawHoveringText(itemStack, tooltips, mouseX, mouseY, this.xSize, this.ySize, 300, this.fontRenderer);
            }
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        TJGuiTextures.SUPER_INTERFACE.draw(this.guiLeft, this.guiTop, this.xSize, this.ySize);
        final IItemHandler upgrades = this.interfaceHost.getInventoryByName("upgrades");
        final IItemHandler storage = this.interfaceHost.getInterfaceDuality().getStorage();
        final IItemHandler patterns = this.interfaceHost.getInterfaceDuality().getPatterns();
        for (int i = 0; i < upgrades.getSlots(); i++) {
            TJGuiTextures.UPGRADE_OVERLAY.draw(this.guiLeft + 186, this.guiTop + 7 + (18 * i), 18, 18);
        }
        for (int i = 0; i < storage.getSlots(); i++) {
            GuiTextures.SLOT.draw(this.guiLeft + 7 + (18 * (i % 9)), this.guiTop + 52 + (18 * (i / 9)), 18, 18);
        }
        for (int i = 0; i < patterns.getSlots(); i++) {
            GuiTextures.SLOT.draw(this.guiLeft + 7 + (18 * (i % 9)), this.guiTop + 109 + (18 * (i / 9)), 18, 18);
            TJGuiTextures.PATTERN_OVERLAY.draw(this.guiLeft + 7 + (18 * (i % 9)), this.guiTop + 109 + (18 * (i / 9)), 18, 18);
        }
    }

    private boolean isMouseOverSlot(int x, int y, int mouseX, int mouseY) {
        final int endX = x + 18;
        final int endY = y + 18;
        return mouseX >= x && mouseX <= endX && mouseY >= y && mouseY <= endY;
    }
}
