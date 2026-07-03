package tj.mui.widgets.impl;

import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.helpers.IInterfaceHost;
import appeng.tile.misc.TileInterface;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.util.Position;
import gregtech.api.util.RenderUtil;
import gregtech.api.util.Size;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import tj.TJ;
import tj.mui.widgets.TJWidget;

import java.io.IOException;
import java.util.List;

public class AEItemListWidget extends TJWidget<AEItemListWidget> {

    private final Int2ObjectMap<Object> elements = new Int2ObjectLinkedOpenHashMap<>();
    private final IGrid grid;
    private final int posX;
    private int scrollOffset;
    private int scrollHeight;

    public AEItemListWidget(int x, int y, int width, int height, IGrid grid) {
        super(new Position(x, y), new Size(width, height));
        this.grid = grid;
        this.posX = x;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInForeground(int mouseX, int mouseY) {
        if (!this.isMouseOverElement(mouseX, mouseY)) return;
        final Position pos = this.getPosition();
        int scrollOffset = 0;
        int slotColumn = 0;
        int slotXOffset = 0;
        for (Int2ObjectMap.Entry<Object> entry : this.elements.int2ObjectEntrySet()) {
            if (entry.getValue() instanceof ItemStack) {
                if (slotColumn > 8) {
                    slotColumn = 0;
                    scrollOffset += 18;
                    slotXOffset = 0;
                }
                final int x = pos.getX() + slotXOffset;
                final int y = pos.getY() + scrollOffset - (this.scrollOffset % 18);
                final ItemStack itemStack = (ItemStack) entry.getValue();
                if (!itemStack.isEmpty() && isMouseOver(x, y, 18, 18, mouseX, mouseY)) {
                    final List<String> tooltip = getItemToolTip(itemStack);
                    final String itemStoredText = I18n.format("gregtech.item_list.item_stored", itemStack.getCount());
                    tooltip.add(TextFormatting.GRAY + itemStoredText);
                    this.drawHoveringText(itemStack, tooltip, -1, mouseX, mouseY);
                }
                slotXOffset += 18;
                slotColumn++;
            } else {
                if (entry.getIntKey() > 0)
                    scrollOffset += 18;
                scrollOffset += 18;
                slotXOffset = 0;
                slotColumn = 0;
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        final Size size = this.getSize();
        final Position pos = this.getPosition();
        RenderUtil.useScissor(pos.getX(), pos.getY(), size.getWidth(), size.getHeight(), () -> {
            int scrollOffset = 0;
            int slotColumn = 0;
            int slotXOffset = 0;
            for (Int2ObjectMap.Entry<Object> entry : this.elements.int2ObjectEntrySet()) {
                if (entry.getValue() instanceof ItemStack) {
                    if (slotColumn > 8) {
                        slotColumn = 0;
                        scrollOffset += 18;
                        slotXOffset = 0;
                    }
                    final int x = pos.getX() + slotXOffset;
                    final int y = pos.getY() + scrollOffset - (this.scrollOffset % 18);
                    GuiTextures.SLOT.draw(x, y, 18, 18);
                    final ItemStack itemStack = (ItemStack) entry.getValue();
                    if (!itemStack.isEmpty())
                        drawItemStack(itemStack, x + 1, y + 1, null);
                    if (this.isMouseOverElement(mouseX, mouseY) && isMouseOver(x, y, 18, 18, mouseX, mouseY))
                        drawSelectionOverlay(x + 1, y + 1, 16, 16);
                    slotXOffset += 18;
                    slotColumn++;
                } else {
                    if (entry.getIntKey() > 0)
                        scrollOffset += 18;
                    this.drawStringSized((String) entry.getValue(), pos.getX(), pos.getY() + 4 + scrollOffset - (this.scrollOffset % 18), 0xAAAAAA, true, 1, false);
                    scrollOffset += 18;
                    slotXOffset = 0;
                    slotColumn = 0;
                }
            }
        });
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        if (this.isMouseInWidget(mouseX, mouseY)) {
            final int delta = MathHelper.clamp(wheelDelta, -1, 1) * 10;
            this.scrollOffset -= delta;
            this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, this.scrollHeight - this.getSize().getHeight()));
            this.writeClientAction(1, buffer -> buffer.writeInt(this.scrollOffset));
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        try {
            if (id == 1) {
                this.scrollHeight = buffer.readInt();
            } else if (id == 2) {
                this.elements.clear();
                final int size = buffer.readInt();
                for (int i = 0; i < size; i++) {
                    final int index = buffer.readInt();
                    if (buffer.readBoolean()) {
                        this.elements.put(index, buffer.readString(Short.MAX_VALUE));
                    } else this.elements.put(index, buffer.readItemStack());
                }
            }
        } catch (IOException e) {
            TJ.logger.info(e.getMessage());
        }
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 1) {
            this.scrollOffset = buffer.readInt();
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        this.elements.clear();
        int index = 0;
        int scrollHeight = 0;
        for (IGridNode gridNode : this.grid.getMachines(TileInterface.class)) {
            if (!gridNode.isActive()) continue;
            final IInterfaceHost interfaceHost = (IInterfaceHost) gridNode.getMachine();
            if (interfaceHost.getInterfaceDuality().getConfigManager().getSetting(Settings.INTERFACE_TERMINAL) == YesNo.NO) continue;
            final IItemHandler patternInventory = interfaceHost.getInterfaceDuality().getPatterns();
            if (scrollHeight >= this.scrollOffset && scrollHeight <= this.scrollOffset + this.getSize().getHeight() + 18)
                this.elements.put(index++, interfaceHost.getInterfaceDuality().getTermName());
            scrollHeight += 18;
            for (int i = 0, slotColumn = 0; i < patternInventory.getSlots(); i++, slotColumn++) {
                if (slotColumn > 8) {
                    scrollHeight += 18;
                    slotColumn = 0;
                }
                if (scrollHeight >= this.scrollOffset && scrollHeight <= this.scrollOffset + this.getSize().getHeight() + 18)
                    this.elements.put(index++, patternInventory.getStackInSlot(i));
            }
            scrollHeight += 18;
        }
        if (this.scrollHeight != scrollHeight) {
            this.scrollHeight = scrollHeight;
            this.writeUpdateInfo(1, buffer -> buffer.writeInt(this.scrollHeight));
        }
        this.writeUpdateInfo(2, buffer -> {
            buffer.writeInt(this.elements.size());
            for (Int2ObjectMap.Entry<Object> entry : this.elements.int2ObjectEntrySet()) {
                buffer.writeInt(entry.getIntKey());
                final boolean isName = entry.getValue() instanceof String;
                buffer.writeBoolean(isName);
                if (isName) {
                    buffer.writeString((String) entry.getValue());
                } else buffer.writeItemStack((ItemStack) entry.getValue());
            }
        });
    }

    private boolean isMouseInWidget(int mouseX, int mouseY) {
        final Size size = this.getSize();
        final int posY = this.getPosition().getY();
        return mouseX >= this.posX && mouseX <= this.posX + size.getWidth() && mouseY >= posY && mouseY <= posY + size.getHeight();
    }
}
