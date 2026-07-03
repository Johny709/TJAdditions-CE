package tj.mui.widgets.impl;

import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.helpers.IInterfaceHost;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.igredient.IIngredientSlot;
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
import tj.mui.TJGuiTextures;
import tj.mui.widgets.TJWidget;

import java.io.IOException;
import java.util.List;

public class AEItemListWidget extends TJWidget<AEItemListWidget> implements IIngredientSlot {

    private final Int2ObjectMap<Object> elements = new Int2ObjectLinkedOpenHashMap<>();
    private final Class<? extends IGridHost>[] gridHosts;
    private final IGrid grid;
    private final int posX;
    private int scrollOffset;
    private int scrollHeight;
    private int autoScrollY;
    private boolean autoScroll;

    @SafeVarargs
    public AEItemListWidget(int x, int y, int width, int height, IGridNode gridNode, Class<? extends IGridHost>... gridHosts) {
        super(new Position(x, y), new Size(width, height));
        this.grid = gridNode != null ? gridNode.getGrid() : null;
        this.posX = x;
        this.gridHosts = gridHosts;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInForeground(int mouseX, int mouseY) {
        if (!this.isMouseOverElement(mouseX, mouseY)) return;
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
                final int x = this.getPosition().getX() + slotXOffset;
                final int y = this.getPosition().getY() + scrollOffset - (this.scrollOffset % 18);
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
        if (this.autoScroll) {
            int scroll = mouseY - this.autoScrollY;
            if (scroll > 5 || scroll < -5) {
                scroll -= scroll < 0 ? 5 : -5;
                this.setScrollOffset(scroll);
                if (scroll > 0)
                    TJGuiTextures.AUTOSCROLL_DOWN.draw(mouseX - 8, mouseY - 8, 16, 16);
                else TJGuiTextures.AUTOSCROLL_UP.draw(mouseX - 8, mouseY - 8, 16, 16);
            } else TJGuiTextures.AUTOSCROLL.draw(mouseX - 8, mouseY - 8, 16, 16);
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
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (!this.isMouseOverElement(mouseX, mouseY))
            return false;
        if (this.autoScroll) {
            this.autoScroll = false;
        } else if (button == 2) {
            this.autoScrollY = mouseY;
            this.autoScroll = true;
        }
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
                final int x = this.getPosition().getX() + slotXOffset;
                final int y = this.getPosition().getY() + scrollOffset - (this.scrollOffset % 18);
                if (isMouseOver(x, y, 18, 18, mouseX, mouseY)) {
                    this.writeClientAction(2, buffer -> {
                        buffer.writeInt(entry.getIntKey());
                        buffer.writeBoolean(true); // isSlot is true.
                    });
                    return true;
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
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        if (this.isMouseInWidget(mouseX, mouseY))
            this.setScrollOffset(MathHelper.clamp(wheelDelta, -1, 1) * 10);
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
            } else if (id == 3) {
                this.gui.entityPlayer.inventory.setItemStack(buffer.readItemStack());
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
        } else if (id == 2) {
            this.onAction(buffer.readInt(), buffer.readBoolean());
        }
    }

    private void onAction(int i, boolean isSlot) {
        ItemStack playerStack = this.gui.entityPlayer.inventory.getItemStack();
        int index = 0;
        int scrollHeight = 0;
        grid:
        for (Class<? extends IGridHost> gridHost : this.gridHosts) {
            for (IGridNode gridNode : this.grid.getMachines(gridHost)) {
                if (!gridNode.isActive()) continue;
                final IInterfaceHost interfaceHost = (IInterfaceHost) gridNode.getMachine();
                if (interfaceHost.getInterfaceDuality().getConfigManager().getSetting(Settings.INTERFACE_TERMINAL) == YesNo.NO) continue;
                if (scrollHeight >= this.scrollOffset && scrollHeight <= this.scrollOffset + this.getSize().getHeight() + 18)
                    index++;
                scrollHeight += 18;
                final IItemHandler patternInventory = interfaceHost.getInterfaceDuality().getPatterns();
                for (int j = 0, slotColumn = 0; j < patternInventory.getSlots(); j++, slotColumn++) {
                    if (slotColumn > 8) {
                        scrollHeight += 18;
                        slotColumn = 0;
                    }
                    if (i == index) {
                        final ItemStack output = patternInventory.extractItem(j, Integer.MAX_VALUE, false);
                        if (patternInventory.insertItem(j, playerStack, false).isEmpty()) {
                            playerStack = output;
                        } else patternInventory.insertItem(j, output, false);
                        break grid;
                    }
                    if (scrollHeight >= this.scrollOffset && scrollHeight <= this.scrollOffset + this.getSize().getHeight() + 18)
                        index++;
                }
                scrollHeight += 18;
            }
        }
        final ItemStack finalPlayerStack = playerStack;
        this.gui.entityPlayer.inventory.setItemStack(finalPlayerStack);
        this.writeUpdateInfo(3, buffer -> buffer.writeItemStack(finalPlayerStack));
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        this.elements.clear();
        int index = 0;
        int scrollHeight = 0;
        for (Class<? extends IGridHost> gridHost : this.gridHosts) {
            for (IGridNode gridNode : this.grid.getMachines(gridHost)) {
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

    private void setScrollOffset(int delta) {
        this.scrollOffset -= delta;
        this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, this.scrollHeight - this.getSize().getHeight()));
        this.writeClientAction(1, buffer -> buffer.writeInt(this.scrollOffset));
    }

    @Override
    public Object getIngredientOverMouse(int mouseX, int mouseY) {
        if (this.isMouseOverElement(mouseX, mouseY)) {
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
                    final int x = this.getPosition().getX() + slotXOffset;
                    final int y = this.getPosition().getY() + scrollOffset - (this.scrollOffset % 18);
                    if (isMouseOver(x, y, 18, 18, mouseX, mouseY))
                        return entry.getValue();
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
        return null;
    }
}
